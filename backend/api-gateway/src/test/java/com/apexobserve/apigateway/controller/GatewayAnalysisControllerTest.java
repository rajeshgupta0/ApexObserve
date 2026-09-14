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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class GatewayAnalysisControllerTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private GatewayAnalysisController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetRca() {
        String serviceId = "service-a";
        String url = (System.getenv("QUERY_SERVICE_URL") != null ? System.getenv("QUERY_SERVICE_URL") : "http://localhost:8083") + "/api/analysis/rca?serviceId=" + serviceId;
        Map<String, Object> mockBody = Collections.singletonMap("rca", "mock-rca");
        ResponseEntity<Map> mockResponse = new ResponseEntity<>(mockBody, HttpStatus.OK);
        when(restTemplate.getForEntity(eq(url), eq(Map.class))).thenReturn(mockResponse);

        ResponseEntity<Map> response = controller.getRca("tenant1", serviceId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockBody, response.getBody());
    }

    @Test
    void testGetBlastRadius() {
        String serviceId = "service-b";
        String url = (System.getenv("QUERY_SERVICE_URL") != null ? System.getenv("QUERY_SERVICE_URL") : "http://localhost:8083") + "/api/analysis/blast-radius?serviceId=" + serviceId;
        Map<String, Object> mockBody = Collections.singletonMap("blast-radius", "mock-br");
        ResponseEntity<Map> mockResponse = new ResponseEntity<>(mockBody, HttpStatus.OK);
        when(restTemplate.getForEntity(eq(url), eq(Map.class))).thenReturn(mockResponse);

        ResponseEntity<Map> response = controller.getBlastRadius("tenant1", serviceId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockBody, response.getBody());
    }
}
