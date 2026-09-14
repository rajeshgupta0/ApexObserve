package com.apexobserve.demo.notification;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NotificationControllerTest {

    private final NotificationController controller = new NotificationController();

    @Test
    void testSend() {
        String response = controller.send();
        assertEquals("SUCCESS", response);
    }
}
