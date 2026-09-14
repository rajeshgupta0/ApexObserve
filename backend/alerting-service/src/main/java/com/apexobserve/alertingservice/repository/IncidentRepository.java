package com.apexobserve.alertingservice.repository;

import com.apexobserve.alertingservice.entity.IncidentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface IncidentRepository extends JpaRepository<IncidentEntity, UUID> {
    List<IncidentEntity> findByTenantIdOrderByCreatedAtDesc(String tenantId);
    List<IncidentEntity> findByTenantIdAndStatusOrderByCreatedAtDesc(String tenantId, String status);
}
