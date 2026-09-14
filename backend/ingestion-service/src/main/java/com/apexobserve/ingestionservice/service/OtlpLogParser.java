package com.apexobserve.ingestionservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OtlpLogParser {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<ParsedLog> parseOtlp(String otlpJson) throws Exception {
        JsonNode root = objectMapper.readTree(otlpJson);
        List<ParsedLog> parsedLogs = new ArrayList<>();

        JsonNode resourceLogsNode = root.path("resourceLogs");
        if (resourceLogsNode.isMissingNode() || !resourceLogsNode.isArray()) {
            return parsedLogs;
        }

        for (JsonNode rl : resourceLogsNode) {
            String serviceName = "unknown";
            Map<String, String> resourceAttributes = parseAttributes(rl.path("resource").path("attributes"));
            if (resourceAttributes.containsKey("service.name")) {
                serviceName = resourceAttributes.get("service.name");
            }
            String environment = resourceAttributes.getOrDefault("deployment.environment", "unknown");

            JsonNode scopeLogsNode = rl.path("scopeLogs");
            if (scopeLogsNode.isArray()) {
                for (JsonNode sl : scopeLogsNode) {
                    JsonNode logRecordsNode = sl.path("logRecords");
                    if (logRecordsNode.isArray()) {
                        for (JsonNode logRec : logRecordsNode) {
                            ParsedLog pl = new ParsedLog();
                            pl.setServiceName(serviceName);
                            pl.setEnvironment(environment);
                            
                            if (logRec.has("timeUnixNano")) {
                                pl.setTimestamp(logRec.path("timeUnixNano").asLong());
                            }
                            if (logRec.has("observedTimeUnixNano")) {
                                pl.setObservedTimestamp(logRec.path("observedTimeUnixNano").asLong());
                            }
                            if (logRec.has("severityNumber")) {
                                pl.setSeverityNumber(logRec.path("severityNumber").asInt());
                            }
                            if (logRec.has("severityText")) {
                                pl.setSeverityText(logRec.path("severityText").asText());
                            }
                            if (logRec.has("body") && logRec.path("body").has("stringValue")) {
                                pl.setBody(logRec.path("body").path("stringValue").asText());
                            }
                            if (logRec.has("traceId")) {
                                pl.setTraceId(logRec.path("traceId").asText());
                            }
                            if (logRec.has("spanId")) {
                                pl.setSpanId(logRec.path("spanId").asText());
                            }

                            Map<String, String> logAttributes = parseAttributes(logRec.path("attributes"));
                            pl.setLogAttributes(logAttributes);
                            pl.setResourceAttributes(resourceAttributes);

                            parsedLogs.add(pl);
                        }
                    }
                }
            }
        }
        return parsedLogs;
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

    public static class ParsedLog {
        private long timestamp;
        private long observedTimestamp;
        private int severityNumber;
        private String severityText;
        private String body;
        private String traceId;
        private String spanId;
        private String serviceName;
        private String environment;
        private Map<String, String> resourceAttributes;
        private Map<String, String> logAttributes;
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        public long getObservedTimestamp() { return observedTimestamp; }
        public void setObservedTimestamp(long observedTimestamp) { this.observedTimestamp = observedTimestamp; }
        public int getSeverityNumber() { return severityNumber; }
        public void setSeverityNumber(int severityNumber) { this.severityNumber = severityNumber; }
        public String getSeverityText() { return severityText; }
        public void setSeverityText(String severityText) { this.severityText = severityText; }
        public String getBody() { return body; }
        public void setBody(String body) { this.body = body; }
        public String getTraceId() { return traceId; }
        public void setTraceId(String traceId) { this.traceId = traceId; }
        public String getSpanId() { return spanId; }
        public void setSpanId(String spanId) { this.spanId = spanId; }
        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }
        public String getEnvironment() { return environment; }
        public void setEnvironment(String environment) { this.environment = environment; }
        public Map<String, String> getResourceAttributes() { return resourceAttributes; }
        public void setResourceAttributes(Map<String, String> resourceAttributes) { this.resourceAttributes = resourceAttributes; }
        public Map<String, String> getLogAttributes() { return logAttributes; }
        public void setLogAttributes(Map<String, String> logAttributes) { this.logAttributes = logAttributes; }
    }
}
