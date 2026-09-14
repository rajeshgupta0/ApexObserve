package com.apexobserve.queryservice.repository;

import com.apexobserve.queryservice.entity.MetricEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface MetricRepository extends JpaRepository<MetricEntity, Object> {
    
    @Query("SELECT m FROM MetricEntity m WHERE m.tenantId = :tenantId AND m.serviceId = :serviceId AND m.metricName = :metricName AND m.time >= :startTime AND m.time <= :endTime ORDER BY m.time ASC")
    List<MetricEntity> findMetrics(
        @Param("tenantId") String tenantId,
        @Param("serviceId") String serviceId,
        @Param("metricName") String metricName,
        @Param("startTime") OffsetDateTime startTime,
        @Param("endTime") OffsetDateTime endTime
    );

    List<MetricEntity> findByTenantIdAndServiceIdAndMetricNameAndTimeGreaterThanEqualOrderByTimeAsc(
            String tenantId, String serviceId, String metricName, OffsetDateTime time);

    @Query(value = "SELECT service_id, " +
                   "AVG(CASE WHEN metric_name = 'latency' THEN value ELSE NULL END) as avg_latency, " +
                   "SUM(CASE WHEN metric_name = 'error_rate' THEN value ELSE NULL END) as total_errors, " +
                   "SUM(CASE WHEN metric_name = 'traffic' THEN value ELSE NULL END) as total_traffic " +
                   "FROM metrics " +
                   "WHERE tenant_id = :tenantId AND time >= :startTime " +
                   "GROUP BY service_id", nativeQuery = true)
    List<Object[]> calculateHealthScoreBase(
        @Param("tenantId") String tenantId,
        @Param("startTime") OffsetDateTime startTime
    );
}
