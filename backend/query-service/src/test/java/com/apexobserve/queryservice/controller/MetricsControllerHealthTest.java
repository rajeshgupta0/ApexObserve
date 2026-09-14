package com.apexobserve.queryservice.controller;

import com.apexobserve.queryservice.repository.MetricRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MetricsControllerHealthTest {

    @Mock
    private MetricRepository metricRepository;

    @Mock
    private com.apexobserve.queryservice.repository.TraceRepository traceRepository;

    private MetricsController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new MetricsController(metricRepository, traceRepository);
    }

    private void mockHealthRow(String serviceId, double latency, double errors, double traffic) {
        Object[] row = new Object[]{serviceId, latency, errors, traffic};
        when(traceRepository.calculateHealthScoreBase(eq("default"), any(OffsetDateTime.class)))
            .thenReturn(List.<Object[]>of(row));
    }

    @Test
    void testHealthyService_highScore() {
        mockHealthRow("svc-a", 100.0, 0.0, 500.0);
        Map<String, Object> result = controller.getHealthScores("default");
        double score = (double) ((Map<?, ?>) result.get("svc-a")).get("score");
        assertTrue(score >= 95.0, "Healthy service (low latency, no errors) should score near 100");
    }

    @Test
    void testHighErrorRate_penalized() {
        // 100% error rate: errors = traffic
        mockHealthRow("svc-b", 100.0, 500.0, 500.0);
        Map<String, Object> result = controller.getHealthScores("default");
        double score = (double) ((Map<?, ?>) result.get("svc-b")).get("score");
        assertTrue(score <= 50.0, "100% error rate should yield score <= 50");
    }

    @Test
    void testHighLatency_penalized() {
        // 5000ms avg latency: max latency penalty
        mockHealthRow("svc-c", 5000.0, 0.0, 100.0);
        Map<String, Object> result = controller.getHealthScores("default");
        double score = (double) ((Map<?, ?>) result.get("svc-c")).get("score");
        assertTrue(score <= 70.0, "Very high latency should reduce score significantly");
    }

    @Test
    void testZeroTraffic_penalized() {
        mockHealthRow("svc-d", 0.0, 0.0, 0.0);
        Map<String, Object> result = controller.getHealthScores("default");
        double score = (double) ((Map<?, ?>) result.get("svc-d")).get("score");
        assertEquals(80.0, score, 0.01, "Zero traffic: only traffic penalty (-20) applies");
    }

    @Test
    void testCombinedDegradation_mixedSignals() {
        // High errors + high latency
        mockHealthRow("svc-e", 3000.0, 200.0, 500.0);
        Map<String, Object> result = controller.getHealthScores("default");
        double score = (double) ((Map<?, ?>) result.get("svc-e")).get("score");
        assertTrue(score >= 0.0 && score <= 100.0, "Score must always be bounded 0-100");
    }

    @Test
    void testScore_neverExceedsBounds() {
        // Edge case: negative-seeming inputs
        mockHealthRow("svc-f", 0.0, 0.0, 1000.0);
        Map<String, Object> result = controller.getHealthScores("default");
        double score = (double) ((Map<?, ?>) result.get("svc-f")).get("score");
        assertEquals(100.0, score, 0.01, "No errors, no high latency, good traffic = 100");
        assertTrue(score <= 100.0 && score >= 0.0);
    }

    @Test
    void testScoreBreakdown_present() {
        mockHealthRow("svc-g", 200.0, 10.0, 100.0);
        Map<String, Object> result = controller.getHealthScores("default");
        Map<?, ?> metrics = (Map<?, ?>) result.get("svc-g");
        assertTrue(metrics.containsKey("scoreBreakdown"), "Response must include scoreBreakdown evidence");
        Map<?, ?> breakdown = (Map<?, ?>) metrics.get("scoreBreakdown");
        assertTrue(breakdown.containsKey("errorPenalty"));
        assertTrue(breakdown.containsKey("latencyPenalty"));
        assertTrue(breakdown.containsKey("trafficPenalty"));
    }
}
