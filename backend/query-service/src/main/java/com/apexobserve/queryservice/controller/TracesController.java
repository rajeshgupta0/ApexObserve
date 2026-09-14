package com.apexobserve.queryservice.controller;

import com.apexobserve.queryservice.entity.TraceEntity;
import com.apexobserve.queryservice.repository.TraceRepository;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/traces")
public class TracesController {
    
    private final TraceRepository traceRepository;

    public TracesController(TraceRepository traceRepository) {
        this.traceRepository = traceRepository;
    }

    @GetMapping("/recent")
    public List<TraceEntity> getRecentTraces(@RequestHeader(value = "X-Tenant-ID", defaultValue = "default") String tenantId) {
        OffsetDateTime startTime = OffsetDateTime.now().minusHours(24);
        return traceRepository.findRecentTraces(tenantId, startTime);
    }
    
    @GetMapping("/{traceId}")
    public List<TraceEntity> getTraceSpans(@RequestHeader(value = "X-Tenant-ID", defaultValue = "default") String tenantId, @PathVariable String traceId) {
        return traceRepository.findSpansByTraceId(tenantId, traceId);
    }
}
