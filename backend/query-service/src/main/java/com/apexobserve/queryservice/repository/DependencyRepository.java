package com.apexobserve.queryservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface DependencyRepository extends org.springframework.data.repository.Repository<com.apexobserve.queryservice.entity.TraceEntity, Object> {
    
    // Join traces on parent_span_id to find service dependencies
    @Query(value = "SELECT p.service_id AS source, c.service_id AS target, COUNT(*) AS call_count " +
                   "FROM traces c " +
                   "JOIN traces p ON c.parent_span_id = p.span_id AND c.tenant_id = p.tenant_id " +
                   "WHERE c.tenant_id = :tenantId AND c.service_id != p.service_id " +
                   "GROUP BY p.service_id, c.service_id", nativeQuery = true)
    List<Object[]> findServiceDependencies(@Param("tenantId") String tenantId);
}
