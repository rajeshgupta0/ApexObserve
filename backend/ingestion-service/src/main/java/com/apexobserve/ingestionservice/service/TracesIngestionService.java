package com.apexobserve.ingestionservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TracesIngestionService {
    private static final Logger log = LoggerFactory.getLogger(TracesIngestionService.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OtlpTraceParser traceParser;

    public TracesIngestionService(KafkaTemplate<String, String> kafkaTemplate, OtlpTraceParser traceParser) {
        this.kafkaTemplate = kafkaTemplate;
        this.traceParser = traceParser;
    }

    @KafkaListener(topics = "telemetry.traces", groupId = "ingestion-group")
    public void consumeTraces(String otlpJson) {
        try {
            log.info("Processing trace payload of length {}", otlpJson.length());
            
            List<OtlpTraceParser.ParsedSpan> parsedSpans = traceParser.parseOtlp(otlpJson);
            
            for (OtlpTraceParser.ParsedSpan ps : parsedSpans) {
                log.info("Parsed span: {} (Service: {}, TraceId: {}, SpanId: {}) - Duration: {}ns", 
                         ps.getOperation(), ps.getServiceName(), ps.getTraceId(), ps.getSpanId(), ps.getDuration());
            }
            
            // Forward validated JSON payload to next stage
            kafkaTemplate.send("telemetry.traces.validated", otlpJson);
        } catch (Exception e) {
            log.error("Failed to parse OTLP Trace JSON", e);
        }
    }
}
