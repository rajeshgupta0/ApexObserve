"use client";

import { usePolling } from "@/lib/usePolling";
import { formatMs, getStatusBg, getStatusColor, cn } from "@/lib/utils";
import { Activity, Server, AlertTriangle, ShieldCheck, ArrowUpRight, ArrowDownRight, Clock, Target, Ghost, Siren } from "lucide-react";
import Link from "next/link";

interface ServiceMetrics {
  score: number;
  latency: number;
  errors: number;
  traffic: number;
  scoreBreakdown: any;
}

interface HealthData {
  [serviceId: string]: ServiceMetrics;
}

export default function Dashboard() {
  const fetchHealth = async () => {
    const res = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080/api"}/metrics/health`);
    if (!res.ok) throw new Error("Failed to fetch health data");
    return res.json() as Promise<HealthData>;
  };

  const { data: healthData, loading, error, lastUpdated } = usePolling<HealthData>(fetchHealth, 3000);

  // Computed KPIs
  const services = healthData ? Object.entries(healthData) : [];
  const totalServices = services.length;
  const healthyServices = services.filter(([_, m]) => m.score >= 90).length;
  const totalTraffic = services.reduce((acc, [_, m]) => acc + m.traffic, 0);
  const totalErrors = services.reduce((acc, [_, m]) => acc + m.errors, 0);
  const avgLatency = totalServices > 0 
    ? services.reduce((acc, [_, m]) => acc + m.latency, 0) / totalServices 
    : 0;
  
  const successRate = totalTraffic > 0 
    ? ((totalTraffic - totalErrors) / totalTraffic) * 100 
    : 100;

  // Active Problems are services with errors or latency > 500
  const activeProblems = services.filter(([_, m]) => m.errors > 0 || m.latency > 500);

  return (
    <div className="space-y-8 pb-12 animate-in fade-in duration-500">
      <header className="flex flex-col md:flex-row md:items-end justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold">Platform Overview</h1>
          <p className="text-text-secondary mt-1">Real-time system telemetry and health status</p>
        </div>
        
        {lastUpdated && (
          <div className="flex items-center gap-2 text-xs text-text-secondary">
            <Clock className="w-3.5 h-3.5" />
            <span>Updated {lastUpdated.toLocaleTimeString()}</span>
          </div>
        )}
      </header>

      {/* TOP KPI CARDS */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="glass-panel p-5 relative overflow-hidden">
          <div className="flex justify-between items-start mb-2">
            <h3 className="text-sm font-medium text-text-secondary">System Health</h3>
            <ShieldCheck className={cn("w-5 h-5", healthyServices === totalServices && totalServices > 0 ? "text-emerald-400" : "text-amber-400")} />
          </div>
          <div className="text-3xl font-bold">{totalServices > 0 ? `${healthyServices}/${totalServices}` : '-'}</div>
          <p className="text-xs text-text-secondary mt-1">Services Healthy</p>
          <div className="absolute -right-4 -bottom-4 opacity-5">
            <ShieldCheck className="w-24 h-24" />
          </div>
        </div>

        <div className="glass-panel p-5 relative overflow-hidden">
          <div className="flex justify-between items-start mb-2">
            <h3 className="text-sm font-medium text-text-secondary">Success Rate</h3>
            <Target className={cn("w-5 h-5", successRate > 99 ? "text-emerald-400" : successRate > 90 ? "text-amber-400" : "text-rose-500")} />
          </div>
          <div className="text-3xl font-bold">{totalTraffic > 0 ? `${successRate.toFixed(2)}%` : '-'}</div>
          <p className="text-xs text-text-secondary mt-1">{totalTraffic - totalErrors} / {totalTraffic} requests</p>
          <div className="absolute -right-4 -bottom-4 opacity-5">
            <Target className="w-24 h-24" />
          </div>
        </div>

        <div className="glass-panel p-5 relative overflow-hidden">
          <div className="flex justify-between items-start mb-2">
            <h3 className="text-sm font-medium text-text-secondary">Total Errors</h3>
            <AlertTriangle className={cn("w-5 h-5", totalErrors === 0 ? "text-emerald-400" : "text-rose-500")} />
          </div>
          <div className="text-3xl font-bold">{totalErrors}</div>
          <p className="text-xs text-text-secondary mt-1">Across all services</p>
          <div className="absolute -right-4 -bottom-4 opacity-5">
            <AlertTriangle className="w-24 h-24" />
          </div>
        </div>

        <div className="glass-panel p-5 relative overflow-hidden">
          <div className="flex justify-between items-start mb-2">
            <h3 className="text-sm font-medium text-text-secondary">Avg Latency</h3>
            <Activity className="w-5 h-5 text-primary" />
          </div>
          <div className="text-3xl font-bold">{totalServices > 0 ? formatMs(avgLatency) : '-'}</div>
          <p className="text-xs text-text-secondary mt-1">Global average</p>
          <div className="absolute -right-4 -bottom-4 opacity-5">
            <Activity className="w-24 h-24" />
          </div>
        </div>
      </div>

      {error && (
        <div className="glass-panel border-rose-500/30 p-6 flex items-start gap-4 bg-rose-500/5 text-rose-200">
          <AlertTriangle className="w-6 h-6 text-rose-500 shrink-0" />
          <div>
            <h3 className="font-semibold text-rose-400">Backend Connection Failed</h3>
            <p className="text-sm opacity-90 mt-1">{error.message}</p>
            <p className="text-xs opacity-75 mt-2">Ensure API Gateway is running at :8080</p>
          </div>
        </div>
      )}

      {/* ACTIVE PROBLEMS */}
      {!loading && !error && activeProblems.length > 0 && (
        <div className="space-y-4">
          <h2 className="text-lg font-semibold flex items-center gap-2">
            <Siren className="w-5 h-5 text-rose-500" />
            Active Problems
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {activeProblems.map(([id, m]) => (
              <div key={`prob-${id}`} className="glass-panel border-rose-500/20 bg-rose-500/5 p-4 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
                <div>
                  <div className="flex items-center gap-2 mb-1">
                    <span className="px-2 py-0.5 rounded text-xs font-semibold bg-rose-500/20 text-rose-400">
                      {m.score < 70 ? 'CRITICAL' : 'DEGRADED'}
                    </span>
                    <span className="font-semibold">{id}</span>
                  </div>
                  <p className="text-sm text-text-secondary">
                    {m.errors > 0 && `${m.errors} errors detected.`} 
                    {m.latency > 500 && ` Latency increased to ${formatMs(m.latency)}.`}
                  </p>
                </div>
                <Link 
                  href={`/services/${id}`}
                  className="px-4 py-2 rounded-lg bg-white/10 hover:bg-white/20 text-sm font-medium transition-colors whitespace-nowrap"
                >
                  Investigate
                </Link>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* SERVICE HEALTH GRID */}
      {!error && (
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-semibold">Service Health</h2>
            {loading && <div className="animate-spin w-4 h-4 border-2 border-primary border-t-transparent rounded-full" />}
          </div>

          {!loading && services.length === 0 ? (
            <div className="glass-panel p-12 text-center flex flex-col items-center">
              <Ghost className="w-12 h-12 text-text-secondary/50 mb-4" />
              <h3 className="text-lg font-medium mb-1">No Telemetry Available</h3>
              <p className="text-text-secondary text-sm">Waiting for incoming traffic or metrics from services.</p>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
              {services.map(([id, m]) => (
                <Link href={`/services/${id}`} key={id}>
                  <div className="glass-panel p-5 hover:border-primary/50 hover:bg-white-[0.02] transition-all cursor-pointer group h-full flex flex-col">
                    <div className="flex justify-between items-start mb-4">
                      <h3 className="font-semibold flex items-center gap-2 group-hover:text-primary transition-colors">
                        <Server className="w-4 h-4 text-text-secondary group-hover:text-primary transition-colors" />
                        {id}
                      </h3>
                      <div className={cn("px-2 py-0.5 rounded text-xs font-semibold border", getStatusBg(m.score), getStatusColor(m.score))}>
                        {m.score >= 90 ? 'HEALTHY' : m.score >= 70 ? 'DEGRADED' : 'CRITICAL'}
                      </div>
                    </div>
                    
                    <div className="flex-1 space-y-4">
                      <div>
                        <div className="flex justify-between text-sm mb-1.5">
                          <span className="text-text-secondary text-xs uppercase tracking-wider">Health Score</span>
                          <span className={cn("font-medium", getStatusColor(m.score))}>{m.score.toFixed(0)}%</span>
                        </div>
                        <div className="h-1.5 w-full bg-black/40 rounded-full overflow-hidden">
                          <div 
                            className={cn("h-full transition-all duration-1000", m.score >= 90 ? 'bg-emerald-500' : m.score >= 70 ? 'bg-amber-500' : 'bg-rose-500')}
                            style={{ width: `${m.score}%` }}
                          />
                        </div>
                      </div>
                      
                      <div className="grid grid-cols-3 gap-2 pt-3 border-t border-white/5">
                        <div>
                          <p className="text-[10px] uppercase tracking-wider text-text-secondary mb-0.5">Traffic</p>
                          <p className="font-semibold text-sm">{m.traffic}</p>
                        </div>
                        <div>
                          <p className="text-[10px] uppercase tracking-wider text-text-secondary mb-0.5">Latency</p>
                          <p className="font-semibold text-sm">{formatMs(m.latency)}</p>
                        </div>
                        <div>
                          <p className="text-[10px] uppercase tracking-wider text-text-secondary mb-0.5">Errors</p>
                          <p className={cn("font-semibold text-sm", m.errors > 0 ? "text-rose-400" : "")}>{m.errors}</p>
                        </div>
                      </div>
                    </div>
                  </div>
                </Link>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
