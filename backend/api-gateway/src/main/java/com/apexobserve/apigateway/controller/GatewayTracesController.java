package com.apexobserve.apigateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("/api/traces")
@CrossOrigin(origins = "${FRONTEND_URL:http://localhost:3000}")
public class GatewayTracesController {

    private final RestTemplate restTemplate;

    public GatewayTracesController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/recent")
    public ResponseEntity<List> getRecentTraces(@RequestHeader(value = "X-Tenant-ID", defaultValue = "default") String tenantId) {
        String queryServiceUrl = (System.getenv("QUERY_SERVICE_URL") != null ? System.getenv("QUERY_SERVICE_URL") : "http://localhost:8083") + "/api/traces/recent";
        ResponseEntity<List> response = restTemplate.getForEntity(queryServiceUrl, List.class);
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }
    
    @GetMapping("/{traceId}")
    public ResponseEntity<List> getTraceSpans(@RequestHeader(value = "X-Tenant-ID", defaultValue = "default") String tenantId, @PathVariable String traceId) {
        String queryServiceUrl = (System.getenv("QUERY_SERVICE_URL") != null ? System.getenv("QUERY_SERVICE_URL") : "http://localhost:8083") + "/api/traces/" + traceId;
        ResponseEntity<List> response = restTemplate.getForEntity(queryServiceUrl, List.class);
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }
}
