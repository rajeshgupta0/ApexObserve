"use client";

import { usePolling } from "@/lib/usePolling";
import { AlertTriangle, Bug, Search } from "lucide-react";
import Link from "next/link";
import { formatMs } from "@/lib/utils";

export default function ErrorAnalysisPage() {
  const { data: healthData, loading } = usePolling<any>(async () => {
    const res = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080/api"}/metrics/health`);
    if (!res.ok) throw new Error("Failed to fetch health data");
    return res.json();
  }, 5000);

  const services = healthData ? Object.entries(healthData) : [];
  const failingServices = services.filter(([_, m]: [string, any]) => m.errors > 0);

  return (
    <div className="max-w-5xl mx-auto space-y-8 animate-in fade-in duration-500 py-8">
      <header>
        <h1 className="text-3xl font-bold flex items-center gap-3">
          <Bug className="w-8 h-8 text-rose-500" />
          Error Analysis
        </h1>
        <p className="text-text-secondary mt-2 max-w-2xl">
          What is wrong right now? Identify failing services and exact error counts to begin your investigation.
        </p>
      </header>

      <div className="glass-panel p-6 min-h-[300px]">
        <div className="relative mb-6">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-secondary" />
          <input 
            type="text" 
            placeholder="Search failing services..." 
            className="w-full bg-black/20 border border-white/10 rounded-lg pl-10 pr-4 py-2 text-sm focus:outline-none focus:border-rose-500/50"
          />
        </div>

        {loading ? (
           <div className="flex justify-center py-12"><div className="animate-spin rounded-full h-8 w-8 border-b-2 border-rose-500"></div></div>
        ) : failingServices.length > 0 ? (
          <div className="space-y-4">
            {failingServices.map(([id, m]: [string, any]) => (
              <div key={id} className="p-4 rounded-lg bg-rose-500/5 border border-rose-500/20 flex flex-col md:flex-row md:items-center justify-between gap-4">
                <div>
                  <h3 className="font-semibold text-lg flex items-center gap-2">
                    <AlertTriangle className="w-5 h-5 text-rose-500" />
                    {id}
                  </h3>
                  <p className="text-sm text-text-secondary mt-1">
                    Encountering a high volume of failed requests affecting upstream dependents.
                  </p>
                  <div className="flex items-center gap-4 mt-3 text-sm">
                    <span className="text-rose-400 font-mono bg-black/30 px-2 py-0.5 rounded">{m.errors} errors</span>
                    <span className="text-text-secondary">Latency: {formatMs(m.latency)}</span>
                  </div>
                </div>
                <Link 
                  href={`/services/${id}`}
                  className="px-4 py-2 bg-white/10 hover:bg-white/20 text-white rounded-lg text-sm font-medium transition-colors whitespace-nowrap text-center"
                >
                  Investigate Service
                </Link>
              </div>
            ))}
          </div>
        ) : (
          <div className="text-center py-12 text-text-secondary">
            <div className="w-16 h-16 rounded-full bg-emerald-500/10 flex items-center justify-center mx-auto mb-4">
               <AlertTriangle className="w-8 h-8 text-emerald-500" />
            </div>
            <h3 className="text-lg font-medium text-emerald-400 mb-1">No Active Errors</h3>
            <p>All monitored services are currently processing requests successfully.</p>
          </div>
        )}
      </div>
    </div>
  );
}
