package com.apexobserve.alertingservice.service;

import com.apexobserve.alertingservice.entity.AlertEntity;
import com.apexobserve.alertingservice.repository.AlertRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AlertEvaluationEngine {
    private static final Logger log = LoggerFactory.getLogger(AlertEvaluationEngine.class);
    private final AlertRepository alertRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public AlertEvaluationEngine(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @Scheduled(fixedRate = 60000)
    public void evaluateAlerts() {
        log.info("Evaluating alert rules...");
        try {
            List<Map<String, Object>> anomalies = restTemplate.exchange(
                (System.getenv("QUERY_SERVICE_URL") != null ? System.getenv("QUERY_SERVICE_URL") : "http://localhost:8083") + "/api/metrics/internal/anomalies",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            ).getBody();
            
            if (anomalies != null) {
                for (Map<String, Object> anomaly : anomalies) {
                    String serviceId = (String) anomaly.get("service_id");
                    String metricName = (String) anomaly.get("metric_name");
                    
                    // Check if an active alert already exists
                    boolean exists = alertRepository.findByTenantIdAndStatusOrderByCreatedAtDesc("default", "ACTIVE")
                        .stream().anyMatch(a -> a.getServiceId().equals(serviceId) && a.getRuleName().contains(metricName));
                        
                    if (!exists) {
                        AlertEntity alert = new AlertEntity();
                        alert.setId(UUID.randomUUID());
                        alert.setTenantId("default");
                        alert.setServiceId(serviceId);
                        alert.setRuleName("High " + metricName);
                        alert.setSeverity("CRITICAL");
                        alert.setStatus("ACTIVE");
                        alert.setCreatedAt(OffsetDateTime.now());
                        alert.setContext("{\"value\": " + anomaly.get("avg_val") + "}");
                        alertRepository.save(alert);
                        log.warn("Created alert for {} due to {}", serviceId, metricName);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to evaluate alerts", e);
        }
    }
}
