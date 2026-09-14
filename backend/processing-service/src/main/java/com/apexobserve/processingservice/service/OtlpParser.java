package com.apexobserve.processingservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Component
public class OtlpParser {
    private final ObjectMapper mapper = new ObjectMapper();

    // Helper to get service name from resource attributes
    public String extractServiceName(JsonNode resource) {
        if (resource != null && resource.has("attributes")) {
            for (JsonNode attr : resource.get("attributes")) {
                if ("service.name".equals(attr.path("key").asText())) {
                    JsonNode val = attr.path("value");
                    if (val.has("stringValue")) return val.get("stringValue").asText();
                }
            }
        }
        return "unknown_service";
    }

    public OffsetDateTime parseNanoTime(String nanoTimeStr) {
        if (nanoTimeStr == null || nanoTimeStr.isEmpty()) return OffsetDateTime.now();
        try {
            long nanoTime = Long.parseLong(nanoTimeStr);
            long epochSeconds = nanoTime / 1_000_000_000L;
            long nanoAdjustment = nanoTime % 1_000_000_000L;
            return OffsetDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds, nanoAdjustment), ZoneOffset.UTC);
        } catch (Exception e) {
            return OffsetDateTime.now();
        }
    }
    
    public ObjectMapper getMapper() {
        return mapper;
    }
}
