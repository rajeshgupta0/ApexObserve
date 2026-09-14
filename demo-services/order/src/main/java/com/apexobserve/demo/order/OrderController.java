package com.apexobserve.demo.order;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/order")
public class OrderController {
    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    private final RestTemplate restTemplate;

    public OrderController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping("/create")
    public Map<String, Object> createOrder(@RequestParam(defaultValue = "false") boolean simulateError) {
        log.info("Creating order...");
        Map<String, Object> response = new HashMap<>();
        
        // 1. Check Inventory
        String inventoryUrl = "http://localhost:9004/api/inventory/deduct?simulateError=" + simulateError;
        restTemplate.postForObject(inventoryUrl, null, String.class);
        
        // 2. Process Payment
        String paymentUrl = "http://localhost:9003/api/payment/process?simulateError=" + simulateError;
        restTemplate.postForObject(paymentUrl, null, String.class);
        
        // 3. Send Notification
        String notifUrl = "http://localhost:9005/api/notification/send";
        restTemplate.postForObject(notifUrl, null, String.class);

        response.put("orderId", java.util.UUID.randomUUID().toString());
        response.put("status", "CREATED");
        log.info("Order created successfully");
        return response;
    }
}

@Configuration
class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
