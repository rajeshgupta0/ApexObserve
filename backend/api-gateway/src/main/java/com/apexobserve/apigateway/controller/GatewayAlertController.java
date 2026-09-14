package com.apexobserve.apigateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/alerts")
@CrossOrigin(origins = "*")
public class GatewayAlertController {

    private final RestTemplate restTemplate;

    public GatewayAlertController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping
    public ResponseEntity<List> getAlerts(
            @RequestHeader(value = "X-Tenant-ID", defaultValue = "default") String tenantId,
            @RequestParam(required = false) String status) {
        String url = (System.getenv("ALERTING_SERVICE_URL") != null ? System.getenv("ALERTING_SERVICE_URL") : "http://localhost:8084") + "/api/alerts" + (status != null ? "?status=" + status : "");
        ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    @PostMapping("/{alertId}/resolve")
    public ResponseEntity<Object> resolveAlert(
            @RequestHeader(value = "X-Tenant-ID", defaultValue = "default") String tenantId,
            @PathVariable UUID alertId) {
        String url = (System.getenv("ALERTING_SERVICE_URL") != null ? System.getenv("ALERTING_SERVICE_URL") : "http://localhost:8084") + "/api/alerts/" + alertId + "/resolve";
        ResponseEntity<Object> response = restTemplate.postForEntity(url, null, Object.class);
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }
}
