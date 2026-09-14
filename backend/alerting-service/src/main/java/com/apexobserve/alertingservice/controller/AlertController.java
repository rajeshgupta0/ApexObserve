package com.apexobserve.alertingservice.controller;

import com.apexobserve.alertingservice.entity.AlertEntity;
import com.apexobserve.alertingservice.repository.AlertRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {
    
    private final AlertRepository alertRepository;

    public AlertController(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @GetMapping
    public List<AlertEntity> getAlerts(
            @RequestHeader(value = "X-Tenant-ID", defaultValue = "default") String tenantId,
            @RequestParam(required = false) String status) {
        if (status != null && !status.isEmpty()) {
            return alertRepository.findByTenantIdAndStatusOrderByCreatedAtDesc(tenantId, status);
        }
        return alertRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);
    }
    
    @PostMapping("/{alertId}/resolve")
    public ResponseEntity<AlertEntity> resolveAlert(
            @RequestHeader(value = "X-Tenant-ID", defaultValue = "default") String tenantId,
            @PathVariable UUID alertId) {
        return alertRepository.findById(alertId)
                .map(alert -> {
                    alert.setStatus("RESOLVED");
                    alert.setResolvedAt(OffsetDateTime.now());
                    return ResponseEntity.ok(alertRepository.save(alert));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
