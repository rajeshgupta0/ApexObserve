package com.apexobserve.alertingservice.service;

import com.apexobserve.alertingservice.entity.EvidenceEntity;
import com.apexobserve.alertingservice.repository.EvidenceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class EvidenceService {
    private static final Logger log = LoggerFactory.getLogger(EvidenceService.class);
    private final EvidenceRepository evidenceRepository;

    public EvidenceService(EvidenceRepository evidenceRepository) {
        this.evidenceRepository = evidenceRepository;
    }

    public void attachEvidenceToIncident(String tenantId, UUID incidentId, String evidenceType, String sourceId, String description, Double relevanceScore) {
        try {
            EvidenceEntity evidence = new EvidenceEntity();
            evidence.setId(UUID.randomUUID());
            evidence.setTenantId(tenantId);
            evidence.setIncidentId(incidentId);
            evidence.setEvidenceType(evidenceType);
            evidence.setSourceId(sourceId);
            evidence.setDescription(description);
            evidence.setRelevanceScore(relevanceScore);
            evidence.setCreatedAt(OffsetDateTime.now());

            evidenceRepository.save(evidence);
            log.info("Attached evidence {} of type {} to incident {}", evidence.getId(), evidenceType, incidentId);
        } catch (Exception e) {
            log.error("Failed to attach evidence to incident", e);
        }
    }

    public List<EvidenceEntity> getEvidenceForIncident(String tenantId, UUID incidentId) {
        return evidenceRepository.findByTenantIdAndIncidentIdOrderByRelevanceScoreDesc(tenantId, incidentId);
    }
}
