package com.apexobserve.ingestionservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OtlpTraceParser {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<ParsedSpan> parseOtlp(String otlpJson) throws Exception {
        JsonNode root = objectMapper.readTree(otlpJson);
        List<ParsedSpan> parsedSpans = new ArrayList<>();

        JsonNode resourceSpansNode = root.path("resourceSpans");
        if (resourceSpansNode.isMissingNode() || !resourceSpansNode.isArray()) {
            return parsedSpans;
        }

        for (JsonNode rs : resourceSpansNode) {
            String serviceName = "unknown";
            Map<String, String> resourceAttributes = parseAttributes(rs.path("resource").path("attributes"));
            if (resourceAttributes.containsKey("service.name")) {
                serviceName = resourceAttributes.get("service.name");
            }
            String environment = resourceAttributes.getOrDefault("deployment.environment", "unknown");

            JsonNode scopeSpansNode = rs.path("scopeSpans");
            if (scopeSpansNode.isArray()) {
                for (JsonNode ss : scopeSpansNode) {
                    JsonNode spansNode = ss.path("spans");
                    if (spansNode.isArray()) {
                        for (JsonNode spanRec : spansNode) {
                            ParsedSpan ps = new ParsedSpan();
                            ps.setServiceName(serviceName);
                            ps.setEnvironment(environment);
                            
                            if (spanRec.has("traceId")) {
                                ps.setTraceId(spanRec.path("traceId").asText());
                            }
                            if (spanRec.has("spanId")) {
                                ps.setSpanId(spanRec.path("spanId").asText());
                            }
                            if (spanRec.has("parentSpanId")) {
                                ps.setParentSpanId(spanRec.path("parentSpanId").asText());
                            }
                            if (spanRec.has("name")) {
                                ps.setOperation(spanRec.path("name").asText());
                            }
                            if (spanRec.has("startTimeUnixNano")) {
                                ps.setStartTime(spanRec.path("startTimeUnixNano").asLong());
                            }
                            if (spanRec.has("endTimeUnixNano")) {
                                ps.setEndTime(spanRec.path("endTimeUnixNano").asLong());
                                ps.setDuration(ps.getEndTime() - ps.getStartTime());
                            }
                            if (spanRec.has("status") && spanRec.path("status").has("code")) {
                                ps.setStatus(spanRec.path("status").path("code").asInt());
                            }

                            Map<String, String> spanAttributes = parseAttributes(spanRec.path("attributes"));
                            ps.setAttributes(spanAttributes);
                            ps.setResourceAttributes(resourceAttributes);

                            parsedSpans.add(ps);
                        }
                    }
                }
            }
        }
        return parsedSpans;
    }

    private Map<String, String> parseAttributes(JsonNode attributesNode) {
        Map<String, String> attributes = new HashMap<>();
        if (attributesNode.isArray()) {
            for (JsonNode attr : attributesNode) {
                String key = attr.path("key").asText();
                JsonNode valueNode = attr.path("value");
                if (valueNode.has("stringValue")) {
                    attributes.put(key, valueNode.path("stringValue").asText());
                } else if (valueNode.has("intValue")) {
                    attributes.put(key, valueNode.path("intValue").asText());
                } else if (valueNode.has("doubleValue")) {
                    attributes.put(key, valueNode.path("doubleValue").asText());
                }
            }
        }
        return attributes;
    }

    public static class ParsedSpan {
        private String traceId;
        private String spanId;
        private String parentSpanId;
        private String serviceName;
        private String operation;
        private long startTime;
        private long endTime;
        private long duration;
        private int status;
        private String environment;
        private Map<String, String> attributes;
        private Map<String, String> resourceAttributes;
        
        // Getters and Setters
        public String getTraceId() { return traceId; }
        public void setTraceId(String traceId) { this.traceId = traceId; }
        public String getSpanId() { return spanId; }
        public void setSpanId(String spanId) { this.spanId = spanId; }
        public String getParentSpanId() { return parentSpanId; }
        public void setParentSpanId(String parentSpanId) { this.parentSpanId = parentSpanId; }
        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }
        public String getOperation() { return operation; }
        public void setOperation(String operation) { this.operation = operation; }
        public long getStartTime() { return startTime; }
        public void setStartTime(long startTime) { this.startTime = startTime; }
        public long getEndTime() { return endTime; }
        public void setEndTime(long endTime) { this.endTime = endTime; }
        public long getDuration() { return duration; }
        public void setDuration(long duration) { this.duration = duration; }
        public int getStatus() { return status; }
        public void setStatus(int status) { this.status = status; }
        public String getEnvironment() { return environment; }
        public void setEnvironment(String environment) { this.environment = environment; }
        public Map<String, String> getAttributes() { return attributes; }
        public void setAttributes(Map<String, String> attributes) { this.attributes = attributes; }
        public Map<String, String> getResourceAttributes() { return resourceAttributes; }
        public void setResourceAttributes(Map<String, String> resourceAttributes) { this.resourceAttributes = resourceAttributes; }
    }
}
