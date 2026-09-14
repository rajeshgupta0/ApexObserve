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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class GatewayAlertControllerTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private GatewayAlertController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAlerts() {
        String url = (System.getenv("ALERTING_SERVICE_URL") != null ? System.getenv("ALERTING_SERVICE_URL") : "http://localhost:8084") + "/api/alerts";
        List<Object> mockBody = Collections.singletonList("mock-alert");
        ResponseEntity<List> mockResponse = new ResponseEntity<>(mockBody, HttpStatus.OK);
        when(restTemplate.getForEntity(eq(url), eq(List.class))).thenReturn(mockResponse);

        ResponseEntity<List> response = controller.getAlerts("tenant1", null);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockBody, response.getBody());
    }
    
    @Test
    void testGetAlertsWithStatus() {
        String url = (System.getenv("ALERTING_SERVICE_URL") != null ? System.getenv("ALERTING_SERVICE_URL") : "http://localhost:8084") + "/api/alerts?status=ACTIVE";
        List<Object> mockBody = Collections.singletonList("mock-alert");
        ResponseEntity<List> mockResponse = new ResponseEntity<>(mockBody, HttpStatus.OK);
        when(restTemplate.getForEntity(eq(url), eq(List.class))).thenReturn(mockResponse);

        ResponseEntity<List> response = controller.getAlerts("tenant1", "ACTIVE");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockBody, response.getBody());
    }

    @Test
    void testResolveAlert() {
        UUID alertId = UUID.randomUUID();
        String url = (System.getenv("ALERTING_SERVICE_URL") != null ? System.getenv("ALERTING_SERVICE_URL") : "http://localhost:8084") + "/api/alerts/" + alertId + "/resolve";
        ResponseEntity<Object> mockResponse = new ResponseEntity<>("resolved", HttpStatus.OK);
        when(restTemplate.postForEntity(eq(url), eq(null), eq(Object.class))).thenReturn(mockResponse);

        ResponseEntity<Object> response = controller.resolveAlert("tenant1", alertId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("resolved", response.getBody());
    }
}
