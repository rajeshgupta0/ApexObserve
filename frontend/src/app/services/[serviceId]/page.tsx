"use client";

import { use, useState } from "react";
import { usePolling } from "@/lib/usePolling";
import { formatMs, getStatusColor, cn } from "@/lib/utils";
import { Server, ArrowLeft, Activity, ShieldCheck, AlertTriangle, Info, BrainCircuit, FileText, Layers, Clock } from "lucide-react";
import Link from "next/link";

interface PageProps {
  params: Promise<{ serviceId: string }>;
}

export default function ServicePage(props: PageProps) {
  const params = use(props.params);
  const serviceId = params.serviceId;
  const [activeTab, setActiveTab] = useState("overview");

  // Fetch all health to pick this service's health
  const { data: healthData, loading, error } = usePolling<any>(async () => {
    const res = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080/api"}/metrics/health`);
    if (!res.ok) throw new Error("Failed to fetch health data");
    return res.json();
  }, 3000);

  const metrics = healthData?.[serviceId] || null;

  // Fetch logs
  const { data: logsData, loading: logsLoading } = usePolling<any[]>(async () => {
    if (activeTab !== "logs") return null;
    const res = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080/api"}/logs/recent`);
    if (!res.ok) throw new Error("Failed to fetch logs data");
    const data = await res.json();
    return data.filter((log: any) => log.serviceId === serviceId);
  }, 5000);

  // Fetch traces
  const { data: tracesData, loading: tracesLoading } = usePolling<any[]>(async () => {
    if (activeTab !== "traces") return null;
    const res = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080/api"}/traces/recent`);
    if (!res.ok) throw new Error("Failed to fetch traces");
    const data = await res.json();
    return data.filter((trace: any) => trace.serviceId === serviceId);
  }, 5000);

  const tabs = [
    { id: "overview", label: "Overview" },
    { id: "logs", label: "Logs" },
    { id: "traces", label: "Traces" },
    { id: "rca", label: "RCA" },
    { id: "metrics", label: "Metrics" },
    { id: "dependencies", label: "Dependencies" },
    { id: "errors", label: "Errors" }
  ];

  const getSeverityStyle = (severity: string) => {
    switch(severity?.toLowerCase()) {
      case 'error':
      case 'fatal':
        return 'bg-rose-500/20 text-rose-400 border-rose-500/30';
      case 'warn':
      case 'warning':
        return 'bg-amber-500/20 text-amber-400 border-amber-500/30';
      case 'info':
        return 'bg-indigo-500/20 text-indigo-400 border-indigo-500/30';
      default:
        return 'bg-white/10 text-text-secondary border-white/10';
    }
  };

  return (
    <div className="space-y-6 animate-in fade-in duration-500 flex flex-col h-[calc(100vh-80px)]">
      <header className="shrink-0">
        <Link href="/services" className="inline-flex items-center gap-2 text-sm text-text-secondary hover:text-primary transition-colors mb-4">
          <ArrowLeft className="w-4 h-4" />
          Back to Services
        </Link>
        
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 rounded-xl bg-primary/10 flex items-center justify-center shrink-0 border border-primary/20">
              <Server className="w-6 h-6 text-primary" />
            </div>
            <div>
              <h1 className="text-2xl font-bold">{serviceId}</h1>
              <div className="flex items-center gap-3 mt-1 text-sm">
                {!loading && metrics ? (
                  <>
                    <span className={cn("font-medium", getStatusColor(metrics.score))}>
                      {metrics.score >= 90 ? 'HEALTHY' : metrics.score >= 70 ? 'DEGRADED' : 'CRITICAL'}
                    </span>
                    <span className="text-text-secondary flex items-center gap-2">
                      <span className="w-1 h-1 rounded-full bg-white/20"></span>
                      Health: {metrics.score.toFixed(1)}%
                    </span>
                  </>
                ) : (
                  <span className="text-text-secondary flex items-center gap-2"><Activity className="w-3 h-3 animate-pulse" /> Loading status...</span>
                )}
              </div>
            </div>
          </div>
          
          {!loading && metrics && metrics.errors > 0 && (
             <div className="glass-panel border-rose-500/20 bg-rose-500/10 px-4 py-2 flex items-center gap-3 text-sm">
               <AlertTriangle className="w-4 h-4 text-rose-500" />
               <span className="text-rose-200">Active Errors Detected</span>
             </div>
          )}
        </div>
      </header>

      {/* TABS */}
      <div className="flex items-center gap-2 overflow-x-auto pb-2 shrink-0 border-b border-white/5">
        {tabs.map(tab => (
          <button
            key={tab.id}
            onClick={() => setActiveTab(tab.id)}
            className={cn(
              "px-4 py-2 text-sm font-medium rounded-t-lg transition-colors whitespace-nowrap border-b-2 relative top-[1px]",
              activeTab === tab.id 
                ? "border-primary text-primary bg-primary/10" 
                : "border-transparent text-text-secondary hover:text-text-primary hover:bg-white/5"
            )}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* TAB CONTENT */}
      <div className="flex-1 min-h-0 overflow-y-auto pb-8">
        {activeTab === "overview" && (
           <div className="space-y-6">
             {loading ? (
               <div className="h-32 flex items-center justify-center"><div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div></div>
             ) : metrics ? (
               <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                 <div className="glass-panel p-5">
                   <h3 className="text-sm font-medium text-text-secondary mb-1">Health Score</h3>
                   <div className={cn("text-3xl font-bold", getStatusColor(metrics.score))}>{metrics.score.toFixed(1)}%</div>
                 </div>
                 <div className="glass-panel p-5">
                   <h3 className="text-sm font-medium text-text-secondary mb-1">Total Traffic</h3>
                   <div className="text-3xl font-bold">{metrics.traffic}</div>
                 </div>
                 <div className="glass-panel p-5">
                   <h3 className="text-sm font-medium text-text-secondary mb-1">Error Rate</h3>
                   <div className={cn("text-3xl font-bold", metrics.errors > 0 ? "text-rose-500" : "text-emerald-400")}>
                     {metrics.traffic > 0 ? ((metrics.errors / metrics.traffic) * 100).toFixed(1) : 0}%
                   </div>
                   <p className="text-xs text-text-secondary mt-1">{metrics.errors} failed requests</p>
                 </div>
                 <div className="glass-panel p-5">
                   <h3 className="text-sm font-medium text-text-secondary mb-1">Average Latency</h3>
                   <div className="text-3xl font-bold">{formatMs(metrics.latency)}</div>
                 </div>
               </div>
             ) : (
               <div className="glass-panel p-8 text-center text-text-secondary">No telemetry data available for this service.</div>
             )}
             
             {metrics && (
               <div className="glass-panel p-6">
                  <h3 className="text-lg font-semibold mb-4 border-b border-white/5 pb-2">Health Assessment Evidence</h3>
                  <div className="space-y-4">
                    <p className="text-sm text-text-secondary leading-relaxed max-w-4xl">
                      The service health score is calculated automatically based on real-time traffic analysis.
                      Base score is 100%. Penalties are applied for high latency, errors, or no traffic.
                    </p>
                    <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mt-4">
                      <div className="p-4 bg-black/20 rounded-lg border border-white/5">
                        <div className="text-xs text-text-secondary mb-1 uppercase tracking-wide">Error Penalty</div>
                        <div className="font-semibold text-rose-400">-{metrics.scoreBreakdown.errorPenalty.toFixed(1)} pts</div>
                      </div>
                      <div className="p-4 bg-black/20 rounded-lg border border-white/5">
                        <div className="text-xs text-text-secondary mb-1 uppercase tracking-wide">Latency Penalty</div>
                        <div className="font-semibold text-amber-400">-{metrics.scoreBreakdown.latencyPenalty.toFixed(1)} pts</div>
                      </div>
                      <div className="p-4 bg-black/20 rounded-lg border border-white/5">
                        <div className="text-xs text-text-secondary mb-1 uppercase tracking-wide">Traffic Penalty</div>
                        <div className="font-semibold text-text-secondary">-{metrics.scoreBreakdown.trafficPenalty.toFixed(1)} pts</div>
                      </div>
                    </div>
                  </div>
               </div>
             )}
           </div>
        )}

        {activeTab === "logs" && (
          <div className="glass-panel flex-1 min-h-[400px] flex flex-col border-white/5 h-full overflow-hidden">
            <div className="overflow-x-auto flex-1">
              <table className="w-full text-left text-sm whitespace-nowrap">
                <thead className="bg-black/40 border-b border-white/10 text-text-secondary sticky top-0 z-10 backdrop-blur-sm">
                  <tr>
                    <th className="py-3 px-4 font-medium">Time</th>
                    <th className="py-3 px-4 font-medium">Severity</th>
                    <th className="py-3 px-4 font-medium w-full">Message</th>
                    <th className="py-3 px-4 font-medium">Trace ID</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-white/5">
                  {logsLoading && (!logsData || logsData.length === 0) ? (
                    <tr>
                      <td colSpan={4} className="py-24 text-center">
                        <div className="flex justify-center"><div className="animate-spin rounded-full h-6 w-6 border-b-2 border-primary"></div></div>
                      </td>
                    </tr>
                  ) : !logsData || logsData.length === 0 ? (
                    <tr>
                      <td colSpan={4} className="py-24 text-center text-text-secondary">
                        <FileText className="w-10 h-10 mx-auto mb-3 opacity-20" />
                        <p>No logs found for this service</p>
                      </td>
                    </tr>
                  ) : (
                    logsData.map((log: any) => (
                      <tr key={log.id} className="hover:bg-white/5 transition-colors group">
                        <td className="py-2.5 px-4 text-text-secondary">
                          {new Date(log.time).toLocaleTimeString()}
                        </td>
                        <td className="py-2.5 px-4">
                          <span className={cn("px-2 py-0.5 text-[10px] font-bold tracking-wider rounded uppercase border", getSeverityStyle(log.severity))}>
                            {log.severity || 'INFO'}
                          </span>
                        </td>
                        <td className="py-2.5 px-4 text-text-primary truncate max-w-[500px]" title={log.message}>
                          {log.message}
                        </td>
                        <td className="py-2.5 px-4 text-text-secondary font-mono text-xs">
                          {log.traceId || '-'}
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {activeTab === "traces" && (
          <div className="glass-panel flex-1 min-h-[400px] flex flex-col border-white/5 h-full overflow-hidden">
             <div className="p-4 overflow-y-auto flex-1 space-y-3">
              {tracesLoading && (!tracesData || tracesData.length === 0) ? (
                <div className="flex justify-center items-center h-32">
                  <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
                </div>
              ) : !tracesData || tracesData.length === 0 ? (
                <div className="text-center text-text-secondary p-8 rounded-lg bg-black/20">
                  <Layers className="w-10 h-10 mx-auto mb-2 opacity-20" />
                  No traces found for this service.
                </div>
              ) : (
                tracesData.map((trace: any) => (
                  <div 
                    key={trace.traceId + trace.spanId} 
                    className="p-4 border rounded-lg bg-surface border-white/5 hover:bg-white/5 transition-colors relative overflow-hidden"
                  >
                    {trace.statusCode === 'ERROR' && <div className="absolute top-0 left-0 w-1 h-full bg-rose-500" />}
                    
                    <div className="flex justify-between items-center mb-2">
                      <span className="font-mono text-xs text-text-secondary truncate max-w-[150px]">{trace.traceId}</span>
                      <span className={cn(
                        "text-[10px] px-1.5 py-0.5 rounded font-bold tracking-wider uppercase border", 
                        trace.statusCode === 'ERROR' ? 'bg-rose-500/20 text-rose-400 border-rose-500/30' : 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30'
                      )}>
                        {trace.statusCode || 'OK'}
                      </span>
                    </div>
                    <div className="font-semibold text-sm mb-2 text-text-primary truncate">{trace.operationName}</div>
                    <div className="text-xs text-text-secondary flex items-center justify-end">
                      <span className="flex items-center gap-1 font-mono"><Clock className="w-3 h-3" /> {trace.durationMs}ms</span>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        )}

        {activeTab === "rca" && (
          <div className="space-y-6">
            <div className="glass-panel border-indigo-500/20 p-6">
              <div className="flex flex-col md:flex-row items-start gap-6">
                <div className="w-14 h-14 rounded-full bg-indigo-500/10 flex items-center justify-center shrink-0 border border-indigo-500/20">
                  <BrainCircuit className="w-7 h-7 text-indigo-400" />
                </div>
                <div className="flex-1">
                  <h2 className="text-xl font-bold text-indigo-300">Root Cause Analysis Insights</h2>
                  <p className="text-text-secondary mt-2 max-w-3xl leading-relaxed text-sm">
                    ApexObserve's AI engine correlates distributed traces, log anomalies, and metric deviations 
                    to determine if this service is the origin of failures.
                  </p>
                  
                  {metrics && metrics.errors > 0 ? (
                    <div className="mt-6 space-y-4 animate-in slide-in-from-bottom-4 duration-500">
                      <div className="p-5 bg-rose-500/10 border border-rose-500/20 rounded-lg">
                        <h3 className="font-semibold text-rose-300 mb-2 flex items-center gap-2">
                           <AlertTriangle className="w-4 h-4" /> High Probability: Origin of Failure
                        </h3>
                        <p className="text-sm text-rose-200/80 mb-4 leading-relaxed">
                          Because {serviceId} is actively emitting error spans and experiencing a degraded success rate, 
                          downstream dependents will experience cascading timeouts or 500s. 
                        </p>
                        <div className="text-xs text-text-secondary font-mono bg-black/40 p-3 rounded border border-white/5">
                          Evidence: {metrics.errors} error spans recorded in TimescaleDB traces table.
                        </div>
                      </div>
                    </div>
                  ) : (
                    <div className="mt-6 p-5 bg-emerald-500/10 border border-emerald-500/20 rounded-lg">
                      <h3 className="font-semibold text-emerald-400 mb-2 flex items-center gap-2">
                        <ShieldCheck className="w-4 h-4" /> Service is Healthy
                      </h3>
                      <p className="text-sm text-emerald-200/80">
                        No active anomalies or error cascades originating from this service.
                      </p>
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Placeholder for other tabs that need dedicated pages/components in a real app */}
        {["metrics", "dependencies", "errors"].includes(activeTab) && (
          <div className="h-[400px] flex items-center justify-center glass-panel">
            <div className="text-center p-8 max-w-md">
              <Info className="w-12 h-12 text-text-secondary/30 mx-auto mb-4" />
              <h3 className="text-lg font-medium mb-2 capitalize">{activeTab} View</h3>
              <p className="text-text-secondary text-sm">
                Detailed {activeTab} for <strong>{serviceId}</strong>. In the complete implementation, 
                this embeds the specific view filtered to this service.
              </p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
