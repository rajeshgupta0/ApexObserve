package com.apexobserve.alertingservice.repository;

import com.apexobserve.alertingservice.entity.EvidenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EvidenceRepository extends JpaRepository<EvidenceEntity, UUID> {
    List<EvidenceEntity> findByIncidentIdOrderByRelevanceScoreDesc(UUID incidentId);
    List<EvidenceEntity> findByTenantIdAndIncidentIdOrderByRelevanceScoreDesc(String tenantId, UUID incidentId);
}
