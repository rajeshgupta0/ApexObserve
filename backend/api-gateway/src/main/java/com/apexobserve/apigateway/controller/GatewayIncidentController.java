package com.apexobserve.apigateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/incidents")
@CrossOrigin(origins = "${FRONTEND_URL:http://localhost:3000}")
public class GatewayIncidentController {

    private final RestTemplate restTemplate;

    public GatewayIncidentController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping
    public ResponseEntity<List> getIncidents(
            @RequestHeader(value = "X-Tenant-ID", defaultValue = "default") String tenantId,
            @RequestParam(required = false) String status) {
        String url = (System.getenv("ALERTING_SERVICE_URL") != null ? System.getenv("ALERTING_SERVICE_URL") : "http://localhost:8084") + "/api/incidents" + (status != null ? "?status=" + status : "");
        ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map> getIncidentDetails(
            @RequestHeader(value = "X-Tenant-ID", defaultValue = "default") String tenantId,
            @PathVariable UUID id) {
        String url = (System.getenv("ALERTING_SERVICE_URL") != null ? System.getenv("ALERTING_SERVICE_URL") : "http://localhost:8084") + "/api/incidents/" + id;
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<Object> updateStatus(
            @RequestHeader(value = "X-Tenant-ID", defaultValue = "default") String tenantId,
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        String url = (System.getenv("ALERTING_SERVICE_URL") != null ? System.getenv("ALERTING_SERVICE_URL") : "http://localhost:8084") + "/api/incidents/" + id + "/status";
        ResponseEntity<Object> response = restTemplate.postForEntity(url, body, Object.class);
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }
}
