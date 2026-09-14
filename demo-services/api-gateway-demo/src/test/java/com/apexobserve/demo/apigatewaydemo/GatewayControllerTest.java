package com.apexobserve.demo.apigatewaydemo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
    void testCheckout_Success() {
        String url = "http://localhost:9002/api/order/create?simulateError=false";
        Map<String, Object> mockOrderResponse = Collections.singletonMap("orderId", "123");
        when(restTemplate.postForObject(eq(url), eq(null), eq(Map.class))).thenReturn(mockOrderResponse);

        Map<String, Object> response = controller.checkout(false);
        assertEquals("SUCCESS", response.get("status"));
        assertEquals(mockOrderResponse, response.get("order"));
    }

    @Test
    void testCheckout_Error() {
        String url = "http://localhost:9002/api/order/create?simulateError=true";
        when(restTemplate.postForObject(eq(url), eq(null), eq(Map.class))).thenThrow(new RuntimeException("Simulated error"));

        Map<String, Object> response = controller.checkout(true);
        assertEquals("ERROR", response.get("status"));
        assertEquals("Simulated error", response.get("message"));
    }
}
