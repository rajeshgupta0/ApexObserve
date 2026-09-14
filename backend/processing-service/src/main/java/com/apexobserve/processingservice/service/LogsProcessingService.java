package com.apexobserve.processingservice.service;

import com.apexobserve.processingservice.entity.LogEntity;
import com.apexobserve.processingservice.repository.LogRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class LogsProcessingService {
    private static final Logger log = LoggerFactory.getLogger(LogsProcessingService.class);
    private final LogRepository logRepository;
    private final OtlpParser otlpParser;

    public LogsProcessingService(LogRepository logRepository, OtlpParser otlpParser) {
        this.logRepository = logRepository;
        this.otlpParser = otlpParser;
    }

    @KafkaListener(topics = "telemetry.logs.validated", groupId = "processing-group")
    public void processLogs(String validatedOtlpJson) {
        try {
            JsonNode root = otlpParser.getMapper().readTree(validatedOtlpJson);
            if (root.has("resourceLogs")) {
                for (JsonNode rl : root.get("resourceLogs")) {
                    String serviceName = otlpParser.extractServiceName(rl.get("resource"));
                    
                    if (rl.has("scopeLogs")) {
                        for (JsonNode sl : rl.get("scopeLogs")) {
                            if (sl.has("logRecords")) {
                                for (JsonNode lr : sl.get("logRecords")) {
                                    LogEntity logEntry = new LogEntity();
                                    logEntry.setId(UUID.randomUUID());
                                    logEntry.setTenantId("default");
                                    logEntry.setServiceId(serviceName);
                                    
                                    logEntry.setTime(otlpParser.parseNanoTime(lr.path("timeUnixNano").asText()));
                                    
                                    if (lr.has("traceId")) logEntry.setTraceId(lr.get("traceId").asText());
                                    if (lr.has("spanId")) logEntry.setSpanId(lr.get("spanId").asText());
                                    
                                    logEntry.setSeverity(lr.path("severityText").asText("INFO"));
                                    
                                    if (lr.has("body") && lr.get("body").has("stringValue")) {
                                        logEntry.setMessage(lr.get("body").get("stringValue").asText());
                                    } else {
                                        logEntry.setMessage("");
                                    }
                                    
                                    if (lr.has("attributes")) {
                                        logEntry.setAttributes(otlpParser.getMapper().writeValueAsString(lr.get("attributes")));
                                    }
                                    
                                    logRepository.save(logEntry);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse/process OTLP log JSON", e);
        }
    }
}
