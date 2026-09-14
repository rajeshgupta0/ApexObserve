package com.apexobserve.queryservice.service;

import com.apexobserve.queryservice.repository.DependencyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RcaServiceTest {

    @Mock
    private DependencyRepository dependencyRepository;

    private RcaService rcaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        rcaService = new RcaService(dependencyRepository);
    }

    // Helpers to build Object[] rows as dependency repository returns
    private Object[] dep(String source, String target, long count) {
        return new Object[]{source, target, count};
    }

    @Test
    void testRca_noData_lowConfidence() {
        when(dependencyRepository.findServiceDependencies("default")).thenReturn(Collections.emptyList());
        Map<String, Object> result = rcaService.analyzeRootCause("default", "service-a");

        assertEquals("service-a", result.get("rootCause"));
        double conf = (double) result.get("confidence");
        assertTrue(conf < 0.50, "Confidence should be low when no data");
    }

    @Test
    void testRca_leafNode_selfIsRootCause() {
        // service-a is called by gateway, but calls nobody
        when(dependencyRepository.findServiceDependencies("default")).thenReturn(
            List.<Object[]>of(dep("gateway", "service-a", 100))
        );
        Map<String, Object> result = rcaService.analyzeRootCause("default", "service-a");

        assertEquals("service-a", result.get("rootCause"));
        double conf = (double) result.get("confidence");
        assertTrue(conf >= 0.70, "Leaf node should have high confidence in self-causation");
    }

    @Test
    void testRca_obviousSingleDownstream() {
        // service-a -> db (200 calls), service-a <- gateway
        when(dependencyRepository.findServiceDependencies("default")).thenReturn(
            List.<Object[]>of(dep("gateway", "service-a", 100), dep("service-a", "db", 200))
        );
        Map<String, Object> result = rcaService.analyzeRootCause("default", "service-a");

        assertEquals("db", result.get("rootCause"));
        double conf = (double) result.get("confidence");
        assertTrue(conf >= 0.90, "Single downstream candidate with 100% share should be high confidence");
    }

    @Test
    void testRca_multipleDownstreamCandidates_ranksByCallCount() {
        // service-a calls db (50 calls) and cache (200 calls)
        when(dependencyRepository.findServiceDependencies("default")).thenReturn(
            List.<Object[]>of(dep("service-a", "db", 50), dep("service-a", "cache", 200))
        );
        Map<String, Object> result = rcaService.analyzeRootCause("default", "service-a");

        // cache has 200/250 = 80% of traffic, should rank first
        assertEquals("cache", result.get("rootCause"));
        double conf = (double) result.get("confidence");
        assertTrue(conf >= 0.50 && conf <= 0.92);
    }

    @Test
    void testBlastRadius_linearChain() {
        // gateway -> order -> payment
        when(dependencyRepository.findServiceDependencies("default")).thenReturn(
            List.<Object[]>of(dep("gateway", "order", 100), dep("order", "payment", 80))
        );
        Map<String, Object> result = rcaService.analyzeBlastRadius("default", "payment");

        List<?> direct = (List<?>) result.get("directlyAffected");
        List<?> transitive = (List<?>) result.get("transitivelyAffected");

        assertTrue(direct.contains("order"), "order directly depends on payment");
        assertTrue(transitive.contains("gateway"), "gateway transitively depends on payment");
    }

    @Test
    void testBlastRadius_isolatedService() {
        when(dependencyRepository.findServiceDependencies("default")).thenReturn(
            List.<Object[]>of(dep("service-a", "service-b", 10))
        );
        Map<String, Object> result = rcaService.analyzeBlastRadius("default", "service-a");

        List<?> direct = (List<?>) result.get("directlyAffected");
        assertEquals(0, direct.size(), "service-a has no upstream callers");
        assertEquals("LOW", result.get("severity"));
    }

    @Test
    void testBlastRadius_cycle_noCrash() {
        // Circular: a->b->c->a
        when(dependencyRepository.findServiceDependencies("default")).thenReturn(
            List.<Object[]>of(dep("a", "b", 10), dep("b", "c", 10), dep("c", "a", 10))
        );
        // Should not throw StackOverflowError or infinite loop
        assertDoesNotThrow(() -> rcaService.analyzeBlastRadius("default", "b"));
    }

    @Test
    void testRca_confidence_bounded() {
        when(dependencyRepository.findServiceDependencies("default")).thenReturn(
            List.<Object[]>of(dep("service-a", "db", 999999))
        );
        Map<String, Object> result = rcaService.analyzeRootCause("default", "service-a");
        double conf = (double) result.get("confidence");
        assertTrue(conf <= 0.92, "Confidence must not exceed 0.92 cap");
        assertTrue(conf >= 0.0);
    }
}
