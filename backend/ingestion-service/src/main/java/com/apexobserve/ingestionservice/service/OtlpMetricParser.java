package com.apexobserve.ingestionservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Component
public class OtlpMetricParser {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<ParsedMetric> parseOtlp(String otlpJson) throws Exception {
        JsonNode root = objectMapper.readTree(otlpJson);
        List<ParsedMetric> parsedMetrics = new ArrayList<>();

        JsonNode resourceMetricsNode = root.path("resourceMetrics");
        if (resourceMetricsNode.isMissingNode() || !resourceMetricsNode.isArray()) {
            return parsedMetrics;
        }

        for (JsonNode rm : resourceMetricsNode) {
            String serviceName = "unknown";
            Map<String, String> resourceAttributes = parseAttributes(rm.path("resource").path("attributes"));
            if (resourceAttributes.containsKey("service.name")) {
                serviceName = resourceAttributes.get("service.name");
            }
            String environment = resourceAttributes.getOrDefault("deployment.environment", "unknown");

            JsonNode scopeMetricsNode = rm.path("scopeMetrics");
            if (scopeMetricsNode.isArray()) {
                for (JsonNode sm : scopeMetricsNode) {
                    JsonNode metricsNode = sm.path("metrics");
                    if (metricsNode.isArray()) {
                        for (JsonNode m : metricsNode) {
                            String name = m.path("name").asText();
                            String description = m.path("description").asText();
                            String unit = m.path("unit").asText();
                            
                            // Determine type and extract data points
                            if (m.has("gauge")) {
                                extractDataPoints(m.path("gauge").path("dataPoints"), parsedMetrics, "Gauge", name, description, unit, serviceName, environment, resourceAttributes);
                            } else if (m.has("sum")) {
                                extractDataPoints(m.path("sum").path("dataPoints"), parsedMetrics, "Sum", name, description, unit, serviceName, environment, resourceAttributes);
                            } else if (m.has("histogram")) {
                                extractDataPoints(m.path("histogram").path("dataPoints"), parsedMetrics, "Histogram", name, description, unit, serviceName, environment, resourceAttributes);
                            }
                        }
                    }
                }
            }
        }
        return parsedMetrics;
    }

    private void extractDataPoints(JsonNode dataPointsNode, List<ParsedMetric> parsedMetrics, String type, String name, String description, String unit, String serviceName, String environment, Map<String, String> resourceAttributes) {
        if (!dataPointsNode.isArray()) return;

        for (JsonNode dp : dataPointsNode) {
            ParsedMetric pm = new ParsedMetric();
            pm.setName(name);
            pm.setDescription(description);
            pm.setUnit(unit);
            pm.setMetricType(type);
            pm.setServiceName(serviceName);
            pm.setEnvironment(environment);
            
            long timestamp = dp.path("timeUnixNano").asLong();
            pm.setTimestamp(timestamp);
            
            if (dp.has("startTimeUnixNano")) {
                pm.setStartTimestamp(dp.path("startTimeUnixNano").asLong());
            }

            // Extract labels/attributes
            Map<String, String> labels = parseAttributes(dp.path("attributes"));
            pm.setLabels(labels);

            // Compute label hash
            pm.setLabelHash(computeLabelHash(labels));

            // Extract values
            if (dp.has("asDouble")) {
                pm.setValue(dp.path("asDouble").asDouble());
            } else if (dp.has("asInt")) {
                pm.setValue(dp.path("asInt").asLong());
            }

            if ("Histogram".equals(type)) {
                if (dp.has("count")) pm.setCount(dp.path("count").asLong());
                if (dp.has("sum")) pm.setSum(dp.path("sum").asDouble());
                // Bucket extraction omitted for brevity but would go here
            }
            
            parsedMetrics.add(pm);
        }
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

    private String computeLabelHash(Map<String, String> labels) {
        if (labels == null || labels.isEmpty()) {
            return hashString("");
        }
        List<String> keys = new ArrayList<>(labels.keySet());
        Collections.sort(keys);
        
        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            String value = labels.get(key);
            if (value == null) value = "";
            sb.append(escape(key)).append("=").append(escape(value)).append(";");
        }
        return hashString(sb.toString());
    }
    
    private String escape(String input) {
        return input.replace("\\", "\\\\").replace("=", "\\=").replace(";", "\\;");
    }

    private String hashString(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not found", e);
        }
    }

    public static class ParsedMetric {
        private String name;
        private String description;
        private String unit;
        private String metricType;
        private long timestamp;
        private long startTimestamp;
        private String serviceName;
        private String environment;
        private Map<String, String> labels;
        private String labelHash;
        private double value;
        private long count;
        private double sum;
        
        // getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        public String getMetricType() { return metricType; }
        public void setMetricType(String metricType) { this.metricType = metricType; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        public long getStartTimestamp() { return startTimestamp; }
        public void setStartTimestamp(long startTimestamp) { this.startTimestamp = startTimestamp; }
        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }
        public String getEnvironment() { return environment; }
        public void setEnvironment(String environment) { this.environment = environment; }
        public Map<String, String> getLabels() { return labels; }
        public void setLabels(Map<String, String> labels) { this.labels = labels; }
        public String getLabelHash() { return labelHash; }
        public void setLabelHash(String labelHash) { this.labelHash = labelHash; }
        public double getValue() { return value; }
        public void setValue(double value) { this.value = value; }
        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }
        public double getSum() { return sum; }
        public void setSum(double sum) { this.sum = sum; }
    }
}
