package com.apexobserve.alertingservice.repository;

import com.apexobserve.alertingservice.entity.AlertEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AlertRepository extends JpaRepository<AlertEntity, UUID> {
    List<AlertEntity> findByTenantIdAndStatusOrderByCreatedAtDesc(String tenantId, String status);
    List<AlertEntity> findByTenantIdOrderByCreatedAtDesc(String tenantId);
}
