package com.apexobserve.alertingservice.service;

import com.apexobserve.alertingservice.entity.EvidenceEntity;
import com.apexobserve.alertingservice.entity.IncidentEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RcaEngineTest {

    @Mock
    private EvidenceService evidenceService;

    private RcaEngine rcaEngine;

    private IncidentEntity incident;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        rcaEngine = new RcaEngine(evidenceService);
        incident = new IncidentEntity();
        incident.setId(UUID.randomUUID());
        incident.setTenantId("default");
    }

    @Test
    void testNoEvidence_returnsLowConfidence() {
        when(evidenceService.getEvidenceForIncident("default", incident.getId()))
            .thenReturn(Collections.emptyList());
        RcaEngine.RcaResult result = rcaEngine.performRootCauseAnalysis("default", incident);
        assertTrue(result.getConfidence() <= 0.2, "No evidence should yield very low confidence");
        assertEquals("Unknown", result.getRootCauseHypothesis());
    }

    @Test
    void testStrongEvidence_highConfidence() {
        EvidenceEntity evidence = new EvidenceEntity();
        evidence.setId(UUID.randomUUID());
        evidence.setEvidenceType("ANOMALY");
        evidence.setDescription("CPU spike detected at Service: order-service");
        evidence.setRelevanceScore(85.0);
        evidence.setCreatedAt(java.time.OffsetDateTime.now());

        when(evidenceService.getEvidenceForIncident("default", incident.getId()))
            .thenReturn(List.of(evidence));

        RcaEngine.RcaResult result = rcaEngine.performRootCauseAnalysis("default", incident);
        assertTrue(result.getConfidence() >= 0.70, "Strong evidence should yield >= 0.70 confidence");
        assertNotNull(result.getRootCauseHypothesis());
    }

    @Test
    void testMultipleEvidenceItems_usesHighestRelevance() {
        EvidenceEntity low = new EvidenceEntity();
        low.setId(UUID.randomUUID());
        low.setEvidenceType("LOG");
        low.setDescription("Minor warning log");
        low.setRelevanceScore(20.0);
        low.setCreatedAt(java.time.OffsetDateTime.now());

        EvidenceEntity high = new EvidenceEntity();
        high.setId(UUID.randomUUID());
        high.setEvidenceType("TRACE");
        high.setDescription("Timeout trace in payment-service");
        high.setRelevanceScore(90.0);
        high.setCreatedAt(java.time.OffsetDateTime.now());

        when(evidenceService.getEvidenceForIncident("default", incident.getId()))
            .thenReturn(List.of(low, high));

        RcaEngine.RcaResult result = rcaEngine.performRootCauseAnalysis("default", incident);
        // Should use the TRACE evidence (relevance 90) as root
        assertTrue(result.getRootCauseHypothesis().contains("TRACE"));
        assertTrue(result.getConfidence() >= 0.70);
    }

    @Test
    void testWeakEvidence_belowThreshold_mediumConfidence() {
        EvidenceEntity weak = new EvidenceEntity();
        weak.setId(UUID.randomUUID());
        weak.setEvidenceType("METRIC");
        weak.setDescription("Slight CPU increase");
        weak.setRelevanceScore(30.0); // Below 50 threshold
        weak.setCreatedAt(java.time.OffsetDateTime.now());

        when(evidenceService.getEvidenceForIncident("default", incident.getId()))
            .thenReturn(List.of(weak));

        RcaEngine.RcaResult result = rcaEngine.performRootCauseAnalysis("default", incident);
        assertTrue(result.getConfidence() >= 0.3 && result.getConfidence() < 0.70,
            "Weak evidence below threshold should be medium-low confidence");
    }

    @Test
    void testContributingSignals_notEmpty() {
        EvidenceEntity ev = new EvidenceEntity();
        ev.setId(UUID.randomUUID());
        ev.setEvidenceType("DEPENDENCY");
        ev.setDescription("Dependency failure detected");
        ev.setRelevanceScore(75.0);
        ev.setCreatedAt(java.time.OffsetDateTime.now());

        when(evidenceService.getEvidenceForIncident("default", incident.getId()))
            .thenReturn(List.of(ev));

        RcaEngine.RcaResult result = rcaEngine.performRootCauseAnalysis("default", incident);
        assertFalse(result.getContributingSignals().isEmpty(), "Contributing signals must be populated");
    }
}
