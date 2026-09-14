package com.apexobserve.demo.inventory;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InventoryControllerTest {

    private final InventoryController controller = new InventoryController();

    @Test
    void testDeduct_Success() {
        String response = controller.deduct(false);
        assertEquals("SUCCESS", response);
    }

    @Test
    void testDeduct_Error() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> controller.deduct(true));
        assertEquals("Simulated inventory failure", exception.getMessage());
    }
}
