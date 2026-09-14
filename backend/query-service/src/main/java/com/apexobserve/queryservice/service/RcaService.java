package com.apexobserve.queryservice.service;

import com.apexobserve.queryservice.repository.DependencyRepository;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Evidence-based RCA engine.
 *
 * Strategy:
 *   1. Fetch the full dependency graph from trace-derived relationships.
 *   2. Identify upstream callers and downstream callees of the affected service.
 *   3. Rank candidate root-cause services by: (a) dependency direction, (b) proximity,
 *      (c) call-count weight.
 *   4. The downstream service with the highest call-count involvement ranks first.
 *      If no downstream exists, the service itself is the most likely root cause.
 *   5. Confidence = 0.1 (insufficient evidence) when graph is empty.
 *      Confidence scales with call_count strength, capped at 0.92.
 *
 * Deliberately NOT: "first downstream" heuristic; NOT an LLM.
 */
@Service
public class RcaService {
    private final DependencyRepository dependencyRepository;

    public RcaService(DependencyRepository dependencyRepository) {
        this.dependencyRepository = dependencyRepository;
    }

    public Map<String, Object> analyzeRootCause(String tenantId, String affectedService) {
        List<Object[]> deps = dependencyRepository.findServiceDependencies(tenantId);

        if (deps == null || deps.isEmpty()) {
            return buildResult(
                affectedService,
                0.15,
                "No dependency data available (no traces recorded). Unable to determine external root cause.",
                List.of("No dependency graph"),
                List.of(),
                List.of()
            );
        }

        // Build adjacency: source -> [(target, callCount)]
        Map<String, List<long[]>> outgoing = new HashMap<>();  // source -> [targetIndex, callCount] using parallel list
        Map<String, Map<String, Long>> outgoingMap = new HashMap<>(); // source -> {target -> callCount}
        Map<String, Long> incomingCallCount = new HashMap<>(); // target -> total calls received

        for (Object[] row : deps) {
            String source = (String) row[0];
            String target = (String) row[1];
            long callCount = ((Number) row[2]).longValue();

            outgoingMap.computeIfAbsent(source, k -> new HashMap<>()).put(target, callCount);
            incomingCallCount.merge(target, callCount, Long::sum);
        }

        // Downstream: services that affectedService calls
        Map<String, Long> downstream = outgoingMap.getOrDefault(affectedService, Collections.emptyMap());

        // Upstream: services that call affectedService
        Map<String, Long> upstream = new HashMap<>();
        for (Object[] row : deps) {
            String source = (String) row[0];
            String target = (String) row[1];
            long callCount = ((Number) row[2]).longValue();
            if (target.equals(affectedService)) {
                upstream.put(source, callCount);
            }
        }

        Map<String, Object> rcaResult;

        if (downstream.isEmpty()) {
            // affectedService has no callees — it is a leaf node or the root itself
            rcaResult = buildResult(
                affectedService,
                0.82,
                String.format(
                    "Service '%s' has no downstream dependencies in the current trace graph. " +
                    "The issue is likely internal to this service (leaf node behavior). " +
                    "Upstream callers: %s.",
                    affectedService, upstream.isEmpty() ? "none" : String.join(", ", upstream.keySet())
                ),
                List.of("Dependency graph: leaf node", "No downstream callees"),
                List.of(affectedService),
                new ArrayList<>(upstream.keySet())
            );
        } else {
            // Rank downstream candidates by call count (most-called = most-likely bottleneck)
            String topCandidate = downstream.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(affectedService);

            long topCallCount = downstream.get(topCandidate);
            long totalOutgoing = downstream.values().stream().mapToLong(Long::longValue).sum();

            // Confidence: proportion of calls going to top candidate, scaled 0.5–0.92
            double callShareRatio = totalOutgoing > 0 ? (double) topCallCount / totalOutgoing : 0;
            double confidence = 0.50 + callShareRatio * 0.42;
            confidence = Math.min(0.92, Math.max(0.50, confidence));

            List<String> evidence = new ArrayList<>();
            evidence.add(String.format("'%s' calls '%s' %d times", affectedService, topCandidate, topCallCount));
            evidence.add(String.format("'%s' accounts for %.0f%% of '%s' outgoing traffic", topCandidate, callShareRatio * 100, affectedService));
            if (downstream.size() > 1) {
                evidence.add(String.format("Other downstream candidates: %s", downstream.keySet().stream()
                    .filter(s -> !s.equals(topCandidate))
                    .reduce((a, b) -> a + ", " + b).orElse("none")));
            }

            rcaResult = buildResult(
                topCandidate,
                confidence,
                String.format(
                    "Root cause likely in '%s'. It is the primary downstream dependency of '%s' " +
                    "with %d recorded call-spans. A failure or degradation in '%s' would propagate to '%s'.",
                    topCandidate, affectedService, topCallCount, topCandidate, affectedService
                ),
                evidence,
                new ArrayList<>(downstream.keySet()),
                new ArrayList<>(upstream.keySet())
            );
        }

        return rcaResult;
    }

    public Map<String, Object> analyzeBlastRadius(String tenantId, String rootService) {
        List<Object[]> deps = dependencyRepository.findServiceDependencies(tenantId);

        // BFS upstream: find all services that transitively depend on rootService
        Map<String, Set<String>> upstreamCallers = new HashMap<>(); // target -> set of sources
        for (Object[] row : deps) {
            String source = (String) row[0];
            String target = (String) row[1];
            upstreamCallers.computeIfAbsent(target, k -> new HashSet<>()).add(source);
        }

        Set<String> directlyAffected = new HashSet<>();
        Set<String> transitivelyAffected = new HashSet<>();
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new LinkedList<>();

        // Seed with direct callers
        Set<String> directCallers = upstreamCallers.getOrDefault(rootService, Collections.emptySet());
        directlyAffected.addAll(directCallers);
        queue.addAll(directCallers);
        visited.add(rootService);
        visited.addAll(directCallers);

        // BFS for transitive callers (cycle-safe via visited set)
        while (!queue.isEmpty()) {
            String current = queue.poll();
            Set<String> callers = upstreamCallers.getOrDefault(current, Collections.emptySet());
            for (String caller : callers) {
                if (!visited.contains(caller)) {
                    visited.add(caller);
                    transitivelyAffected.add(caller);
                    queue.add(caller);
                }
            }
        }

        // Remove direct from transitive to keep them separate
        transitivelyAffected.removeAll(directlyAffected);

        String severity;
        int total = directlyAffected.size() + transitivelyAffected.size();
        if (total >= 4) severity = "CRITICAL";
        else if (total >= 2) severity = "HIGH";
        else if (total >= 1) severity = "MEDIUM";
        else severity = "LOW";

        Map<String, Object> result = new HashMap<>();
        result.put("rootService", rootService);
        result.put("directlyAffected", new ArrayList<>(directlyAffected));
        result.put("transitivelyAffected", new ArrayList<>(transitivelyAffected));
        result.put("totalAffectedCount", total);
        result.put("severity", severity);
        result.put("evidence", String.format(
            "%d direct callers and %d transitive callers would be impacted by a failure in '%s'.",
            directlyAffected.size(), transitivelyAffected.size(), rootService
        ));
        return result;
    }

    private Map<String, Object> buildResult(
            String rootCause, double confidence, String explanation,
            List<String> evidence, List<String> affected, List<String> upstreams) {
        Map<String, Object> r = new HashMap<>();
        r.put("rootCause", rootCause);
        r.put("confidence", confidence);
        r.put("explanation", explanation);
        r.put("contributingSignals", evidence);
        r.put("affectedServices", affected);
        r.put("upstreamCallers", upstreams);
        return r;
    }
}
