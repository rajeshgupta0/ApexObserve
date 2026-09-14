package com.apexobserve.queryservice.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/metrics/internal")
public class InternalMetricsController {
    private final JdbcTemplate jdbcTemplate;

    public InternalMetricsController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Used by alerting service to check for high latency or error rates
    @GetMapping("/anomalies")
    public List<Map<String, Object>> getAnomalousServices(@RequestHeader(value = "X-Tenant-ID", defaultValue = "default") String tenantId) {
        String sql = "SELECT service_id, metric_name, AVG(value) as avg_val " +
                     "FROM metrics " +
                     "WHERE tenant_id = ? AND time > NOW() - INTERVAL '5 minutes' " +
                     "AND (metric_name LIKE '%error%' OR metric_name LIKE '%duration%') " +
                     "GROUP BY service_id, metric_name " +
                     "HAVING AVG(value) > 1000"; // simplistic threshold for demo
        return jdbcTemplate.queryForList(sql, tenantId);
    }
}
