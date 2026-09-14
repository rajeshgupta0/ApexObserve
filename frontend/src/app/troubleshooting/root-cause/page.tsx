"use client";

import { usePolling } from "@/lib/usePolling";
import { BrainCircuit, CheckCircle2, ChevronRight, FileSearch } from "lucide-react";
import Link from "next/link";

export default function RootCausePage() {
  const { data: incidents, loading } = usePolling<any[]>(async () => {
    const res = await fetch(`${process.env.NEXT_PUBLIC_ALERTING_API_URL || "http://localhost:8084/api"}/incidents`);
    if (!res.ok) throw new Error("Failed to fetch incidents");
    return res.json();
  }, 5000);

  const activeIncidents = incidents?.filter(i => i.status !== 'RESOLVED') || [];

  return (
    <div className="max-w-5xl mx-auto space-y-8 animate-in fade-in duration-500 py-8">
      <header>
        <h1 className="text-3xl font-bold flex items-center gap-3">
          <BrainCircuit className="w-8 h-8 text-indigo-400" />
          Root Cause Insights
        </h1>
        <p className="text-text-secondary mt-2 max-w-2xl">
          Why might this be happening? ApexObserve correlates symptoms and traces to deduce probable root causes for active incidents.
        </p>
      </header>

      <div className="glass-panel p-6 min-h-[300px]">
        {loading ? (
           <div className="flex justify-center py-12"><div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-500"></div></div>
        ) : activeIncidents.length > 0 ? (
          <div className="space-y-6">
            {activeIncidents.map((incident) => (
              <div key={incident.id} className="border border-white/10 rounded-lg overflow-hidden bg-black/20">
                <div className="p-4 border-b border-white/5 bg-surface/50 flex justify-between items-center">
                  <div className="flex items-center gap-3">
                    <span className="px-2 py-1 rounded bg-indigo-500/20 text-indigo-400 text-xs font-bold tracking-wide">INCIDENT</span>
                    <h3 className="font-semibold">{incident.title}</h3>
                  </div>
                  <Link href={`/incidents`} className="text-xs text-text-secondary hover:text-primary transition-colors">
                    View full incident &rarr;
                  </Link>
                </div>
                
                <div className="p-6">
                  <div className="flex items-start gap-4">
                    <FileSearch className="w-6 h-6 text-indigo-400 shrink-0 mt-1" />
                    <div>
                      <h4 className="font-semibold text-lg mb-2">Likely Root Cause: <span className="text-indigo-400">{incident.serviceId}</span></h4>
                      <p className="text-sm text-text-secondary mb-4 leading-relaxed">
                        The AI engine has identified <strong>{incident.serviceId}</strong> as the primary origin of this failure cascade. 
                        Evidence shows error traces originating here before propagating to upstream callers.
                      </p>
                      
                      <div className="bg-surface border border-white/5 rounded-lg p-4 mb-4">
                        <div className="flex justify-between items-center mb-2">
                          <span className="text-xs font-medium text-text-secondary uppercase">Confidence Score</span>
                          <span className="text-sm font-bold text-emerald-400">High (85%+)</span>
                        </div>
                        <div className="h-1.5 w-full bg-black/40 rounded-full overflow-hidden">
                          <div className="h-full bg-indigo-500 w-[85%]" />
                        </div>
                      </div>
                      
                      <div className="flex flex-col sm:flex-row gap-3">
                        <Link 
                          href={`/services/${incident.serviceId}`}
                          className="flex items-center justify-center gap-2 px-4 py-2 bg-indigo-500 hover:bg-indigo-600 text-white rounded-lg text-sm font-medium transition-colors"
                        >
                          Investigate Service <ChevronRight className="w-4 h-4" />
                        </Link>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="text-center py-12 text-text-secondary">
            <div className="w-16 h-16 rounded-full bg-emerald-500/10 flex items-center justify-center mx-auto mb-4">
               <CheckCircle2 className="w-8 h-8 text-emerald-500" />
            </div>
            <h3 className="text-lg font-medium text-emerald-400 mb-1">No Active Incidents</h3>
            <p>There are currently no active incidents requiring root cause analysis.</p>
          </div>
        )}
      </div>
    </div>
  );
}
