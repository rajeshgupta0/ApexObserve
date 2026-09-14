package com.apexobserve.demo.apigatewaydemo;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/demo")
public class GatewayController {

    private final RestTemplate restTemplate;

    public GatewayController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping("/checkout")
    public Map<String, Object> checkout(@RequestParam(defaultValue = "false") boolean simulateError) {
        Map<String, Object> response = new HashMap<>();
        try {
            String orderUrl = "http://localhost:9002/api/order/create?simulateError=" + simulateError;
            Map orderResponse = restTemplate.postForObject(orderUrl, null, Map.class);
            response.put("status", "SUCCESS");
            response.put("order", orderResponse);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
        }
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
