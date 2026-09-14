package com.apexobserve.queryservice.repository;

import com.apexobserve.queryservice.entity.TraceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface TraceRepository extends JpaRepository<TraceEntity, Object> {
    
    @Query("SELECT t FROM TraceEntity t WHERE t.tenantId = :tenantId AND t.startTime >= :startTime ORDER BY t.startTime DESC LIMIT 100")
    List<TraceEntity> findRecentTraces(@Param("tenantId") String tenantId, @Param("startTime") OffsetDateTime startTime);
    
    @Query("SELECT t FROM TraceEntity t WHERE t.tenantId = :tenantId AND t.traceId = :traceId ORDER BY t.startTime ASC")
    List<TraceEntity> findSpansByTraceId(@Param("tenantId") String tenantId, @Param("traceId") String traceId);

    @Query(value = "SELECT service_id, " +
                   "AVG(duration_ms) as avg_latency, " +
                   "SUM(CASE WHEN status_code = 'ERROR' THEN 1 ELSE 0 END) as total_errors, " +
                   "COUNT(*) as total_traffic " +
                   "FROM traces " +
                   "WHERE tenant_id = :tenantId AND start_time >= :startTime " +
                   "GROUP BY service_id", nativeQuery = true)
    List<Object[]> calculateHealthScoreBase(
        @Param("tenantId") String tenantId,
        @Param("startTime") OffsetDateTime startTime
    );
}
