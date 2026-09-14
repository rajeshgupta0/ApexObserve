package com.apexobserve.demo.inventory;

import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    private static final Logger log = LoggerFactory.getLogger(InventoryController.class);

    @PostMapping("/deduct")
    public String deduct(@RequestParam(defaultValue = "false") boolean simulateError) {
        log.info("Deducting inventory...");
        if (simulateError) {
            log.error("Inventory deduction failed - simulated error");
            throw new RuntimeException("Simulated inventory failure");
        }
        return "SUCCESS";
    }
}
