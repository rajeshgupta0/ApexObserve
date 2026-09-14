package com.apexobserve.queryservice.controller;

import com.apexobserve.queryservice.entity.MetricEntity;
import com.apexobserve.queryservice.repository.MetricRepository;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/metrics")
public class MetricsHistoryController {
    
    private final MetricRepository metricRepository;

    public MetricsHistoryController(MetricRepository metricRepository) {
        this.metricRepository = metricRepository;
    }

    @GetMapping("/history")
    public List<MetricEntity> getMetricsHistory(
            @RequestHeader(value = "X-Tenant-ID", defaultValue = "default") String tenantId,
            @RequestParam String serviceId,
            @RequestParam String metricName,
            @RequestParam(defaultValue = "24") int hours) {
        
        OffsetDateTime startTime = OffsetDateTime.now().minusHours(hours);
        return metricRepository.findByTenantIdAndServiceIdAndMetricNameAndTimeGreaterThanEqualOrderByTimeAsc(
                tenantId, serviceId, metricName, startTime);
    }
}
