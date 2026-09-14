"use client";

import { useState } from "react";
import { usePolling } from "@/lib/usePolling";
import { AlertTriangle, ShieldAlert, Activity, CheckCircle, Clock, ServerCrash, Search } from "lucide-react";
import { cn } from "@/lib/utils";
import Link from "next/link";

export default function IncidentsPage() {
  const { data: incidents, loading, error, refetch } = usePolling<any[]>(async () => {
    const res = await fetch(`${process.env.NEXT_PUBLIC_ALERTING_API_URL || "http://localhost:8084/api"}/incidents`);
    if (!res.ok) throw new Error("Failed to fetch incidents");
    return res.json();
  }, 10000);

  const [selectedIncident, setSelectedIncident] = useState<any | null>(null);

  const viewDetails = async (id: string) => {
    try {
      const res = await fetch(`${process.env.NEXT_PUBLIC_ALERTING_API_URL || "http://localhost:8084/api"}/incidents/${id}`);
      const data = await res.json();
      setSelectedIncident(data);
    } catch (e) {
      console.error(e);
    }
  };

  const updateStatus = async (id: string, newStatus: string) => {
    try {
      await fetch(`${process.env.NEXT_PUBLIC_ALERTING_API_URL || "http://localhost:8084/api"}/incidents/${id}/status`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ status: newStatus }),
      });
      refetch();
      viewDetails(id);
    } catch (e) {
      console.error(e);
    }
  };

  return (
    <div className="space-y-6 animate-in fade-in duration-500 h-[calc(100vh-80px)] flex flex-col">
      <header>
        <h1 className="text-3xl font-bold flex items-center gap-3">
          <ServerCrash className="w-8 h-8 text-indigo-500" />
          Incident Management
        </h1>
        <p className="text-text-secondary mt-1">Correlated multi-service alerts and root cause analysis</p>
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
              <Activity className="w-4 h-4 text-indigo-400" /> Active Incidents
            </h2>
          </div>
          
          <div className="p-4 overflow-y-auto flex-1 space-y-3">
            {loading && !incidents ? (
              <div className="flex justify-center items-center h-32">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-500"></div>
              </div>
            ) : !incidents || incidents.length === 0 ? (
              <div className="text-center text-text-secondary p-8 border border-white/5 rounded-lg bg-black/20">
                <CheckCircle className="w-10 h-10 mx-auto mb-2 text-emerald-500 opacity-50" />
                No active incidents.
              </div>
            ) : (
              incidents.map((inc) => (
                <div 
                  key={inc.id} 
                  onClick={() => viewDetails(inc.id)}
                  className={cn(
                    "p-4 border rounded-lg cursor-pointer transition-colors relative overflow-hidden group",
                    selectedIncident?.incident?.id === inc.id ? 'bg-indigo-500/20 border-indigo-500/50' : 'bg-surface border-white/5 hover:bg-white/5'
                  )}
                >
                  {inc.status === 'OPEN' && <div className="absolute top-0 left-0 w-1 h-full bg-rose-500" />}
                  {inc.status === 'INVESTIGATING' && <div className="absolute top-0 left-0 w-1 h-full bg-amber-500" />}
                  {inc.status === 'RESOLVED' && <div className="absolute top-0 left-0 w-1 h-full bg-emerald-500" />}
                  
                  <div className="flex justify-between items-start mb-3">
                    <span className={cn("px-2 py-0.5 text-[10px] font-bold tracking-wider rounded uppercase", inc.severity === 'CRITICAL' ? 'bg-rose-500 text-white' : 'bg-amber-500 text-white')}>
                      {inc.severity}
                    </span>
                    <span className={cn("text-[10px] px-2 py-0.5 rounded-full border uppercase tracking-wider", 
                      inc.status === 'RESOLVED' ? 'border-emerald-500/30 text-emerald-400 bg-emerald-500/10' : 
                      inc.status === 'INVESTIGATING' ? 'border-amber-500/30 text-amber-400 bg-amber-500/10' :
                      'border-rose-500/30 text-rose-400 bg-rose-500/10'
                    )}>
                      {inc.status}
                    </span>
                  </div>
                  <h3 className="font-semibold text-sm mb-2 line-clamp-2 leading-snug">{inc.title}</h3>
                  <div className="text-xs text-text-secondary flex items-center gap-1.5 mt-3">
                    <Clock className="w-3 h-3" /> {new Date(inc.createdAt).toLocaleString()}
                  </div>
                </div>
              ))
            )}
          </div>
        </div>

        <div className="lg:col-span-2 glass-panel flex flex-col h-full overflow-hidden relative">
          {selectedIncident ? (
            <div className="flex flex-col h-full">
              <div className="p-6 border-b border-white/5 shrink-0">
                <div className="flex justify-between items-start mb-6">
                  <div>
                    <h2 className="text-2xl font-bold mb-2">{selectedIncident.incident.title}</h2>
                    <div className="text-sm text-text-secondary flex flex-wrap gap-x-6 gap-y-2">
                      <span className="font-mono text-xs bg-black/30 px-2 py-1 rounded">ID: {selectedIncident.incident.id}</span>
                      <span className="flex items-center gap-1"><Clock className="w-4 h-4" /> Created: {new Date(selectedIncident.incident.createdAt).toLocaleString()}</span>
                    </div>
                  </div>
                  <div className="flex gap-2">
                    {selectedIncident.incident.status !== 'RESOLVED' && (
                      <>
                        <button 
                          onClick={() => updateStatus(selectedIncident.incident.id, 'INVESTIGATING')} 
                          className="px-4 py-2 bg-amber-500/20 text-amber-400 border border-amber-500/30 rounded-lg text-sm font-medium hover:bg-amber-500/30 transition-colors"
                        >
                          Investigate
                        </button>
                        <button 
                          onClick={() => updateStatus(selectedIncident.incident.id, 'RESOLVED')} 
                          className="px-4 py-2 bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 rounded-lg text-sm font-medium hover:bg-emerald-500/30 transition-colors"
                        >
                          Resolve
                        </button>
                      </>
                    )}
                  </div>
                </div>
              </div>
              
              <div className="p-6 overflow-y-auto flex-1 space-y-6">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div className="p-5 bg-black/20 rounded-lg border border-white/5">
                    <h3 className="font-semibold mb-4 flex items-center gap-2"><Activity className="w-5 h-5 text-indigo-400" /> Affected Services</h3>
                    <div className="flex flex-wrap gap-2">
                      {selectedIncident.incident.affectedServices ? JSON.parse(selectedIncident.incident.affectedServices).map((svc: string) => (
                        <Link href={`/services/${svc}`} key={svc} className="px-3 py-1.5 bg-indigo-500/10 hover:bg-indigo-500/20 border border-indigo-500/20 text-indigo-200 transition-colors rounded-md text-sm font-mono flex items-center gap-2">
                          {svc} <Search className="w-3 h-3" />
                        </Link>
                      )) : <span className="text-text-secondary text-sm">None</span>}
                    </div>
                  </div>
                  
                  <div className="p-5 bg-black/20 rounded-lg border border-white/5">
                    <h3 className="font-semibold mb-4 flex items-center gap-2"><ShieldAlert className="w-5 h-5 text-amber-500" /> Correlated Alerts ({(selectedIncident.alerts || []).length})</h3>
                    <div className="space-y-3">
                      {(selectedIncident.alerts || []).map((alert: any) => (
                        <div key={alert.id} className="text-sm bg-surface p-3 rounded-md border border-white/5 flex flex-col gap-1">
                          <div className="flex justify-between items-start">
                            <span className="font-medium">{alert.ruleName}</span>
                            <span className={cn("text-[10px] px-1.5 py-0.5 rounded font-bold uppercase", alert.severity === 'CRITICAL' ? 'bg-rose-500/20 text-rose-400' : 'bg-amber-500/20 text-amber-400')}>{alert.severity}</span>
                          </div>
                          <span className="text-text-secondary text-xs font-mono">{alert.serviceId}</span>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          ) : (
            <div className="absolute inset-0 flex flex-col items-center justify-center text-text-secondary bg-black/10">
              <div className="w-16 h-16 rounded-full bg-white/5 flex items-center justify-center mb-4">
                 <AlertTriangle className="w-8 h-8 opacity-50" />
              </div>
              <p className="text-lg font-medium">Select an incident to view details and RCA.</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
