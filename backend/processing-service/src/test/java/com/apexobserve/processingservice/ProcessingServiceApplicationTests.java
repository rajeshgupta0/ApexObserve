package com.apexobserve.processingservice;

import com.apexobserve.processingservice.repository.LogRepository;
import com.apexobserve.processingservice.repository.MetricRepository;
import com.apexobserve.processingservice.repository.TraceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ProcessingServiceApplicationTests {

    // Mock Kafka and JPA to avoid needing real infrastructure
    @MockBean @SuppressWarnings("rawtypes") private KafkaTemplate kafkaTemplate;
    @MockBean private LogRepository logRepository;
    @MockBean private MetricRepository metricRepository;
    @MockBean private TraceRepository traceRepository;

    @Test
    void contextLoads() {
        // Verifies Spring context starts successfully with mocked Kafka and persistence layer
    }
}
