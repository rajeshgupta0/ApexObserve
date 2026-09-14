package com.apexobserve.alertingservice.service;

import com.apexobserve.alertingservice.entity.IncidentEntity;
import com.apexobserve.alertingservice.repository.IncidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IncidentServiceTest {

    @Mock
    private IncidentRepository incidentRepository;

    private IncidentService incidentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        incidentService = new IncidentService(incidentRepository);
    }

    private IncidentEntity buildIncident(String status) {
        IncidentEntity i = new IncidentEntity();
        i.setId(UUID.randomUUID());
        i.setTenantId("default");
        i.setStatus(status);
        i.setCreatedAt(OffsetDateTime.now());
        i.setUpdatedAt(OffsetDateTime.now());
        return i;
    }

    @Test
    void testTransition_openToAcknowledged_valid() {
        IncidentEntity incident = buildIncident("OPEN");
        when(incidentRepository.findById(incident.getId())).thenReturn(Optional.of(incident));
        when(incidentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        IncidentEntity result = incidentService.updateIncidentStatus("default", incident.getId(), "ACKNOWLEDGED");
        assertEquals("ACKNOWLEDGED", result.getStatus());
    }

    @Test
    void testTransition_resolvedToReopened_withinWindow_valid() {
        IncidentEntity incident = buildIncident("RESOLVED");
        // updatedAt just now, well within 24h window
        incident.setUpdatedAt(OffsetDateTime.now().minusMinutes(10));
        when(incidentRepository.findById(incident.getId())).thenReturn(Optional.of(incident));
        when(incidentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        IncidentEntity result = incidentService.updateIncidentStatus("default", incident.getId(), "REOPENED");
        assertEquals("REOPENED", result.getStatus());
    }

    @Test
    void testTransition_resolvedToReopened_afterWindow_throws() {
        IncidentEntity incident = buildIncident("RESOLVED");
        // updatedAt 25h ago, past the 24h reopen window
        incident.setUpdatedAt(OffsetDateTime.now().minusHours(25));
        when(incidentRepository.findById(incident.getId())).thenReturn(Optional.of(incident));

        assertThrows(IllegalStateException.class, () ->
            incidentService.updateIncidentStatus("default", incident.getId(), "REOPENED")
        );
    }

    @Test
    void testTransition_resolvedToOther_throws() {
        IncidentEntity incident = buildIncident("RESOLVED");
        when(incidentRepository.findById(incident.getId())).thenReturn(Optional.of(incident));

        assertThrows(IllegalStateException.class, () ->
            incidentService.updateIncidentStatus("default", incident.getId(), "INVESTIGATING")
        );
    }

    @Test
    void testGetIncident_notFound_throws() {
        UUID id = UUID.randomUUID();
        when(incidentRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
            incidentService.getIncident("default", id)
        );
    }

    @Test
    void testGetIncident_wrongTenant_throws() {
        IncidentEntity incident = buildIncident("OPEN");
        when(incidentRepository.findById(incident.getId())).thenReturn(Optional.of(incident));

        // Querying with a different tenantId
        assertThrows(RuntimeException.class, () ->
            incidentService.getIncident("other-tenant", incident.getId())
        );
    }
}
