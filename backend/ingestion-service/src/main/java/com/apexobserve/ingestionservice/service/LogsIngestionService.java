package com.apexobserve.ingestionservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogsIngestionService {
    private static final Logger log = LoggerFactory.getLogger(LogsIngestionService.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OtlpLogParser logParser;

    public LogsIngestionService(KafkaTemplate<String, String> kafkaTemplate, OtlpLogParser logParser) {
        this.kafkaTemplate = kafkaTemplate;
        this.logParser = logParser;
    }

    @KafkaListener(topics = "telemetry.logs", groupId = "ingestion-group")
    public void consumeLogs(String otlpJson) {
        try {
            log.info("Processing log payload of length {}", otlpJson.length());
            
            List<OtlpLogParser.ParsedLog> parsedLogs = logParser.parseOtlp(otlpJson);
            
            for (OtlpLogParser.ParsedLog pl : parsedLogs) {
                log.info("Parsed log: [{}] {} (Service: {}, Env: {}) - TraceId: {}", 
                         pl.getSeverityText(), pl.getBody(), pl.getServiceName(), pl.getEnvironment(), pl.getTraceId());
            }
            
            // Forward validated JSON payload to next stage
            kafkaTemplate.send("telemetry.logs.validated", otlpJson);
        } catch (Exception e) {
            log.error("Failed to parse OTLP Log JSON", e);
        }
    }
}
