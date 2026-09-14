package com.apexobserve.alertingservice.controller;

import com.apexobserve.alertingservice.entity.EvidenceEntity;
import com.apexobserve.alertingservice.service.EvidenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/incidents/{incidentId}/evidence")
public class EvidenceController {
    private final EvidenceService evidenceService;

    public EvidenceController(EvidenceService evidenceService) {
        this.evidenceService = evidenceService;
    }

    @GetMapping
    public ResponseEntity<List<EvidenceEntity>> getEvidenceForIncident(
            @PathVariable UUID incidentId,
            @RequestHeader(value = "X-Tenant-ID", defaultValue = "default") String tenantId) {
        
        List<EvidenceEntity> evidence = evidenceService.getEvidenceForIncident(tenantId, incidentId);
        return ResponseEntity.ok(evidence);
    }
}
