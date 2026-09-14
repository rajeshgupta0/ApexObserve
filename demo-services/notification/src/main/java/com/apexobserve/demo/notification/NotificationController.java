package com.apexobserve.demo.notification;

import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {
    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    @PostMapping("/send")
    public String send() {
        log.info("Sending notification...");
        return "SUCCESS";
    }
}
