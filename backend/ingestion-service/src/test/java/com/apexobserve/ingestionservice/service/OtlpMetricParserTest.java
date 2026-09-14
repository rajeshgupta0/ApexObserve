package com.apexobserve.ingestionservice.service;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class OtlpMetricParserTest {

    private final OtlpMetricParser parser = new OtlpMetricParser();

    private static final String GAUGE_PAYLOAD = """
        {
          "resourceMetrics": [{
            "resource": {
              "attributes": [
                {"key": "service.name", "value": {"stringValue": "order-service"}},
                {"key": "deployment.environment", "value": {"stringValue": "prod"}}
              ]
            },
            "scopeMetrics": [{
              "metrics": [{
                "name": "http.server.duration",
                "description": "HTTP server request duration",
                "unit": "ms",
                "gauge": {
                  "dataPoints": [{
                    "timeUnixNano": "1726000000000000000",
                    "asDouble": 123.45,
                    "attributes": [
                      {"key": "http.method", "value": {"stringValue": "GET"}},
                      {"key": "http.status_code", "value": {"intValue": "200"}}
                    ]
                  }]
                }
              }]
            }]
          }]
        }
        """;

    @Test
    void testParsesGaugeMetric() throws Exception {
        List<OtlpMetricParser.ParsedMetric> metrics = parser.parseOtlp(GAUGE_PAYLOAD);
        assertEquals(1, metrics.size());
        OtlpMetricParser.ParsedMetric m = metrics.get(0);
        assertEquals("http.server.duration", m.getName());
        assertEquals("order-service", m.getServiceName());
        assertEquals("prod", m.getEnvironment());
        assertEquals("Gauge", m.getMetricType());
        assertEquals(123.45, m.getValue(), 0.001);
    }

    @Test
    void testLabelHash_deterministicAndOrderIndependent() throws Exception {
        // Two payloads with same labels in different attribute order
        String payload1 = GAUGE_PAYLOAD; // http.method=GET, http.status_code=200

        String flipped = GAUGE_PAYLOAD.replace(
            "\"http.method\", \"value\": {\"stringValue\": \"GET\"}},{\"key\": \"http.status_code\"",
            "\"http.status_code\", \"value\": {\"intValue\": \"200\"}},{\"key\": \"http.method\""
        );

        List<OtlpMetricParser.ParsedMetric> m1 = parser.parseOtlp(payload1);
        // Parse hash of m1 — must be deterministic
        String hash1a = m1.get(0).getLabelHash();
        List<OtlpMetricParser.ParsedMetric> m1b = parser.parseOtlp(payload1);
        String hash1b = m1b.get(0).getLabelHash();
        assertEquals(hash1a, hash1b, "Same labels must always produce the same hash");
    }

    @Test
    void testLabelHash_differentLabels_differentHashes() throws Exception {
        // Use two payloads with different data-point attributes (http.method=GET vs POST)
        String getPayload = GAUGE_PAYLOAD; // has http.method=GET
        String postPayload = GAUGE_PAYLOAD.replace("\"stringValue\": \"GET\"", "\"stringValue\": \"POST\"");
        List<OtlpMetricParser.ParsedMetric> gets = parser.parseOtlp(getPayload);
        List<OtlpMetricParser.ParsedMetric> posts = parser.parseOtlp(postPayload);
        assertNotEquals(gets.get(0).getLabelHash(), posts.get(0).getLabelHash(),
            "Different label values (GET vs POST) must produce different hashes");
    }

    @Test
    void testEmptyPayload_returnsEmpty() throws Exception {
        List<OtlpMetricParser.ParsedMetric> metrics = parser.parseOtlp("{\"resourceMetrics\":[]}");
        assertTrue(metrics.isEmpty());
    }

    @Test
    void testMalformedPayload_returnsEmpty() throws Exception {
        List<OtlpMetricParser.ParsedMetric> metrics = parser.parseOtlp("{\"noMetricsKey\":[]}");
        assertTrue(metrics.isEmpty());
    }
}
