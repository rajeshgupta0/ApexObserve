package com.apexobserve.processingservice.service;

import com.apexobserve.processingservice.entity.MetricEntity;
import com.apexobserve.processingservice.repository.MetricRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MetricsProcessingService {
    private static final Logger log = LoggerFactory.getLogger(MetricsProcessingService.class);
    private final MetricRepository metricRepository;
    private final OtlpParser otlpParser;

    public MetricsProcessingService(MetricRepository metricRepository, OtlpParser otlpParser) {
        this.metricRepository = metricRepository;
        this.otlpParser = otlpParser;
    }

    @KafkaListener(topics = "telemetry.metrics.validated", groupId = "processing-group")
    public void processMetrics(String validatedOtlpJson) {
        try {
            JsonNode root = otlpParser.getMapper().readTree(validatedOtlpJson);
            if (root.has("resourceMetrics")) {
                for (JsonNode rm : root.get("resourceMetrics")) {
                    String serviceName = otlpParser.extractServiceName(rm.get("resource"));
                    
                    if (rm.has("scopeMetrics")) {
                        for (JsonNode sm : rm.get("scopeMetrics")) {
                            if (sm.has("metrics")) {
                                for (JsonNode m : sm.get("metrics")) {
                                    String metricName = m.path("name").asText();
                                    
                                    // Handle Gauge/Sum datatypes
                                    JsonNode dataPoints = null;
                                    String metricType = "unknown";
                                    if (m.has("gauge")) {
                                        dataPoints = m.get("gauge").get("dataPoints");
                                        metricType = "gauge";
                                    } else if (m.has("sum")) {
                                        dataPoints = m.get("sum").get("dataPoints");
                                        metricType = "sum";
                                    }
                                    
                                    if (dataPoints != null) {
                                        for (JsonNode dp : dataPoints) {
                                            MetricEntity entity = new MetricEntity();
                                            entity.setTenantId("default");
                                            entity.setServiceId(serviceName);
                                            entity.setMetricName(metricName);
                                            entity.setMetricType(metricType);
                                            entity.setTime(otlpParser.parseNanoTime(dp.path("timeUnixNano").asText()));
                                            
                                            if (dp.has("asDouble")) {
                                                entity.setValue(dp.get("asDouble").asDouble());
                                            } else if (dp.has("asInt")) {
                                                entity.setValue(dp.get("asInt").asDouble());
                                            } else {
                                                entity.setValue(0.0);
                                            }
                                            
                                            // Extract labels
                                            Map<String, String> labelsMap = new HashMap<>();
                                            if (dp.has("attributes")) {
                                                for (JsonNode attr : dp.get("attributes")) {
                                                    String key = attr.path("key").asText();
                                                    String val = attr.path("value").path("stringValue").asText();
                                                    labelsMap.put(key, val);
                                                }
                                            }
                                            
                                            String canonicalLabels = LabelHasher.toCanonicalString(labelsMap);
                                            entity.setLabelHash(LabelHasher.hashLabels(canonicalLabels));
                                            entity.setLabels(otlpParser.getMapper().writeValueAsString(labelsMap));
                                            
                                            metricRepository.save(entity);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse/process OTLP metric JSON", e);
        }
    }
}
