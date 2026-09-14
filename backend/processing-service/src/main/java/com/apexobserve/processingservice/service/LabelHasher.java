package com.apexobserve.processingservice.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class LabelHasher {
    
    public static String hashLabels(String canonicalLabels) throws Exception {
        if (canonicalLabels == null) {
            canonicalLabels = "";
        }
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedhash = digest.digest(canonicalLabels.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
        for (byte b : encodedhash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
    
    public static String toCanonicalString(Map<String, String> labels) {
        if (labels == null || labels.isEmpty()) {
            return "";
        }
        // Force canonical ordering using TreeMap
        TreeMap<String, String> sorted = new TreeMap<>(labels);
        return sorted.entrySet().stream()
                .map(e -> escape(e.getKey()) + ":" + escape(e.getValue()))
                .collect(Collectors.joining(","));
    }
    
    private static String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace(":", "\\:").replace(",", "\\,");
    }
}
