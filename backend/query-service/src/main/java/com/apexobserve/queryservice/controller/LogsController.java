package com.apexobserve.queryservice.controller;

import com.apexobserve.queryservice.entity.LogEntity;
import com.apexobserve.queryservice.repository.LogRepository;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class LogsController {
    
    private final LogRepository logRepository;

    public LogsController(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @GetMapping("/recent")
    public List<LogEntity> getRecentLogs(@RequestHeader(value = "X-Tenant-ID", defaultValue = "default") String tenantId) {
        OffsetDateTime startTime = OffsetDateTime.now().minusHours(24);
        return logRepository.findRecentLogs(tenantId, startTime);
    }
}
