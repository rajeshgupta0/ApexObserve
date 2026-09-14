"use client";

import { useState } from "react";
import { usePolling } from "@/lib/usePolling";
import { Activity, Search, AlertTriangle, Layers, Clock, ArrowRight } from "lucide-react";
import { cn } from "@/lib/utils";

export default function TracesPage() {
  const { data: tracesData, loading, error } = usePolling<any[]>(async () => {
    const res = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080/api"}/traces/recent`);
    if (!res.ok) throw new Error("Failed to fetch traces");
    return res.json();
  }, 5000);

  const traces = tracesData || [];
  const [selectedTraceId, setSelectedTraceId] = useState<string | null>(null);
  const [spans, setSpans] = useState<any[]>([]);
  const [spansLoading, setSpansLoading] = useState(false);

  const loadSpans = async (traceId: string) => {
    setSelectedTraceId(traceId);
    setSpansLoading(true);
    try {
      const res = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080/api"}/traces/${traceId}`);
      if (!res.ok) throw new Error("Failed to fetch spans");
      const data = await res.json();
      setSpans(data);
    } catch (err) {
      console.error(err);
    } finally {
      setSpansLoading(false);
    }
  };

  return (
    <div className="space-y-6 animate-in fade-in duration-500 h-[calc(100vh-80px)] flex flex-col">
      <header className="shrink-0">
        <h1 className="text-3xl font-bold flex items-center gap-3">
          <Layers className="w-8 h-8 text-indigo-500" />
          Distributed Traces
        </h1>
        <p className="text-text-secondary mt-1">End-to-End Request Visibility across microservices</p>
      </header>

      {error && (
        <div className="glass-panel border-rose-500/30 p-4 bg-rose-500/5 text-rose-200 flex items-center gap-3 shrink-0">
          <AlertTriangle className="w-5 h-5 text-rose-500" />
          {error.message}
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 flex-1 min-h-0">
        <div className="lg:col-span-1 glass-panel flex flex-col h-full overflow-hidden">
          <div className="p-4 border-b border-white/5 shrink-0 flex items-center justify-between">
            <h2 className="font-semibold flex items-center gap-2">
              <Activity className="w-4 h-4 text-indigo-400" /> Recent Traces
            </h2>
          </div>
          
          <div className="p-4 overflow-y-auto flex-1 space-y-3">
            {loading && traces.length === 0 ? (
              <div className="flex justify-center items-center h-32">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-500"></div>
              </div>
            ) : traces.length === 0 ? (
              <div className="text-center text-text-secondary p-8 border border-white/5 rounded-lg bg-black/20">
                <Layers className="w-10 h-10 mx-auto mb-2 opacity-20" />
                No traces found in the last 24 hours.
              </div>
            ) : (
              traces.map((trace) => (
                <div 
                  key={trace.traceId + trace.spanId} 
                  className={cn(
                    "p-4 border rounded-lg cursor-pointer transition-colors relative overflow-hidden group",
                    selectedTraceId === trace.traceId ? 'bg-indigo-500/20 border-indigo-500/50' : 'bg-surface border-white/5 hover:bg-white/5'
                  )}
                  onClick={() => loadSpans(trace.traceId)}
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
                  <div className="text-xs text-text-secondary flex justify-between items-center mt-3 pt-3 border-t border-white/5">
                    <span className="font-medium text-indigo-300">{trace.serviceId}</span>
                    <span className="flex items-center gap-1 font-mono"><Clock className="w-3 h-3" /> {trace.durationMs}ms</span>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>

        <div className="lg:col-span-2 glass-panel flex flex-col h-full overflow-hidden relative">
          {!selectedTraceId ? (
            <div className="absolute inset-0 flex flex-col items-center justify-center text-text-secondary bg-black/10">
              <div className="w-16 h-16 rounded-full bg-white/5 flex items-center justify-center mb-4">
                 <Search className="w-8 h-8 opacity-50" />
              </div>
              <p className="text-lg font-medium">Select a trace to view its full span hierarchy.</p>
            </div>
          ) : (
            <div className="flex flex-col h-full">
              <div className="p-6 border-b border-white/5 shrink-0 bg-surface/50">
                <h2 className="text-xl font-bold mb-3 flex items-center gap-2"><Activity className="w-5 h-5 text-indigo-400" /> Trace Details</h2>
                <div className="flex flex-wrap items-center gap-x-6 gap-y-2 text-sm text-text-secondary">
                  <span className="flex items-center gap-2">ID: <span className="font-mono text-xs bg-black/40 px-2 py-1 rounded text-text-primary border border-white/10">{selectedTraceId}</span></span>
                  <span>Total Spans: <span className="font-semibold text-text-primary">{spans.length}</span></span>
                </div>
              </div>
              
              <div className="p-6 overflow-y-auto flex-1 space-y-4">
                {spansLoading ? (
                  <div className="flex justify-center items-center h-32">
                    <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-500"></div>
                  </div>
                ) : spans.length === 0 ? (
                  <div className="text-center text-text-secondary py-12">No spans found for this trace.</div>
                ) : (
                  <div className="space-y-3 relative pl-4 border-l-2 border-indigo-500/20">
                    {spans.map((span, idx) => (
                      <div key={span.spanId} className="relative">
                        <div className="absolute -left-[21px] top-4 w-4 h-0.5 bg-indigo-500/20" />
                        <div className="p-4 bg-surface rounded-lg border border-white/5 hover:border-white/10 transition-colors">
                          <div className="flex flex-col md:flex-row md:justify-between md:items-start gap-2 mb-3">
                            <div>
                              <span className="font-semibold text-sm text-text-primary">{span.operationName}</span>
                              <div className="flex items-center gap-2 mt-1">
                                <span className="text-xs font-medium text-indigo-300">{span.serviceId}</span>
                              </div>
                            </div>
                            <div className="flex items-center gap-4 text-xs text-text-secondary">
                              <span className="font-mono border border-white/10 bg-black/20 px-2 py-0.5 rounded">Span: {span.spanId}</span>
                              <span className="font-mono font-medium text-text-primary bg-white/5 px-2 py-0.5 rounded border border-white/5">{span.durationMs}ms</span>
                            </div>
                          </div>
                          
                          {span.attributes && (
                            <div className="mt-3 text-xs font-mono bg-black/40 p-3 rounded-md overflow-x-auto text-text-secondary border border-white/5">
                              {span.attributes}
                            </div>
                          )}
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
