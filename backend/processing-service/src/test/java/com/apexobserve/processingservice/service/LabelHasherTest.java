package com.apexobserve.processingservice.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.TreeMap;

class LabelHasherTest {

    @Test
    void testDeterminism() throws Exception {
        String hash1 = LabelHasher.hashLabels("env:prod,region:us-east");
        String hash2 = LabelHasher.hashLabels("env:prod,region:us-east");
        assertEquals(hash1, hash2);
    }

    @Test
    void testOrdering() throws Exception {
        // Labels must be sorted canonically before hashing in real use.
        // Assuming canonical representation is sorted:
        Map<String, String> labels1 = new TreeMap<>();
        labels1.put("env", "prod");
        labels1.put("region", "us-east");

        Map<String, String> labels2 = new TreeMap<>();
        labels2.put("region", "us-east");
        labels2.put("env", "prod");

        String canonical1 = LabelHasher.toCanonicalString(labels1);
        String canonical2 = LabelHasher.toCanonicalString(labels2);
        
        assertEquals(canonical1, canonical2);
        assertEquals(LabelHasher.hashLabels(canonical1), LabelHasher.hashLabels(canonical2));
    }

    @Test
    void testDifferentLabels() throws Exception {
        String hash1 = LabelHasher.hashLabels("env:prod");
        String hash2 = LabelHasher.hashLabels("env:dev");
        assertNotEquals(hash1, hash2);
    }

    @Test
    void testEmptyLabels() throws Exception {
        String hash1 = LabelHasher.hashLabels("");
        assertNotNull(hash1);
    }

    @Test
    void testDelimiterSafety() throws Exception {
        // "env:prod,region:us" vs "env:prod,reg:ion:us" (if colon was unescaped)
        Map<String, String> labels1 = new TreeMap<>();
        labels1.put("env", "prod");
        labels1.put("reg", "ion:us");
        
        Map<String, String> labels2 = new TreeMap<>();
        labels2.put("env", "prod");
        labels2.put("region", "us");

        String c1 = LabelHasher.toCanonicalString(labels1);
        String c2 = LabelHasher.toCanonicalString(labels2);
        
        assertNotEquals(c1, c2);
        assertNotEquals(LabelHasher.hashLabels(c1), LabelHasher.hashLabels(c2));
    }
}
