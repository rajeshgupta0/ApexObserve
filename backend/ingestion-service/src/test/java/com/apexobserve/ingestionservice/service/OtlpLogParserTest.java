package com.apexobserve.ingestionservice.service;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class OtlpLogParserTest {

    private final OtlpLogParser parser = new OtlpLogParser();

    private static final String LOG_PAYLOAD = """
        {
          "resourceLogs": [{
            "resource": {
              "attributes": [
                {"key": "service.name", "value": {"stringValue": "payment-service"}},
                {"key": "deployment.environment", "value": {"stringValue": "prod"}}
              ]
            },
            "scopeLogs": [{
              "logRecords": [{
                "timeUnixNano": "1726000000000000000",
                "severityNumber": 17,
                "severityText": "ERROR",
                "body": {"stringValue": "Payment processing failed for order 42"},
                "traceId": "abc123",
                "spanId": "span456"
              }]
            }]
          }]
        }
        """;

    @Test
    void testParsesLogRecord() throws Exception {
        List<OtlpLogParser.ParsedLog> logs = parser.parseOtlp(LOG_PAYLOAD);
        assertEquals(1, logs.size());
        OtlpLogParser.ParsedLog log = logs.get(0);
        assertEquals("payment-service", log.getServiceName());
        assertEquals("prod", log.getEnvironment());
        assertEquals("ERROR", log.getSeverityText());
        assertEquals(17, log.getSeverityNumber());
        assertEquals("Payment processing failed for order 42", log.getBody());
        assertEquals("abc123", log.getTraceId());
        assertEquals("span456", log.getSpanId());
    }

    @Test
    void testTimestampParsed() throws Exception {
        List<OtlpLogParser.ParsedLog> logs = parser.parseOtlp(LOG_PAYLOAD);
        assertTrue(logs.get(0).getTimestamp() > 0, "Timestamp should be non-zero");
    }

    @Test
    void testEmptyPayload_returnsEmpty() throws Exception {
        List<OtlpLogParser.ParsedLog> logs = parser.parseOtlp("{\"resourceLogs\":[]}");
        assertTrue(logs.isEmpty());
    }
}
