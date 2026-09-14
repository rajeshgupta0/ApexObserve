export const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080/api';

export interface Metric {
  name: string;
  description: string;
  value: number;
  labels: Record<string, string>;
  timestamp: number;
}

export interface Alert {
  id: string;
  title: string;
  severity: string;
  status: string;
  serviceId: string;
  createdAt: string;
}

export interface Incident {
  id: string;
  title: string;
  severity: string;
  status: string;
  affectedServices: string;
  createdAt: string;
}

export const fetchMetrics = async (): Promise<Metric[]> => {
  const res = await fetch(`${API_BASE_URL}/metrics/history`);
  if (!res.ok) throw new Error('Failed to fetch metrics');
  return res.json();
};

export const fetchAlerts = async (): Promise<Alert[]> => {
  const res = await fetch(`${API_BASE_URL}/alerts`);
  if (!res.ok) throw new Error('Failed to fetch alerts');
  return res.json();
};

export const fetchIncidents = async (): Promise<Incident[]> => {
  const res = await fetch(`${API_BASE_URL}/incidents`);
  if (!res.ok) throw new Error('Failed to fetch incidents');
  return res.json();
};
