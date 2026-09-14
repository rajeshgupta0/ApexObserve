package com.apexobserve.queryservice.repository;

import com.apexobserve.queryservice.entity.LogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface LogRepository extends JpaRepository<LogEntity, Object> {
    
    @Query("SELECT l FROM LogEntity l WHERE l.tenantId = :tenantId AND l.time >= :startTime ORDER BY l.time DESC LIMIT 100")
    List<LogEntity> findRecentLogs(@Param("tenantId") String tenantId, @Param("startTime") OffsetDateTime startTime);
}
