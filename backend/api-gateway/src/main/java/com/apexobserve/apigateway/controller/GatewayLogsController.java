package com.apexobserve.apigateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
@CrossOrigin(origins = "*")
public class GatewayLogsController {

    private final RestTemplate restTemplate;

    public GatewayLogsController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/recent")
    public ResponseEntity<List> getRecentLogs(@RequestHeader(value = "X-Tenant-ID", defaultValue = "default") String tenantId) {
        String queryServiceUrl = (System.getenv("QUERY_SERVICE_URL") != null ? System.getenv("QUERY_SERVICE_URL") : "http://localhost:8083") + "/api/logs/recent";
        ResponseEntity<List> response = restTemplate.getForEntity(queryServiceUrl, List.class);
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }
}
