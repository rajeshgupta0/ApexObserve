package com.apexobserve.demo.user;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UserControllerTest {

    private final UserController controller = new UserController();

    @Test
    void testGetUser() {
        String response = controller.getUser("123");
        assertEquals("User 123", response);
    }
}
