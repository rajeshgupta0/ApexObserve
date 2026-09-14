package com.apexobserve.ingestionservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;

@SpringBootTest
@ActiveProfiles("test")
class IngestionServiceApplicationTests {

    // Provide a mock KafkaTemplate so the context can wire services without a real Kafka broker
    @MockBean
    @SuppressWarnings("rawtypes")
    private KafkaTemplate kafkaTemplate;

    @Test
    void contextLoads() {
        // Verifies Spring context starts successfully with mocked Kafka infrastructure
    }
}
