package com.apexobserve.queryservice.controller;

import com.apexobserve.queryservice.service.RcaService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {
    private final RcaService rcaService;

    public AnalysisController(RcaService rcaService) {
        this.rcaService = rcaService;
    }

    @GetMapping("/rca")
    public Map<String, Object> getRca(
            @RequestHeader(value = "X-Tenant-ID", defaultValue = "default") String tenantId,
            @RequestParam String serviceId) {
        return rcaService.analyzeRootCause(tenantId, serviceId);
    }
    
    @GetMapping("/blast-radius")
    public Map<String, Object> getBlastRadius(
            @RequestHeader(value = "X-Tenant-ID", defaultValue = "default") String tenantId,
            @RequestParam String serviceId) {
        return rcaService.analyzeBlastRadius(tenantId, serviceId);
    }
}
