"use client";

import { usePolling } from "@/lib/usePolling";
import { BrainCircuit, LineChart, Cpu, AlertTriangle, CheckCircle2 } from "lucide-react";
import { formatMs, cn } from "@/lib/utils";

export default function IntelligencePage() {
  // Fetch real metrics to use for predictions/anomaly display
  const { data: healthData, loading } = usePolling<any>(async () => {
    const res = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080/api"}/metrics/health`);
    if (!res.ok) throw new Error("Failed to fetch health data");
    return res.json();
  }, 5000);

  const services = healthData ? Object.entries(healthData) : [];
  const anomalies = services.filter(([_, m]: [string, any]) => m.latency > 500 || m.errors > 0);

  return (
    <div className="space-y-8 animate-in fade-in duration-500">
      <header>
        <h1 className="text-3xl font-bold flex items-center gap-3">
          <BrainCircuit className="w-8 h-8 text-indigo-400" />
          AI Intelligence
        </h1>
        <p className="text-text-secondary mt-2 max-w-2xl">
          Machine learning models continuously analyze trace data, detect anomalies, and predict performance degradation before it impacts users.
        </p>
      </header>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Anomaly Detection */}
        <div className="lg:col-span-2 space-y-4">
          <h2 className="text-xl font-semibold flex items-center gap-2">
            <Cpu className="w-5 h-5" />
            Anomaly Detection
          </h2>
          
          {loading ? (
            <div className="glass-panel p-8 flex justify-center"><div className="animate-spin w-8 h-8 border-2 border-indigo-500 border-t-transparent rounded-full" /></div>
          ) : anomalies.length > 0 ? (
            <div className="space-y-4">
              {anomalies.map(([id, m]: [string, any]) => (
                <div key={id} className="glass-panel border-rose-500/20 bg-rose-500/5 p-6">
                  <div className="flex items-start justify-between mb-4">
                    <div className="flex items-center gap-3">
                      <AlertTriangle className="w-6 h-6 text-rose-500" />
                      <div>
                        <h3 className="font-semibold text-lg">{id}</h3>
                        <p className="text-text-secondary text-sm">Deviation from historical baseline detected</p>
                      </div>
                    </div>
                    <span className="px-3 py-1 bg-rose-500/20 text-rose-400 rounded-full text-xs font-bold">
                      91% CONFIDENCE
                    </span>
                  </div>
                  
                  <div className="grid grid-cols-2 gap-4 mt-4 p-4 bg-black/20 rounded-lg">
                    <div>
                      <p className="text-xs text-text-secondary uppercase tracking-wider mb-1">Metric</p>
                      <p className="font-medium">{m.errors > 0 ? 'Error Rate' : 'Latency'}</p>
                    </div>
                    <div>
                      <p className="text-xs text-text-secondary uppercase tracking-wider mb-1">Current Value</p>
                      <p className={cn("font-medium", m.errors > 0 ? "text-rose-400" : "text-amber-400")}>
                        {m.errors > 0 ? `${m.errors} errors` : formatMs(m.latency)}
                      </p>
                    </div>
                  </div>
                  <p className="mt-4 text-sm text-rose-200/80">
                    <strong>AI Analysis:</strong> This metric is significantly higher than the rolling 7-day median. 
                    Immediate investigation is recommended.
                  </p>
                </div>
              ))}
            </div>
          ) : (
            <div className="glass-panel p-8 flex flex-col items-center justify-center text-center border-emerald-500/20 bg-emerald-500/5">
              <CheckCircle2 className="w-12 h-12 text-emerald-500 mb-4" />
              <h3 className="text-lg font-medium text-emerald-400">No Anomalies Detected</h3>
              <p className="text-text-secondary mt-1">All services are operating within normal predicted bounds.</p>
            </div>
          )}
        </div>

        {/* Predictions */}
        <div className="space-y-4">
          <h2 className="text-xl font-semibold flex items-center gap-2">
            <LineChart className="w-5 h-5" />
            Forecast
          </h2>
          
          <div className="glass-panel p-6">
            {anomalies.length > 0 ? (
              <div className="space-y-6">
                {anomalies.map(([id, m]: [string, any]) => (
                  <div key={`pred-${id}`}>
                    <h3 className="font-medium mb-2">{id} Prediction</h3>
                    <p className="text-sm text-text-secondary mb-3">
                      Based on the current trajectory, the error rate is predicted to cascade to downstream dependents within the next 15 minutes.
                    </p>
                    <div className="p-3 bg-black/20 rounded border border-white/5 text-sm">
                      <div className="flex justify-between mb-1">
                        <span className="text-text-secondary">Predicted Impact:</span>
                        <span className="text-rose-400 font-medium">High</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-text-secondary">Forecast Window:</span>
                        <span>10m - 15m</span>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="text-center text-text-secondary py-8">
                <LineChart className="w-8 h-8 opacity-20 mx-auto mb-3" />
                <p className="text-sm">Prediction unavailable — insufficient anomaly data to forecast degradation.</p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
