package com.apexobserve.ingestionservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MetricsIngestionService {
    private static final Logger log = LoggerFactory.getLogger(MetricsIngestionService.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OtlpMetricParser metricParser;

    public MetricsIngestionService(KafkaTemplate<String, String> kafkaTemplate, OtlpMetricParser metricParser) {
        this.kafkaTemplate = kafkaTemplate;
        this.metricParser = metricParser;
    }

    @KafkaListener(topics = "telemetry.metrics", groupId = "ingestion-group")
    public void consumeMetrics(String otlpJson) {
        try {
            log.info("Processing metric payload of length {}", otlpJson.length());
            
            List<OtlpMetricParser.ParsedMetric> parsedMetrics = metricParser.parseOtlp(otlpJson);
            
            for (OtlpMetricParser.ParsedMetric pm : parsedMetrics) {
                log.info("Parsed metric: {} (Type: {}, Service: {}, Env: {}) - Label Hash: {}", 
                         pm.getName(), pm.getMetricType(), pm.getServiceName(), pm.getEnvironment(), pm.getLabelHash());
            }
            
            // Forward validated JSON payload to next stage
            kafkaTemplate.send("telemetry.metrics.validated", otlpJson);
        } catch (Exception e) {
            log.error("Failed to parse OTLP JSON", e);
        }
    }
}
