package com.apexobserve.apigateway.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class GatewayIncidentControllerTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private GatewayIncidentController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetIncidents() {
        String url = (System.getenv("ALERTING_SERVICE_URL") != null ? System.getenv("ALERTING_SERVICE_URL") : "http://localhost:8084") + "/api/incidents";
        List<Object> mockBody = Collections.singletonList("incident");
        ResponseEntity<List> mockResponse = new ResponseEntity<>(mockBody, HttpStatus.OK);
        when(restTemplate.getForEntity(eq(url), eq(List.class))).thenReturn(mockResponse);

        ResponseEntity<List> response = controller.getIncidents("tenant1", null);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockBody, response.getBody());
    }

    @Test
    void testGetIncidentDetails() {
        UUID id = UUID.randomUUID();
        String url = (System.getenv("ALERTING_SERVICE_URL") != null ? System.getenv("ALERTING_SERVICE_URL") : "http://localhost:8084") + "/api/incidents/" + id;
        Map<String, Object> mockBody = Collections.singletonMap("id", id);
        ResponseEntity<Map> mockResponse = new ResponseEntity<>(mockBody, HttpStatus.OK);
        when(restTemplate.getForEntity(eq(url), eq(Map.class))).thenReturn(mockResponse);

        ResponseEntity<Map> response = controller.getIncidentDetails("tenant1", id);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockBody, response.getBody());
    }
    
    @Test
    void testUpdateStatus() {
        UUID id = UUID.randomUUID();
        String url = (System.getenv("ALERTING_SERVICE_URL") != null ? System.getenv("ALERTING_SERVICE_URL") : "http://localhost:8084") + "/api/incidents/" + id + "/status";
        Map<String, String> requestBody = Collections.singletonMap("status", "RESOLVED");
        ResponseEntity<Object> mockResponse = new ResponseEntity<>("updated", HttpStatus.OK);
        when(restTemplate.postForEntity(eq(url), eq(requestBody), eq(Object.class))).thenReturn(mockResponse);

        ResponseEntity<Object> response = controller.updateStatus("tenant1", id, requestBody);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("updated", response.getBody());
    }
}
