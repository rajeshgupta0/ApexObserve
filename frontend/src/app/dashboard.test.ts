/**
 * Tests for dashboard health-score computation logic (extracted for unit testing).
 */
import { describe, it, expect } from 'vitest';

// Mirror the health score calculation from MetricsController / Dashboard
function computeHealthScore(latency: number, errors: number): number {
  const score = 100.0 - errors * 10.0 - latency / 100.0;
  return Math.max(0.0, Math.min(100.0, score));
}

describe('computeHealthScore', () => {
  it('returns 100 for zero errors and zero latency', () => {
    expect(computeHealthScore(0, 0)).toBe(100);
  });

  it('decreases with more errors', () => {
    const low = computeHealthScore(0, 0);
    const high = computeHealthScore(0, 5);
    expect(high).toBeLessThan(low);
  });

  it('decreases with higher latency', () => {
    const low = computeHealthScore(100, 0);
    const high = computeHealthScore(5000, 0);
    expect(high).toBeLessThan(low);
  });

  it('clamps to 0 for extreme values', () => {
    expect(computeHealthScore(100000, 100)).toBe(0);
  });

  it('clamps to 100 for negative error values', () => {
    // Negative errors should still give max 100
    expect(computeHealthScore(0, -5)).toBe(100);
  });
});

// Mirror the alert severity badge class
function getAlertSeverityClass(severity: string): string {
  return severity === 'CRITICAL' ? 'bg-danger text-white' : 'bg-warning text-white';
}

describe('getAlertSeverityClass', () => {
  it('returns danger class for CRITICAL', () => {
    expect(getAlertSeverityClass('CRITICAL')).toContain('bg-danger');
  });
  it('returns warning class for HIGH', () => {
    expect(getAlertSeverityClass('HIGH')).toContain('bg-warning');
  });
});
