package com.apexobserve.queryservice.controller;

import com.apexobserve.queryservice.entity.MetricEntity;
import com.apexobserve.queryservice.repository.MetricRepository;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {
    
    private final MetricRepository metricRepository;
    private final com.apexobserve.queryservice.repository.TraceRepository traceRepository;

    public MetricsController(MetricRepository metricRepository, com.apexobserve.queryservice.repository.TraceRepository traceRepository) {
        this.metricRepository = metricRepository;
        this.traceRepository = traceRepository;
    }

    @GetMapping("/query")
    public List<MetricEntity> queryMetrics(
            @RequestHeader(value = "X-Tenant-ID", defaultValue = "default") String tenantId,
            @RequestParam String serviceId,
            @RequestParam String metricName,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        
        OffsetDateTime start;
        OffsetDateTime end;
        try {
            start = OffsetDateTime.parse(startTime);
            end = OffsetDateTime.parse(endTime);
        } catch (Exception e) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST, "Invalid ISO-8601 timestamp format for startTime or endTime");
        }

        if (start.isAfter(end)) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST, "startTime must be before or equal to endTime");
        }
        
        return metricRepository.findMetrics(tenantId, serviceId, metricName, start, end);
    }

    /**
     * Compute a real health score from persisted telemetry.
     *
     * Score formula (bounded 0–100):
     *   Base = 100
     *   - Error penalty:  error_rate > 0      → subtract up to 50 (linearly, max at error_rate = 1.0)
     *   - Latency penalty: avg latency > 500ms → subtract up to 30 (linearly, max at latency = 5000ms)
     *   - Availability:   traffic == 0         → subtract 20 (no-traffic penalty)
     *
     * Rationale:
     *   - Errors are the primary signal; a 100% error rate yields -50.
     *   - Latency degrades experience but is secondary; 5s avg latency yields -30.
     *   - Zero traffic might mean the service is down or unstaffed; yields -20.
     *   - All penalties cap to their max; score never goes below 0.
     *
     * Services with no persisted telemetry at all are excluded from the response.
     */
    @GetMapping("/health")
    public Map<String, Object> getHealthScores(
            @RequestHeader(value = "X-Tenant-ID", defaultValue = "default") String tenantId) {

        OffsetDateTime startTime = OffsetDateTime.now().minusHours(1);
        List<Object[]> rawScores = traceRepository.calculateHealthScoreBase(tenantId, startTime);
        
        Map<String, Object> response = new HashMap<>();
        
        for (Object[] row : rawScores) {
            String serviceId = (String) row[0];
            double avgLatencyMs  = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
            double totalErrors   = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
            double totalTraffic  = row[3] != null ? ((Number) row[3]).doubleValue() : 0.0;

            double score = 100.0;

            // Error rate penalty: 0–50 points, maxed at totalErrors >= totalTraffic (100% errors)
            if (totalTraffic > 0) {
                double errorRate = Math.min(1.0, totalErrors / totalTraffic);
                score -= errorRate * 50.0;
            }

            // Latency penalty: 0–30 points, maxed at avgLatencyMs >= 5000ms
            if (avgLatencyMs > 500.0) {
                double latencyExcess = Math.min(4500.0, avgLatencyMs - 500.0); // excess above 500ms
                score -= (latencyExcess / 4500.0) * 30.0;
            }

            // No-traffic penalty: assume service may be down
            if (totalTraffic == 0) {
                score -= 20.0;
            }

            score = Math.max(0.0, Math.min(100.0, score));
            
            Map<String, Object> metrics = new HashMap<>();
            metrics.put("score",   score);
            metrics.put("latency", avgLatencyMs);
            metrics.put("errors",  totalErrors);
            metrics.put("traffic", totalTraffic);
            // Include evidence so callers understand why the score is what it is
            metrics.put("scoreBreakdown", buildBreakdown(score, avgLatencyMs, totalErrors, totalTraffic));
            
            response.put(serviceId, metrics);
        }
        
        return response;
    }

    private Map<String, Object> buildBreakdown(double score, double latency, double errors, double traffic) {
        Map<String, Object> b = new HashMap<>();
        b.put("base", 100.0);
        double errorPenalty = 0.0;
        if (traffic > 0) errorPenalty = Math.min(1.0, errors / traffic) * 50.0;
        double latencyPenalty = 0.0;
        if (latency > 500.0) latencyPenalty = (Math.min(4500.0, latency - 500.0) / 4500.0) * 30.0;
        double trafficPenalty = traffic == 0 ? 20.0 : 0.0;
        b.put("errorPenalty",   errorPenalty);
        b.put("latencyPenalty", latencyPenalty);
        b.put("trafficPenalty", trafficPenalty);
        b.put("finalScore",     score);
        return b;
    }
}
