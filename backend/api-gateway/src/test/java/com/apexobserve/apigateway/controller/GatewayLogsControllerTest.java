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

class GatewayLogsControllerTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private GatewayLogsController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetRecentLogs() {
        String url = (System.getenv("QUERY_SERVICE_URL") != null ? System.getenv("QUERY_SERVICE_URL") : "http://localhost:8083") + "/api/logs/recent";
        List<Object> mockBody = Collections.singletonList("log-entry");
        ResponseEntity<List> mockResponse = new ResponseEntity<>(mockBody, HttpStatus.OK);
        when(restTemplate.getForEntity(eq(url), eq(List.class))).thenReturn(mockResponse);

        ResponseEntity<List> response = controller.getRecentLogs("tenant1");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockBody, response.getBody());
    }
}
