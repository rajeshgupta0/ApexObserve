package com.apexobserve.demo.payment;

import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {
    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    @PostMapping("/process")
    public String process(@RequestParam(defaultValue = "false") boolean simulateError) {
        log.info("Processing payment...");
        try {
            Thread.sleep(100); // Simulate latency
        } catch (InterruptedException e) {}
        
        if (simulateError) {
            log.error("Payment processing failed - simulated error");
            throw new RuntimeException("Simulated payment failure");
        }
        return "SUCCESS";
    }
}
