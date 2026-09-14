package com.apexobserve.alertingservice.controller;

import com.apexobserve.alertingservice.entity.IncidentEntity;
import com.apexobserve.alertingservice.service.IncidentService;
import com.apexobserve.alertingservice.service.RcaEngine;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/incidents/{incidentId}/rca")
public class RcaController {
    private final RcaEngine rcaEngine;
    private final IncidentService incidentService;

    public RcaController(RcaEngine rcaEngine, IncidentService incidentService) {
        this.rcaEngine = rcaEngine;
        this.incidentService = incidentService;
    }

    @GetMapping
    public ResponseEntity<RcaEngine.RcaResult> getRca(
            @PathVariable UUID incidentId,
            @RequestHeader(value = "X-Tenant-ID", defaultValue = "default") String tenantId) {
        IncidentEntity incident = incidentService.getIncident(tenantId, incidentId);
        RcaEngine.RcaResult result = rcaEngine.performRootCauseAnalysis(tenantId, incident);
        return ResponseEntity.ok(result);
    }
}
