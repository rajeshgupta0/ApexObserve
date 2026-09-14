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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class GatewayTracesControllerTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private GatewayTracesController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetRecentTraces() {
        String url = (System.getenv("QUERY_SERVICE_URL") != null ? System.getenv("QUERY_SERVICE_URL") : "http://localhost:8083") + "/api/traces/recent";
        List<Object> mockBody = Collections.singletonList("trace-entry");
        ResponseEntity<List> mockResponse = new ResponseEntity<>(mockBody, HttpStatus.OK);
        when(restTemplate.getForEntity(eq(url), eq(List.class))).thenReturn(mockResponse);

        ResponseEntity<List> response = controller.getRecentTraces("tenant1");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockBody, response.getBody());
    }

    @Test
    void testGetTraceSpans() {
        String traceId = "trace-123";
        String url = (System.getenv("QUERY_SERVICE_URL") != null ? System.getenv("QUERY_SERVICE_URL") : "http://localhost:8083") + "/api/traces/" + traceId;
        List<Object> mockBody = Collections.singletonList("span-entry");
        ResponseEntity<List> mockResponse = new ResponseEntity<>(mockBody, HttpStatus.OK);
        when(restTemplate.getForEntity(eq(url), eq(List.class))).thenReturn(mockResponse);

        ResponseEntity<List> response = controller.getTraceSpans("tenant1", traceId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockBody, response.getBody());
    }
}
