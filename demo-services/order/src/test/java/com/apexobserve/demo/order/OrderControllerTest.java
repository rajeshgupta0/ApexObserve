package com.apexobserve.demo.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class OrderControllerTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private OrderController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateOrder() {
        boolean simulateError = false;
        String inventoryUrl = "http://localhost:9004/api/inventory/deduct?simulateError=" + simulateError;
        String paymentUrl = "http://localhost:9003/api/payment/process?simulateError=" + simulateError;
        String notifUrl = "http://localhost:9005/api/notification/send";

        when(restTemplate.postForObject(eq(inventoryUrl), eq(null), eq(String.class))).thenReturn("SUCCESS");
        when(restTemplate.postForObject(eq(paymentUrl), eq(null), eq(String.class))).thenReturn("SUCCESS");
        when(restTemplate.postForObject(eq(notifUrl), eq(null), eq(String.class))).thenReturn("SUCCESS");

        Map<String, Object> response = controller.createOrder(simulateError);
        assertNotNull(response.get("orderId"));
        assertEquals("CREATED", response.get("status"));
    }
}
