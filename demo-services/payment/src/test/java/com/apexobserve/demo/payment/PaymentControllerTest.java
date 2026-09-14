package com.apexobserve.demo.payment;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentControllerTest {

    private final PaymentController controller = new PaymentController();

    @Test
    void testProcess_Success() {
        String response = controller.process(false);
        assertEquals("SUCCESS", response);
    }

    @Test
    void testProcess_Error() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> controller.process(true));
        assertEquals("Simulated payment failure", exception.getMessage());
    }
}
