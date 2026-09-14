package com.apexobserve.apigateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/dependencies")
@CrossOrigin(origins = "${FRONTEND_URL:http://localhost:3000}")
public class GatewayDependencyController {

    private final RestTemplate restTemplate;

    public GatewayDependencyController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping({"", "/graph"})
    public ResponseEntity<Map> getDependencyGraph(@RequestHeader(value = "X-Tenant-ID", defaultValue = "default") String tenantId) {
        String queryServiceUrl = (System.getenv("QUERY_SERVICE_URL") != null ? System.getenv("QUERY_SERVICE_URL") : "http://localhost:8083") + "/api/dependencies/graph";
        ResponseEntity<Map> response = restTemplate.getForEntity(queryServiceUrl, Map.class);
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }
}
