package com.apexobserve.processingservice.service;

import com.apexobserve.processingservice.entity.TraceEntity;
import com.apexobserve.processingservice.repository.TraceRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class TracesProcessingService {
    private static final Logger log = LoggerFactory.getLogger(TracesProcessingService.class);
    private final TraceRepository traceRepository;
    private final OtlpParser otlpParser;

    public TracesProcessingService(TraceRepository traceRepository, OtlpParser otlpParser) {
        this.traceRepository = traceRepository;
        this.otlpParser = otlpParser;
    }

    @KafkaListener(topics = "telemetry.traces.validated", groupId = "processing-group")
    public void processTraces(String validatedOtlpJson) {
        try {
            JsonNode root = otlpParser.getMapper().readTree(validatedOtlpJson);
            if (root.has("resourceSpans")) {
                for (JsonNode rs : root.get("resourceSpans")) {
                    String serviceName = otlpParser.extractServiceName(rs.get("resource"));
                    
                    if (rs.has("scopeSpans")) {
                        for (JsonNode ss : rs.get("scopeSpans")) {
                            if (ss.has("spans")) {
                                for (JsonNode spanNode : ss.get("spans")) {
                                    TraceEntity span = new TraceEntity();
                                    span.setTenantId("default");
                                    span.setServiceId(serviceName);
                                    
                                    span.setTraceId(spanNode.path("traceId").asText());
                                    span.setSpanId(spanNode.path("spanId").asText());
                                    if (spanNode.has("parentSpanId") && !spanNode.get("parentSpanId").asText().isEmpty()) {
                                        span.setParentSpanId(spanNode.get("parentSpanId").asText());
                                    }
                                    
                                    span.setOperationName(spanNode.path("name").asText("unknown"));
                                    
                                    OffsetDateTime startTime = otlpParser.parseNanoTime(spanNode.path("startTimeUnixNano").asText());
                                    OffsetDateTime endTime = otlpParser.parseNanoTime(spanNode.path("endTimeUnixNano").asText());
                                    span.setStartTime(startTime);
                                    span.setEndTime(endTime);
                                    
                                    long duration = ChronoUnit.MILLIS.between(startTime, endTime);
                                    span.setDurationMs(duration);
                                    
                                    String statusCode = "UNSET";
                                    if (spanNode.has("status") && spanNode.get("status").has("code")) {
                                        statusCode = spanNode.get("status").get("code").asText();
                                        if ("STATUS_CODE_OK".equals(statusCode) || "1".equals(statusCode)) statusCode = "OK";
                                        else if ("STATUS_CODE_ERROR".equals(statusCode) || "2".equals(statusCode)) statusCode = "ERROR";
                                    }
                                    span.setStatusCode(statusCode);
                                    
                                    if (spanNode.has("attributes")) {
                                        span.setAttributes(otlpParser.getMapper().writeValueAsString(spanNode.get("attributes")));
                                    }
                                    
                                    traceRepository.save(span);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse/process OTLP trace JSON", e);
        }
    }
}
