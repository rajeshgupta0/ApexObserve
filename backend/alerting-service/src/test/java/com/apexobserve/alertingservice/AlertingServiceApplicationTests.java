package com.apexobserve.alertingservice;

import com.apexobserve.alertingservice.repository.AlertRepository;
import com.apexobserve.alertingservice.repository.EvidenceRepository;
import com.apexobserve.alertingservice.repository.IncidentAlertRepository;
import com.apexobserve.alertingservice.repository.IncidentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AlertingServiceApplicationTests {

    // Mock all JPA repositories to avoid needing a live DataSource
    @MockBean private AlertRepository alertRepository;
    @MockBean private IncidentRepository incidentRepository;
    @MockBean private IncidentAlertRepository incidentAlertRepository;
    @MockBean private EvidenceRepository evidenceRepository;

    @Test
    void contextLoads() {
        // Verifies Spring context starts successfully with mocked persistence layer
    }
}
