"use client";

import { usePolling } from "@/lib/usePolling";
import { Search, Info, Target, ArrowDown } from "lucide-react";
import Link from "next/link";
import { formatMs, cn } from "@/lib/utils";

export default function BlastRadiusPage() {
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
          <Target className="w-8 h-8 text-amber-500" />
          Blast Radius Assessment
        </h1>
        <p className="text-text-secondary mt-2 max-w-2xl">
          What services are affected? Understand how downstream failures propagate up the stack to impact user-facing operations.
        </p>
      </header>

      <div className="glass-panel p-6 min-h-[300px]">
        {loading ? (
           <div className="flex justify-center py-12"><div className="animate-spin rounded-full h-8 w-8 border-b-2 border-amber-500"></div></div>
        ) : failingServices.length > 0 ? (
          <div className="space-y-8">
            {failingServices.map(([id, m]) => (
              <div key={id} className="p-6 rounded-lg bg-surface border border-white/5 relative overflow-hidden">
                <div className="absolute top-0 left-0 w-1 h-full bg-amber-500" />
                
                <h3 className="font-semibold text-lg flex items-center gap-2 mb-4">
                  Root Issue: <span className="text-amber-500">{id}</span>
                </h3>
                
                <div className="space-y-4">
                  <div className="p-4 bg-black/20 rounded-md border border-white/5">
                    <p className="text-sm font-medium mb-1 text-text-primary">Directly Affected (Upstream)</p>
                    <p className="text-xs text-text-secondary">Services calling {id} directly are experiencing 500s or timeouts.</p>
                    <div className="flex items-center gap-2 mt-3">
                      <span className="px-2 py-1 bg-amber-500/10 text-amber-400 rounded text-xs font-mono">demo-order</span>
                    </div>
                  </div>
                  
                  <div className="flex justify-center -my-2 relative z-10">
                    <div className="bg-surface p-1 rounded-full border border-white/10">
                      <ArrowDown className="w-4 h-4 text-text-secondary" />
                    </div>
                  </div>

                  <div className="p-4 bg-black/20 rounded-md border border-white/5">
                    <p className="text-sm font-medium mb-1 text-text-primary">Transitively Affected (User Facing)</p>
                    <p className="text-xs text-text-secondary">If {id} remains degraded, checkout operations via API Gateway will fail.</p>
                    <div className="flex items-center gap-2 mt-3">
                      <span className="px-2 py-1 bg-amber-500/10 text-amber-400 rounded text-xs font-mono">demo-api-gateway</span>
                    </div>
                  </div>
                </div>
                
                <div className="mt-6 pt-4 border-t border-white/5">
                  <Link 
                    href={`/dependencies`}
                    className="text-sm text-amber-400 hover:text-amber-300 font-medium transition-colors"
                  >
                    View full dependency graph &rarr;
                  </Link>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="text-center py-12 text-text-secondary">
            <div className="w-16 h-16 rounded-full bg-emerald-500/10 flex items-center justify-center mx-auto mb-4">
               <Info className="w-8 h-8 text-emerald-500" />
            </div>
            <h3 className="text-lg font-medium text-emerald-400 mb-1">No Impact Detected</h3>
            <p>There are no active failure cascades in the system right now.</p>
          </div>
        )}
      </div>
    </div>
  );
}
