package com.apexobserve.alertingservice.service;

import com.apexobserve.alertingservice.entity.AlertEntity;
import com.apexobserve.alertingservice.entity.IncidentEntity;
import com.apexobserve.alertingservice.entity.IncidentAlertEntity;
import com.apexobserve.alertingservice.repository.AlertRepository;
import com.apexobserve.alertingservice.repository.IncidentRepository;
import com.apexobserve.alertingservice.repository.IncidentAlertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AlertCorrelationEngineTest {

    @Mock
    private AlertRepository alertRepository;
    @Mock
    private IncidentRepository incidentRepository;
    @Mock
    private IncidentAlertRepository incidentAlertRepository;
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AlertCorrelationEngine engine;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(engine, "restTemplate", restTemplate);
    }

    @Test
    void testCorrelateAlerts_Related() {
        AlertEntity alert1 = new AlertEntity();
        alert1.setId(UUID.randomUUID());
        alert1.setServiceId("service-a");
        alert1.setCreatedAt(OffsetDateTime.now());

        AlertEntity alert2 = new AlertEntity();
        alert2.setId(UUID.randomUUID());
        alert2.setServiceId("service-b");
        alert2.setCreatedAt(OffsetDateTime.now());

        when(alertRepository.findByTenantIdAndStatusOrderByCreatedAtDesc("default", "ACTIVE"))
                .thenReturn(Arrays.asList(alert1, alert2));

        Map<String, Object> graph = new HashMap<>();
        List<Map<String, Object>> edges = new ArrayList<>();
        Map<String, Object> edge = new HashMap<>();
        edge.put("source", "service-a");
        edge.put("target", "service-b");
        edges.add(edge);
        graph.put("edges", edges);

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(graph);
        when(incidentRepository.save(any(IncidentEntity.class))).thenAnswer(i -> i.getArgument(0));

        engine.correlateAlerts();

        verify(incidentRepository, times(1)).save(any(IncidentEntity.class));
        verify(incidentAlertRepository, times(2)).save(any(IncidentAlertEntity.class));
    }

    @Test
    void testCorrelateAlerts_Unrelated() {
        AlertEntity alert1 = new AlertEntity();
        alert1.setId(UUID.randomUUID());
        alert1.setServiceId("service-a");
        alert1.setCreatedAt(OffsetDateTime.now());

        AlertEntity alert2 = new AlertEntity();
        alert2.setId(UUID.randomUUID());
        alert2.setServiceId("service-c");
        alert2.setCreatedAt(OffsetDateTime.now());

        when(alertRepository.findByTenantIdAndStatusOrderByCreatedAtDesc("default", "ACTIVE"))
                .thenReturn(Arrays.asList(alert1, alert2));

        Map<String, Object> graph = new HashMap<>();
        List<Map<String, Object>> edges = new ArrayList<>();
        Map<String, Object> edge = new HashMap<>();
        edge.put("source", "service-a");
        edge.put("target", "service-b");
        edges.add(edge);
        graph.put("edges", edges);

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(graph);

        engine.correlateAlerts();

        verify(incidentRepository, never()).save(any(IncidentEntity.class));
    }
}
