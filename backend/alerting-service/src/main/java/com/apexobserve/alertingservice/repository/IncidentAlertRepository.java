package com.apexobserve.alertingservice.repository;

import com.apexobserve.alertingservice.entity.IncidentAlertEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface IncidentAlertRepository extends JpaRepository<IncidentAlertEntity, Object> {
    List<IncidentAlertEntity> findByIncidentId(UUID incidentId);
    List<IncidentAlertEntity> findByAlertId(UUID alertId);
}
