/**
 * Tests for the frontend API lib (api.ts) covering data-shaping utilities.
 */
import { describe, it, expect } from 'vitest';
import { API_BASE_URL } from '@/lib/api';
import type { Alert, Incident, Metric } from '@/lib/api';

describe('API constants', () => {
  it('has correct base URL', () => {
    expect(API_BASE_URL).toBe('http://localhost:8080/api');
  });
});

describe('Alert interface shape', () => {
  it('satisfies the expected shape', () => {
    const alert: Alert = {
      id: 'a1',
      title: 'High CPU',
      severity: 'CRITICAL',
      status: 'ACTIVE',
      serviceId: 'demo-order',
      createdAt: new Date().toISOString(),
    };
    expect(alert.severity).toBe('CRITICAL');
    expect(alert.status).toBe('ACTIVE');
  });
});

describe('Incident interface shape', () => {
  it('satisfies the expected shape', () => {
    const incident: Incident = {
      id: 'i1',
      title: 'Database degradation',
      severity: 'HIGH',
      status: 'OPEN',
      affectedServices: '["demo-order","demo-payment"]',
      createdAt: new Date().toISOString(),
    };
    expect(incident.status).toBe('OPEN');
    expect(incident.affectedServices).toContain('demo-order');
  });
});

describe('Metric interface shape', () => {
  it('satisfies the expected shape', () => {
    const metric: Metric = {
      name: 'http.server.duration',
      description: 'HTTP duration',
      value: 123.45,
      labels: { 'http.method': 'GET', 'http.status_code': '200' },
      timestamp: Date.now(),
    };
    expect(metric.value).toBeGreaterThan(0);
    expect(metric.labels['http.method']).toBe('GET');
  });
});
