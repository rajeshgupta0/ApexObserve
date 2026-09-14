package com.apexobserve.alertingservice.service;

import com.apexobserve.alertingservice.entity.AlertEntity;
import com.apexobserve.alertingservice.entity.IncidentEntity;
import com.apexobserve.alertingservice.entity.IncidentAlertEntity;
import com.apexobserve.alertingservice.repository.AlertRepository;
import com.apexobserve.alertingservice.repository.IncidentRepository;
import com.apexobserve.alertingservice.repository.IncidentAlertRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AlertCorrelationEngine {
    private static final Logger log = LoggerFactory.getLogger(AlertCorrelationEngine.class);
    private final AlertRepository alertRepository;
    private final IncidentRepository incidentRepository;
    private final IncidentAlertRepository incidentAlertRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    public AlertCorrelationEngine(AlertRepository alertRepository, IncidentRepository incidentRepository, IncidentAlertRepository incidentAlertRepository) {
        this.alertRepository = alertRepository;
        this.incidentRepository = incidentRepository;
        this.incidentAlertRepository = incidentAlertRepository;
    }

    @Scheduled(fixedRate = 60000)
    public void correlateAlerts() {
        log.info("Running Alert Correlation Engine...");
        try {
            // Get all active alerts
            List<AlertEntity> activeAlerts = alertRepository.findByTenantIdAndStatusOrderByCreatedAtDesc("default", "ACTIVE");
            if (activeAlerts.size() < 2) return; // Need at least 2 to correlate

            // Fetch Dependency Graph to check relationships
            Map<String, Object> graph = null;
            try {
                graph = restTemplate.getForObject((System.getenv("QUERY_SERVICE_URL") != null ? System.getenv("QUERY_SERVICE_URL") : "http://localhost:8083") + "/api/dependencies/graph", Map.class);
            } catch (Exception e) {
                log.warn("Could not fetch dependency graph for correlation: {}", e.getMessage());
            }

            // Simple topological grouping logic
            Set<String> processedAlertIds = new HashSet<>();
            
            for (AlertEntity rootAlert : activeAlerts) {
                if (processedAlertIds.contains(rootAlert.getId().toString())) continue;
                
                List<AlertEntity> correlated = new ArrayList<>();
                correlated.add(rootAlert);
                processedAlertIds.add(rootAlert.getId().toString());
                
                String rootService = rootAlert.getServiceId();
                
                for (AlertEntity otherAlert : activeAlerts) {
                    if (processedAlertIds.contains(otherAlert.getId().toString())) continue;
                    
                    boolean isRelated = false;
                    // Temporal correlation (within 5 mins)
                    long timeDiff = Math.abs(rootAlert.getCreatedAt().toEpochSecond() - otherAlert.getCreatedAt().toEpochSecond());
                    if (timeDiff <= 300) {
                        // Topological correlation
                        if (rootService.equals(otherAlert.getServiceId())) {
                            isRelated = true;
                        } else if (graph != null && graph.get("edges") != null) {
                            List<Map<String, Object>> edges = (List<Map<String, Object>>) graph.get("edges");
                            for (Map<String, Object> edge : edges) {
                                String source = (String) edge.get("source");
                                String target = (String) edge.get("target");
                                if ((source.equals(rootService) && target.equals(otherAlert.getServiceId())) ||
                                    (target.equals(rootService) && source.equals(otherAlert.getServiceId()))) {
                                    isRelated = true;
                                    break;
                                }
                            }
                        }
                    }
                    
                    if (isRelated) {
                        correlated.add(otherAlert);
                        processedAlertIds.add(otherAlert.getId().toString());
                    }
                }
                
                // If we found correlated alerts, create/update an incident
                if (correlated.size() > 1) {
                    createIncident(correlated);
                }
            }
        } catch (Exception e) {
            log.error("Failed to correlate alerts", e);
        }
    }

    private void createIncident(List<AlertEntity> alerts) {
        try {
            // Check if any alert is already in an OPEN incident
            IncidentEntity incident = null;
            for (AlertEntity alert : alerts) {
                List<IncidentAlertEntity> links = incidentAlertRepository.findByAlertId(alert.getId());
                for (IncidentAlertEntity link : links) {
                    IncidentEntity existing = incidentRepository.findById(link.getIncidentId()).orElse(null);
                    if (existing != null && ("OPEN".equals(existing.getStatus()) || "INVESTIGATING".equals(existing.getStatus()))) {
                        incident = existing;
                        break;
                    }
                }
                if (incident != null) break;
            }
            
            if (incident == null) {
                incident = new IncidentEntity();
                incident.setId(UUID.randomUUID());
                incident.setTenantId("default");
                
                Set<String> services = alerts.stream().map(AlertEntity::getServiceId).collect(Collectors.toSet());
                incident.setTitle("Multi-Service Degradation: " + String.join(", ", services));
                incident.setStatus("OPEN");
                
                boolean hasCritical = alerts.stream().anyMatch(a -> "CRITICAL".equals(a.getSeverity()));
                incident.setSeverity(hasCritical ? "CRITICAL" : "WARNING");
                
                incident.setCreatedAt(OffsetDateTime.now());
                incident.setUpdatedAt(OffsetDateTime.now());
                
                incident.setAffectedServices(mapper.writeValueAsString(services));
                incidentRepository.save(incident);
                log.info("Created new incident {} for {} correlated alerts", incident.getId(), alerts.size());
            } else {
                Set<String> services = alerts.stream().map(AlertEntity::getServiceId).collect(Collectors.toSet());
                incident.setAffectedServices(mapper.writeValueAsString(services));
                incident.setUpdatedAt(OffsetDateTime.now());
                incidentRepository.save(incident);
            }
            
            // Link alerts
            final UUID finalIncidentId = incident.getId();
            for (AlertEntity alert : alerts) {
                boolean linked = incidentAlertRepository.findByAlertId(alert.getId()).stream()
                    .anyMatch(link -> link.getIncidentId().equals(finalIncidentId));
                if (!linked) {
                    IncidentAlertEntity link = new IncidentAlertEntity();
                    link.setIncidentId(finalIncidentId);
                    link.setAlertId(alert.getId());
                    link.setLinkedAt(OffsetDateTime.now());
                    incidentAlertRepository.save(link);
                }
            }
        } catch (Exception e) {
            log.error("Error creating incident from correlated alerts", e);
        }
    }
}
