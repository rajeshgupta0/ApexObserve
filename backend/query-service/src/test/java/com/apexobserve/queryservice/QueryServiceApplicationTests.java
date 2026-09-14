package com.apexobserve.queryservice;

import com.apexobserve.queryservice.repository.DependencyRepository;
import com.apexobserve.queryservice.repository.LogRepository;
import com.apexobserve.queryservice.repository.MetricRepository;
import com.apexobserve.queryservice.repository.TraceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class QueryServiceApplicationTests {

    // Mock all JPA repositories to avoid needing a live DataSource in tests
    @MockBean private DependencyRepository dependencyRepository;
    @MockBean private LogRepository logRepository;
    @MockBean private MetricRepository metricRepository;
    @MockBean private TraceRepository traceRepository;
    @MockBean private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Test
    void contextLoads() {
        // Verifies Spring context starts successfully with mocked persistence layer
    }
}
