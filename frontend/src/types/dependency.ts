export interface DependencyEdge {
  source: string;
  target: string;
  callCount?: number;
  avgLatency?: number;
  errorRate?: number;
}

export interface DependencyGraphResponse {
  edges?: DependencyEdge[];
  nodes?: string[];
}

export interface ServiceHealthMetrics {
  score: number;
  errors: number;
  traffic: number;
  latency: number;
  scoreBreakdown?: {
    errorPenalty: number;
    latencyPenalty: number;
    trafficPenalty: number;
  };
}

export interface DependencyNodeData {
  label: string;
  health: number;
  errors: number;
  traffic: number;
  latency: number;
}

export interface NormalizedDependencyGraph {
  nodes: {
    id: string;
    data: DependencyNodeData;
    position: { x: number; y: number };
  }[];
  edges: {
    id: string;
    source: string;
    target: string;
    calls: number;
    avgLatency: number;
  }[];
}
