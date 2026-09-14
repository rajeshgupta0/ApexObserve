package com.apexobserve.apigateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Allows Next.js frontend to access
public class GatewayController {

    private final RestTemplate restTemplate;

    public GatewayController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/metrics/health")
    public ResponseEntity<Map> getHealthScores(@RequestHeader(value = "X-Tenant-ID", defaultValue = "default") String tenantId) {
        String queryServiceUrl = (System.getenv("QUERY_SERVICE_URL") != null ? System.getenv("QUERY_SERVICE_URL") : "http://localhost:8083") + "/api/metrics/health";
        // Forward headers manually if needed, or just let it pass
        ResponseEntity<Map> response = restTemplate.getForEntity(queryServiceUrl, Map.class);
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    @GetMapping("/metrics/query")
    public ResponseEntity<Object> queryMetrics(
            @RequestHeader(value = "X-Tenant-ID", defaultValue = "default") String tenantId,
            @RequestParam String serviceId,
            @RequestParam String metricName,
            @RequestParam String startTime,
            @RequestParam String endTime) {
            
        String queryServiceUrl = String.format((System.getenv("QUERY_SERVICE_URL") != null ? System.getenv("QUERY_SERVICE_URL") : "http://localhost:8083") + "/api/metrics/query?serviceId=%s&metricName=%s&startTime=%s&endTime=%s", 
                serviceId, metricName, startTime, endTime);
                
        ResponseEntity<Object> response = restTemplate.getForEntity(queryServiceUrl, Object.class);
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }
}
