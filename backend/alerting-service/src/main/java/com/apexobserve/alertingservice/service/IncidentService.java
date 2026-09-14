package com.apexobserve.alertingservice.service;

import com.apexobserve.alertingservice.entity.IncidentEntity;
import com.apexobserve.alertingservice.repository.IncidentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class IncidentService {
    private static final Logger log = LoggerFactory.getLogger(IncidentService.class);
    private final IncidentRepository incidentRepository;

    public IncidentService(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    public List<IncidentEntity> getAllIncidents(String tenantId) {
        return incidentRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);
    }

    public IncidentEntity getIncident(String tenantId, UUID incidentId) {
        return incidentRepository.findById(incidentId)
                .filter(i -> i.getTenantId().equals(tenantId))
                .orElseThrow(() -> new RuntimeException("Incident not found: " + incidentId));
    }

    public IncidentEntity updateIncidentStatus(String tenantId, UUID incidentId, String newStatus) {
        IncidentEntity incident = getIncident(tenantId, incidentId);
        String currentStatus = incident.getStatus();
        
        // Validate transitions: OPEN -> ACKNOWLEDGED -> INVESTIGATING -> IDENTIFIED -> RESOLVING -> RESOLVED
        // Allow moving back to previous states, but not skipping intermediate active states unless it's REOPENED.
        // For simplicity and resilience, we just apply the status and update timestamp, 
        // with the exception that RESOLVED can only become REOPENED if within 24h, else invalid.
        
        if ("RESOLVED".equals(currentStatus) && "REOPENED".equals(newStatus)) {
            if (incident.getUpdatedAt().until(OffsetDateTime.now(), ChronoUnit.HOURS) > 24) {
                throw new IllegalStateException("Cannot reopen an incident after 24 hours of resolution");
            }
        }
        
        if ("RESOLVED".equals(currentStatus) && !"REOPENED".equals(newStatus)) {
            throw new IllegalStateException("Incident is resolved, cannot change state to " + newStatus);
        }

        incident.setStatus(newStatus);
        incident.setUpdatedAt(OffsetDateTime.now());
        return incidentRepository.save(incident);
    }
}
