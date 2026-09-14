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

class GatewayControllerTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private GatewayController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetHealthScores() {
        String url = (System.getenv("QUERY_SERVICE_URL") != null ? System.getenv("QUERY_SERVICE_URL") : "http://localhost:8083") + "/api/metrics/health";
        Map<String, Object> mockBody = Collections.singletonMap("health", "good");
        ResponseEntity<Map> mockResponse = new ResponseEntity<>(mockBody, HttpStatus.OK);
        when(restTemplate.getForEntity(eq(url), eq(Map.class))).thenReturn(mockResponse);

        ResponseEntity<Map> response = controller.getHealthScores("tenant1");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockBody, response.getBody());
    }

    @Test
    void testQueryMetrics() {
        String url = (System.getenv("QUERY_SERVICE_URL") != null ? System.getenv("QUERY_SERVICE_URL") : "http://localhost:8083") + "/api/metrics/query?serviceId=srvA&metricName=cpu&startTime=t1&endTime=t2";
        Object mockBody = "mockData";
        ResponseEntity<Object> mockResponse = new ResponseEntity<>(mockBody, HttpStatus.OK);
        when(restTemplate.getForEntity(eq(url), eq(Object.class))).thenReturn(mockResponse);

        ResponseEntity<Object> response = controller.queryMetrics("tenant1", "srvA", "cpu", "t1", "t2");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockBody, response.getBody());
    }
}
