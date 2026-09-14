package com.apexobserve.processingservice.repository;

import com.apexobserve.processingservice.entity.LogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogRepository extends JpaRepository<LogEntity, Object> {}
