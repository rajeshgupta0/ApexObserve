package com.apexobserve.processingservice.repository;

import com.apexobserve.processingservice.entity.TraceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TraceRepository extends JpaRepository<TraceEntity, Object> {}
