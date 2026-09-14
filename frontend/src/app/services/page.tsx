"use client";

import { usePolling } from "@/lib/usePolling";
import { formatMs, getStatusBg, getStatusColor, cn } from "@/lib/utils";
import { Server, Activity, ArrowRight, ShieldCheck, AlertTriangle, Filter } from "lucide-react";
import Link from "next/link";
import { useState } from "react";

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

export default function ServicesPage() {
  const [filter, setFilter] = useState("all");

  const { data: healthData, loading, error } = usePolling<HealthData>(async () => {
    const res = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080/api"}/metrics/health`);
    if (!res.ok) throw new Error("Failed to fetch health data");
    return res.json();
  }, 5000);

  const services = healthData ? Object.entries(healthData) : [];
  
  const filteredServices = services.filter(([_, m]) => {
    if (filter === "healthy") return m.score >= 90;
    if (filter === "degraded") return m.score >= 70 && m.score < 90;
    if (filter === "critical") return m.score < 70;
    return true;
  });

  return (
    <div className="space-y-6 animate-in fade-in duration-500">
      <header className="flex flex-col md:flex-row md:items-end justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold">Services Directory</h1>
          <p className="text-text-secondary mt-1">Monitored microservices and infrastructure</p>
        </div>
        
        <div className="flex items-center gap-2">
          <div className="flex items-center gap-2 bg-black/20 border border-white/10 rounded-lg p-1">
            <button 
              onClick={() => setFilter("all")}
              className={cn("px-3 py-1.5 rounded-md text-sm font-medium transition-colors", filter === "all" ? "bg-white/10 text-white" : "text-text-secondary hover:text-white")}
            >
              All
            </button>
            <button 
              onClick={() => setFilter("healthy")}
              className={cn("px-3 py-1.5 rounded-md text-sm font-medium transition-colors", filter === "healthy" ? "bg-emerald-500/20 text-emerald-400" : "text-text-secondary hover:text-emerald-400")}
            >
              Healthy
            </button>
            <button 
              onClick={() => setFilter("critical")}
              className={cn("px-3 py-1.5 rounded-md text-sm font-medium transition-colors", filter === "critical" ? "bg-rose-500/20 text-rose-400" : "text-text-secondary hover:text-rose-400")}
            >
              Critical
            </button>
          </div>
        </div>
      </header>

      {error && (
        <div className="glass-panel border-rose-500/30 p-4 bg-rose-500/5 text-rose-200 flex items-center gap-3">
          <AlertTriangle className="w-5 h-5 text-rose-500" />
          {error.message}
        </div>
      )}

      <div className="glass-panel overflow-hidden border border-white/5">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="border-b border-white/5 text-xs uppercase tracking-wider text-text-secondary bg-black/20">
              <th className="px-6 py-4 font-medium">Service</th>
              <th className="px-6 py-4 font-medium">Status</th>
              <th className="px-6 py-4 font-medium text-right">Traffic</th>
              <th className="px-6 py-4 font-medium text-right">Latency</th>
              <th className="px-6 py-4 font-medium text-right">Errors</th>
              <th className="px-6 py-4 font-medium text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-white/5">
            {loading && services.length === 0 ? (
              <tr>
                <td colSpan={6} className="px-6 py-12 text-center text-text-secondary">
                  <div className="flex justify-center mb-4">
                    <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-primary"></div>
                  </div>
                  Loading services...
                </td>
              </tr>
            ) : filteredServices.length === 0 ? (
              <tr>
                <td colSpan={6} className="px-6 py-12 text-center text-text-secondary">
                  No services found matching the current filter.
                </td>
              </tr>
            ) : (
              filteredServices.map(([id, m]) => (
                <tr key={id} className="hover:bg-white-[0.02] transition-colors group">
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-3">
                      <Server className="w-4 h-4 text-primary opacity-70 group-hover:opacity-100 transition-opacity" />
                      <span className="font-semibold text-text-primary group-hover:text-primary transition-colors">{id}</span>
                    </div>
                  </td>
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-2">
                      <span className={cn("w-2 h-2 rounded-full", m.score >= 90 ? "bg-emerald-500" : m.score >= 70 ? "bg-amber-500" : "bg-rose-500")} />
                      <span className={cn("text-sm font-medium", getStatusColor(m.score))}>
                        {m.score >= 90 ? 'HEALTHY' : m.score >= 70 ? 'DEGRADED' : 'CRITICAL'}
                      </span>
                    </div>
                  </td>
                  <td className="px-6 py-4 text-right">
                    <span className="text-sm font-medium">{m.traffic}</span>
                  </td>
                  <td className="px-6 py-4 text-right">
                    <span className="text-sm">{formatMs(m.latency)}</span>
                  </td>
                  <td className="px-6 py-4 text-right">
                    <span className={cn("text-sm font-medium", m.errors > 0 ? "text-rose-400" : "text-text-secondary")}>
                      {m.errors}
                    </span>
                  </td>
                  <td className="px-6 py-4 text-right">
                    <Link 
                      href={`/services/${id}`}
                      className="inline-flex items-center gap-1 px-3 py-1.5 rounded bg-white/5 hover:bg-primary/20 text-text-secondary hover:text-primary transition-colors text-sm font-medium"
                    >
                      Investigate <ArrowRight className="w-3.5 h-3.5" />
                    </Link>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
