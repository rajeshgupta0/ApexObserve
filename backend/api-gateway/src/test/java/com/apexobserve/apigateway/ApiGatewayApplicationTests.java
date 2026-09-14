package com.apexobserve.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApiGatewayApplicationTests {

    @Test
    void contextLoads() {
        // Ponytail: The smallest meaningful test for a proxy is that its context loads
        // and the application can start without failing on missing beans.
    }
}
