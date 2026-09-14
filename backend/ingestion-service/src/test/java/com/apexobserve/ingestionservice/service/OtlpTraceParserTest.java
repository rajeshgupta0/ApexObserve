package com.apexobserve.ingestionservice.service;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class OtlpTraceParserTest {

    private final OtlpTraceParser parser = new OtlpTraceParser();

    private static final String TRACE_PAYLOAD = """
        {
          "resourceSpans": [{
            "resource": {
              "attributes": [
                {"key": "service.name", "value": {"stringValue": "user-service"}},
                {"key": "deployment.environment", "value": {"stringValue": "staging"}}
              ]
            },
            "scopeSpans": [{
              "spans": [{
                "traceId": "trace-001",
                "spanId": "span-001",
                "parentSpanId": "span-parent-001",
                "name": "GET /users/{id}",
                "startTimeUnixNano": "1726000000000000000",
                "endTimeUnixNano":   "1726000000100000000",
                "status": {"code": 1}
              }]
            }]
          }]
        }
        """;

    @Test
    void testParsesSpan() throws Exception {
        List<OtlpTraceParser.ParsedSpan> spans = parser.parseOtlp(TRACE_PAYLOAD);
        assertEquals(1, spans.size());
        OtlpTraceParser.ParsedSpan span = spans.get(0);
        assertEquals("user-service", span.getServiceName());
        assertEquals("staging", span.getEnvironment());
        assertEquals("trace-001", span.getTraceId());
        assertEquals("span-001", span.getSpanId());
        assertEquals("span-parent-001", span.getParentSpanId());
        assertEquals("GET /users/{id}", span.getOperation());
    }

    @Test
    void testDurationCalculated() throws Exception {
        List<OtlpTraceParser.ParsedSpan> spans = parser.parseOtlp(TRACE_PAYLOAD);
        OtlpTraceParser.ParsedSpan span = spans.get(0);
        // endTime - startTime = 100ms in nanos
        assertTrue(span.getDuration() > 0, "Duration should be computed from start/end times");
    }

    @Test
    void testMissingParentSpanId_isNull() throws Exception {
        String noParent = TRACE_PAYLOAD.replace("\"parentSpanId\": \"span-parent-001\",", "");
        List<OtlpTraceParser.ParsedSpan> spans = parser.parseOtlp(noParent);
        assertNull(spans.get(0).getParentSpanId());
    }

    @Test
    void testEmptyPayload_returnsEmpty() throws Exception {
        List<OtlpTraceParser.ParsedSpan> spans = parser.parseOtlp("{\"resourceSpans\":[]}");
        assertTrue(spans.isEmpty());
    }
}
