package com.apexobserve.alertingservice.controller;

import com.apexobserve.alertingservice.entity.AlertEntity;
import com.apexobserve.alertingservice.entity.IncidentAlertEntity;
import com.apexobserve.alertingservice.entity.IncidentEntity;
import com.apexobserve.alertingservice.repository.AlertRepository;
import com.apexobserve.alertingservice.repository.IncidentAlertRepository;
import com.apexobserve.alertingservice.service.IncidentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {
    private final IncidentService incidentService;
    private final IncidentAlertRepository incidentAlertRepository;
    private final AlertRepository alertRepository;

    public IncidentController(IncidentService incidentService,
                              IncidentAlertRepository incidentAlertRepository,
                              AlertRepository alertRepository) {
        this.incidentService = incidentService;
        this.incidentAlertRepository = incidentAlertRepository;
        this.alertRepository = alertRepository;
    }

    @GetMapping
    public ResponseEntity<List<IncidentEntity>> getAllIncidents(
            @RequestHeader(value = "X-Tenant-ID", defaultValue = "default") String tenantId) {
        return ResponseEntity.ok(incidentService.getAllIncidents(tenantId));
    }

    @GetMapping("/{incidentId}")
    public ResponseEntity<Map<String, Object>> getIncident(
            @PathVariable UUID incidentId,
            @RequestHeader(value = "X-Tenant-ID", defaultValue = "default") String tenantId) {
        IncidentEntity incident = incidentService.getIncident(tenantId, incidentId);
        List<IncidentAlertEntity> links = incidentAlertRepository.findByIncidentId(incidentId);
        List<UUID> alertIds = links.stream().map(IncidentAlertEntity::getAlertId).collect(Collectors.toList());
        List<AlertEntity> alerts = alertIds.isEmpty() ? Collections.emptyList() : alertRepository.findAllById(alertIds);

        Map<String, Object> response = new HashMap<>();
        response.put("incident", incident);
        response.put("alerts", alerts);
        response.put("id", incident.getId());
        response.put("title", incident.getTitle());
        response.put("status", incident.getStatus());
        response.put("severity", incident.getSeverity());
        response.put("createdAt", incident.getCreatedAt());
        response.put("updatedAt", incident.getUpdatedAt());
        response.put("affectedServices", incident.getAffectedServices());
        return ResponseEntity.ok(response);
    }

    @RequestMapping(value = "/{incidentId}/status", method = {RequestMethod.POST, RequestMethod.PUT})
    public ResponseEntity<IncidentEntity> updateIncidentStatus(
            @PathVariable UUID incidentId,
            @RequestBody Map<String, String> payload,
            @RequestHeader(value = "X-Tenant-ID", defaultValue = "default") String tenantId) {
        
        String newStatus = payload.get("status");
        if (newStatus == null) {
            return ResponseEntity.badRequest().build();
        }
        
        return ResponseEntity.ok(incidentService.updateIncidentStatus(tenantId, incidentId, newStatus));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalStateException(IllegalStateException ex) {
        return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("not found")) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        }
        return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", ex.getMessage()));
    }
}
