"use client";

import { usePolling } from "@/lib/usePolling";
import { CheckSquare, ArrowRight, Lightbulb, CheckCircle2 } from "lucide-react";
import Link from "next/link";
import { cn } from "@/lib/utils";

export default function RecommendedChecksPage() {
  const { data: healthData, loading } = usePolling<any>(async () => {
    const res = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080/api"}/metrics/health`);
    if (!res.ok) throw new Error("Failed to fetch health data");
    return res.json();
  }, 5000);

  const services = healthData ? Object.entries(healthData) : [];
  const degradedServices = services.filter(([_, m]: [string, any]) => m.score < 90);

  return (
    <div className="max-w-5xl mx-auto space-y-8 animate-in fade-in duration-500 py-8">
      <header>
        <h1 className="text-3xl font-bold flex items-center gap-3">
          <CheckSquare className="w-8 h-8 text-emerald-400" />
          Recommended Checks
        </h1>
        <p className="text-text-secondary mt-2 max-w-2xl">
          What should I inspect next? ApexObserve correlates active metrics to suggest the most effective next steps for mitigation.
        </p>
      </header>

      <div className="glass-panel p-6 min-h-[300px]">
        {loading ? (
           <div className="flex justify-center py-12"><div className="animate-spin rounded-full h-8 w-8 border-b-2 border-emerald-500"></div></div>
        ) : degradedServices.length > 0 ? (
          <div className="space-y-6">
            {degradedServices.map(([id, m]: [string, any]) => (
              <div key={id} className="p-6 rounded-lg bg-surface border border-white/5 flex gap-4">
                <div className="mt-1">
                  <Lightbulb className={cn("w-6 h-6", m.score < 70 ? "text-rose-400" : "text-amber-400")} />
                </div>
                <div className="flex-1">
                  <h3 className="font-semibold text-lg mb-2">{id} Requires Inspection</h3>
                  <div className="space-y-3 mb-4">
                    {m.errors > 0 && (
                      <div className="flex gap-2 p-3 bg-black/20 rounded border border-white/5 text-sm text-text-secondary">
                        <ArrowRight className="w-4 h-4 mt-0.5 text-text-primary" />
                        <p><strong>Check Logs for Exceptions:</strong> {id} is returning 5xx status codes. Search the logs for stack traces indicating unhandled exceptions or database connection failures.</p>
                      </div>
                    )}
                    {m.latency > 300 && (
                      <div className="flex gap-2 p-3 bg-black/20 rounded border border-white/5 text-sm text-text-secondary">
                        <ArrowRight className="w-4 h-4 mt-0.5 text-text-primary" />
                        <p><strong>Check Dependency Latency:</strong> {id} has elevated response times. Look at the traces view to identify if a downstream database query or third-party API is bottlenecking the request.</p>
                      </div>
                    )}
                    <div className="flex gap-2 p-3 bg-black/20 rounded border border-white/5 text-sm text-text-secondary">
                      <ArrowRight className="w-4 h-4 mt-0.5 text-text-primary" />
                      <p><strong>Verify Resource Utilization:</strong> Ensure the container or node running {id} has not exhausted its CPU or Memory limits causing throttling.</p>
                    </div>
                  </div>
                  <Link 
                    href={`/services/${id}`}
                    className="inline-flex items-center gap-2 text-sm font-medium text-emerald-400 hover:text-emerald-300 transition-colors"
                  >
                    Open Investigation Workspace &rarr;
                  </Link>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="text-center py-12 text-text-secondary">
            <div className="w-16 h-16 rounded-full bg-emerald-500/10 flex items-center justify-center mx-auto mb-4">
               <CheckCircle2 className="w-8 h-8 text-emerald-500" />
            </div>
            <h3 className="text-lg font-medium text-emerald-400 mb-1">System Healthy</h3>
            <p>No immediate checks are recommended. The system is operating within normal parameters.</p>
          </div>
        )}
      </div>
    </div>
  );
}
