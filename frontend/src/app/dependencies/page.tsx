"use client";

import { useEffect, useState, useMemo } from "react";
import ReactFlow, {
  Controls,
  Background,
  useNodesState,
  useEdgesState,
  Handle,
  Position,
  MarkerType,
  Panel
} from "reactflow";
import "reactflow/dist/style.css";
import { Server, Activity, AlertTriangle, ShieldCheck, Zap } from "lucide-react";
import { formatMs, cn } from "@/lib/utils";
import { usePolling } from "@/lib/usePolling";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { 
  DependencyGraphResponse, 
  ServiceHealthMetrics,
  NormalizedDependencyGraph
} from "@/types/dependency";

// CUSTOM NODE
const ServiceNode = ({ data }: any) => {
  const isHealthy = data.health >= 90;
  const isDegraded = data.health >= 70 && data.health < 90;
  const isCritical = data.health < 70;
  
  // Show unknown state if health is explicitly 0 and there's no traffic (e.g. backend returning no metrics)
  const isUnknown = data.health === 0 && data.traffic === 0;

  return (
    <div className={cn(
      "px-4 py-3 rounded-lg border-2 shadow-xl bg-surface/95 backdrop-blur min-w-[180px] cursor-pointer hover:ring-2 ring-primary/50 transition-all",
      isUnknown ? "border-white/20" : isHealthy ? "border-emerald-500/50" : isDegraded ? "border-amber-500/50" : "border-rose-500/50"
    )}>
      <Handle type="target" position={Position.Top} className="!bg-text-secondary w-3 h-3" />
      
      <div className="flex items-center justify-between mb-2">
        <div className="flex items-center gap-2">
          <Server className={cn("w-4 h-4", isUnknown ? "text-text-secondary" : isHealthy ? "text-emerald-400" : isCritical ? "text-rose-500" : "text-amber-400")} />
          <strong className="text-sm font-semibold text-text-primary">{data.label}</strong>
        </div>
      </div>
      
      <div className="grid grid-cols-2 gap-2 text-[10px] uppercase tracking-wider text-text-secondary mt-2 pt-2 border-t border-white/10">
        <div>
          <div>Health</div>
          <div className={cn("font-bold text-xs mt-0.5", isUnknown ? "text-text-secondary" : isHealthy ? "text-emerald-400" : isCritical ? "text-rose-400" : "text-amber-400")}>
            {isUnknown ? "---" : `${data.health?.toFixed(0)}%`}
          </div>
        </div>
        <div>
          <div>Errors</div>
          <div className={cn("font-bold text-xs mt-0.5", data.errors > 0 ? "text-rose-400" : "text-text-primary")}>
            {isUnknown ? "---" : (data.errors || 0)}
          </div>
        </div>
        <div>
          <div>Traffic</div>
          <div className="font-bold text-xs mt-0.5 text-text-primary">
            {isUnknown ? "---" : (data.traffic || 0)}
          </div>
        </div>
        <div>
          <div>Latency</div>
          <div className="font-bold text-xs mt-0.5 text-text-primary">
            {isUnknown ? "---" : (data.latency ? formatMs(data.latency) : "0ms")}
          </div>
        </div>
      </div>
      
      <Handle type="source" position={Position.Bottom} className="!bg-text-secondary w-3 h-3" />
    </div>
  );
};

// CUSTOM EDGE
const MetricEdge = ({
  id,
  sourceX,
  sourceY,
  targetX,
  targetY,
  style = {},
  markerEnd,
  data
}: any) => {
  const edgePath = `M ${sourceX} ${sourceY} C ${sourceX} ${(sourceY + targetY) / 2} ${targetX} ${(sourceY + targetY) / 2} ${targetX} ${targetY}`;
  
  return (
    <>
      <path id={id} style={style} className="react-flow__edge-path group-hover:stroke-primary" d={edgePath} markerEnd={markerEnd} />
      {data && data.calls > 0 && (
        <foreignObject
          width={120}
          height={40}
          x={(sourceX + targetX) / 2 - 60}
          y={(sourceY + targetY) / 2 - 20}
          className="overflow-visible"
        >
          <div className="flex flex-col items-center justify-center bg-surface/95 border border-white/10 rounded px-2 py-1 text-[10px] shadow-sm hover:border-primary/50 cursor-pointer transition-colors">
            <span className="font-semibold text-primary">{data.calls} reqs</span>
            {data.avgLatency ? <span className="text-text-secondary">{formatMs(data.avgLatency)}</span> : null}
          </div>
        </foreignObject>
      )}
    </>
  );
};

const nodeTypes = { serviceNode: ServiceNode };
const edgeTypes = { metricEdge: MetricEdge };

// HIERARCHICAL LAYOUT GENERATOR (BFS based)
function generateLayout(edges: any[], nodeIds: Set<string>) {
  const positions: Record<string, {x: number, y: number}> = {};
  
  // Build adjacency list & indegree map
  const adj: Record<string, string[]> = {};
  const inDegree: Record<string, number> = {};
  nodeIds.forEach(id => {
    adj[id] = [];
    inDegree[id] = 0;
  });
  
  edges.forEach(e => {
    if (adj[e.source] && inDegree[e.target] !== undefined) {
      adj[e.source].push(e.target);
      inDegree[e.target]++;
    }
  });

  // Find root nodes (inDegree === 0)
  let roots = Object.keys(inDegree).filter(id => inDegree[id] === 0);
  if (roots.length === 0 && nodeIds.size > 0) {
    // Fallback for circular graphs
    roots = [Array.from(nodeIds)[0]];
  }

  // BFS to assign levels
  const levels: Record<string, number> = {};
  const queue: {id: string, level: number}[] = roots.map(id => ({id, level: 0}));
  const visited = new Set<string>();

  while (queue.length > 0) {
    const {id, level} = queue.shift()!;
    if (visited.has(id)) continue;
    
    visited.add(id);
    levels[id] = Math.max(levels[id] || 0, level);

    adj[id]?.forEach(neighbor => {
      queue.push({id: neighbor, level: level + 1});
    });
  }
  
  // Group by level
  const levelGroups: Record<number, string[]> = {};
  nodeIds.forEach(id => {
    const lvl = levels[id] || 0;
    if (!levelGroups[lvl]) levelGroups[lvl] = [];
    levelGroups[lvl].push(id);
  });

  // Assign X/Y coordinates based on level grouping
  const NODE_WIDTH = 250;
  const LEVEL_HEIGHT = 200;

  Object.entries(levelGroups).forEach(([levelStr, nodesInLevel]) => {
    const level = parseInt(levelStr);
    const totalWidth = nodesInLevel.length * NODE_WIDTH;
    const startX = -(totalWidth / 2) + (NODE_WIDTH / 2);
    
    nodesInLevel.forEach((id, index) => {
      positions[id] = {
        x: startX + (index * NODE_WIDTH),
        y: level * LEVEL_HEIGHT
      };
    });
  });

  return positions;
}

export default function DependenciesPage() {
  const router = useRouter();
  const [nodes, setNodes, onNodesChange] = useNodesState([]);
  const [edges, setEdges, onEdgesChange] = useEdgesState([]);

  const fetchGraphData = async () => {
    const [graphRes, healthRes] = await Promise.all([
      fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080/api"}/dependencies/graph`).catch(() => null),
      fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080/api"}/metrics/health`).catch(() => null)
    ]);
    
    if (!graphRes || !graphRes.ok) throw new Error("Failed to fetch dependency graph data");
    
    const graphData: DependencyGraphResponse = await graphRes.json();
    const healthData: Record<string, ServiceHealthMetrics> = (healthRes && healthRes.ok) ? await healthRes.json() : {};
    
    return { graphData, healthData };
  };

  const { data, loading, error } = usePolling<{graphData: DependencyGraphResponse, healthData: Record<string, ServiceHealthMetrics>}>(fetchGraphData, 5000);

  // Normalize API Response & Compute Graph Layout
  useEffect(() => {
    if (data) {
      const { graphData, healthData } = data;
      const rawEdges = graphData.edges || [];
      
      // Safely extract unique nodes from edges (API contract normalization)
      const uniqueNodeIds = new Set<string>();
      rawEdges.forEach(edge => {
        if (edge.source) uniqueNodeIds.add(edge.source);
        if (edge.target) uniqueNodeIds.add(edge.target);
      });

      // Calculate auto-layout positions
      const positions = generateLayout(rawEdges, uniqueNodeIds);

      // Create strictly typed nodes
      const newNodes = Array.from(uniqueNodeIds).map(id => {
        const metrics = healthData[id] || { score: 0, errors: 0, traffic: 0, latency: 0 };
        return {
          id,
          type: 'serviceNode',
          position: positions[id] || { x: 0, y: 0 },
          data: { 
            label: id,
            health: metrics.score,
            errors: metrics.errors,
            traffic: metrics.traffic,
            latency: metrics.latency
          }
        };
      });

      // Create strictly typed edges
      const newEdges = rawEdges.map((edge, i) => ({
        id: `e-${edge.source}-${edge.target}-${i}`,
        source: edge.source,
        target: edge.target,
        type: 'metricEdge',
        data: {
          calls: edge.callCount || 0,
          avgLatency: edge.avgLatency || 0
        },
        animated: (edge.callCount || 0) > 0,
        style: { stroke: 'rgba(255,255,255,0.25)', strokeWidth: 2 },
        markerEnd: { type: MarkerType.ArrowClosed, color: 'rgba(255,255,255,0.25)' }
      }));

      setNodes(newNodes);
      setEdges(newEdges);
    }
  }, [data, setNodes, setEdges]);

  // Compute System Impact Metrics
  const systemImpact = useMemo(() => {
    if (!data || !nodes.length) return null;
    let healthy = 0, degraded = 0, critical = 0;
    let maxErrors = -1;
    let mostImpactfulService = null;

    nodes.forEach(n => {
      const h = n.data.health;
      if (h >= 90) healthy++;
      else if (h >= 70) degraded++;
      else if (h > 0 || (h === 0 && n.data.traffic > 0)) critical++;

      if (n.data.errors > maxErrors) {
        maxErrors = n.data.errors;
        mostImpactfulService = n.data.label;
      }
    });

    return { total: nodes.length, healthy, degraded, critical, maxErrors, mostImpactfulService };
  }, [nodes, data]);

  const onNodeClick = (_: React.MouseEvent, node: any) => {
    router.push(`/services/${node.id}`);
  };

  return (
    <div className="h-[calc(100vh-80px)] flex flex-col animate-in fade-in duration-500">
      <header className="mb-6 shrink-0 flex flex-col lg:flex-row lg:items-start justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold">Service Dependencies</h1>
          <p className="text-text-secondary mt-1">Real-time topology and blast radius mapped from distributed traces.</p>
          
          <div className="mt-3 text-xs bg-primary/10 border border-primary/20 text-primary px-3 py-2 rounded-lg inline-block">
            <strong>How to read this map:</strong> Each box is a service. Arrows indicate communication flow. A degraded upstream service can cause problems downstream. Click any service to investigate.
          </div>
        </div>

        {systemImpact && systemImpact.total > 0 && (
          <div className="glass-panel p-4 flex gap-6 text-sm">
             <div>
               <div className="text-text-secondary mb-1">System Health</div>
               <div className="flex items-center gap-3">
                 <span className="flex items-center gap-1 text-emerald-400"><ShieldCheck className="w-4 h-4"/> {systemImpact.healthy}</span>
                 <span className="flex items-center gap-1 text-amber-400"><Activity className="w-4 h-4"/> {systemImpact.degraded}</span>
                 <span className="flex items-center gap-1 text-rose-400"><AlertTriangle className="w-4 h-4"/> {systemImpact.critical}</span>
               </div>
             </div>
             <div className="border-l border-white/10 pl-6">
               <div className="text-text-secondary mb-1">Most Impactful Service</div>
               {systemImpact.maxErrors > 0 ? (
                 <div className="flex items-center gap-2 text-rose-400 font-semibold">
                   {systemImpact.mostImpactfulService}
                   <span className="text-xs text-rose-500/80 font-normal">({systemImpact.maxErrors} errors)</span>
                 </div>
               ) : (
                 <div className="text-text-primary">No active anomalies</div>
               )}
             </div>
             <div className="border-l border-white/10 pl-6 flex items-center">
               <Link href="/troubleshooting/blast-radius" className="bg-white/5 hover:bg-white/10 border border-white/10 px-4 py-2 rounded-lg flex items-center gap-2 transition-colors">
                 <Zap className="w-4 h-4 text-amber-400" />
                 View Blast Radius
               </Link>
             </div>
          </div>
        )}
      </header>
      
      {error && (
        <div className="p-4 bg-rose-500/10 border border-rose-500/20 text-rose-400 rounded-lg mb-4 shrink-0 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <AlertTriangle className="w-5 h-5" />
            Unable to load dependency data from the backend API.
          </div>
          <button onClick={() => window.location.reload()} className="px-3 py-1 bg-white/5 hover:bg-white/10 rounded text-sm transition-colors">Retry</button>
        </div>
      )}

      <div className="flex-1 glass-panel overflow-hidden relative rounded-xl border border-white/10 shadow-2xl">
        {loading && nodes.length === 0 ? (
          <div className="absolute inset-0 flex flex-col items-center justify-center gap-4 text-text-secondary">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
            Loading Dependency Graph...
          </div>
        ) : !loading && !error && nodes.length === 0 ? (
           <div className="absolute inset-0 flex flex-col items-center justify-center gap-3 text-text-secondary">
             <Activity className="w-12 h-12 opacity-20" />
             <p className="font-medium">No dependency relationships detected yet.</p>
             <p className="text-sm opacity-60">Dependency visualization will appear after services exchange traffic.</p>
           </div>
        ) : (
          <ReactFlow
            nodes={nodes}
            edges={edges}
            onNodesChange={onNodesChange}
            onEdgesChange={onEdgesChange}
            onNodeClick={onNodeClick}
            nodeTypes={nodeTypes}
            edgeTypes={edgeTypes}
            fitView
            className="bg-black/40"
            minZoom={0.1}
            maxZoom={1.5}
          >
            <Background color="#ffffff" gap={32} size={1} className="opacity-5" />
            <Controls className="!bg-surface !border-white/10 !text-text-primary !shadow-xl [&>button]:!border-white/5 [&>button]:hover:!bg-white/10 !fill-text-primary" />
            
            <Panel position="bottom-left" className="glass-panel p-4 m-4 text-xs shadow-xl border-white/10">
              <h4 className="font-semibold mb-3 border-b border-white/10 pb-2">Legend</h4>
              <div className="space-y-3">
                <div className="flex items-center gap-2 text-text-secondary">
                  <div className="w-3 h-3 rounded-full bg-emerald-500 shadow-[0_0_8px_rgba(16,185,129,0.5)]" /> Healthy (&ge; 90%)
                </div>
                <div className="flex items-center gap-2 text-text-secondary">
                  <div className="w-3 h-3 rounded-full bg-amber-500 shadow-[0_0_8px_rgba(245,158,11,0.5)]" /> Degraded (&ge; 70%)
                </div>
                <div className="flex items-center gap-2 text-text-secondary">
                  <div className="w-3 h-3 rounded-full bg-rose-500 shadow-[0_0_8px_rgba(244,63,94,0.5)]" /> Critical (&lt; 70%)
                </div>
                <div className="flex items-center gap-2 text-text-secondary">
                  <div className="w-3 h-3 rounded-full border border-white/20 bg-transparent" /> Unknown State
                </div>
                <div className="flex items-center gap-2 mt-4 pt-3 border-t border-white/10 text-text-secondary">
                  <div className="w-8 h-px bg-white/20 relative shadow-[0_0_4px_rgba(255,255,255,0.3)]">
                    <div className="absolute right-0 top-1/2 -translate-y-1/2 w-2 h-2 border-r-2 border-b-2 border-white/20 -rotate-45" />
                  </div>
                  Live Request Traffic
                </div>
              </div>
            </Panel>
          </ReactFlow>
        )}
      </div>
    </div>
  );
}
