package com.apexobserve.processingservice.repository;

import com.apexobserve.processingservice.entity.MetricEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MetricRepository extends JpaRepository<MetricEntity, Object> {
}
