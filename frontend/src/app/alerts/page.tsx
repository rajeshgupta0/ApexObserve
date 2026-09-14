"use client";

import { usePolling } from "@/lib/usePolling";
import { AlertCircle, CheckCircle2, Clock, ShieldAlert } from "lucide-react";
import { cn } from "@/lib/utils";

export default function AlertsPage() {
  const { data: alerts, loading, error, refetch } = usePolling<any[]>(async () => {
    const res = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080/api"}/alerts`);
    if (!res.ok) throw new Error("Failed to fetch alerts");
    return res.json();
  }, 10000);

  const resolveAlert = async (alertId: string) => {
    try {
      const res = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080/api"}/alerts/${alertId}/resolve`, { method: 'POST' });
      if (res.ok) refetch();
    } catch (e) {
      console.error(e);
    }
  };

  const activeAlerts = alerts?.filter(a => a.status === 'ACTIVE') || [];
  const resolvedAlerts = alerts?.filter(a => a.status === 'RESOLVED') || [];

  return (
    <div className="space-y-6 animate-in fade-in duration-500">
      <header>
        <h1 className="text-3xl font-bold flex items-center gap-3">
          <ShieldAlert className="w-8 h-8 text-rose-500" />
          System Alerts
        </h1>
        <p className="text-text-secondary mt-1">Active threshold breaches and anomalies detected across telemetry</p>
      </header>

      {error && (
        <div className="glass-panel border-rose-500/30 p-4 bg-rose-500/5 text-rose-200 flex items-center gap-3">
          <AlertCircle className="w-5 h-5 text-rose-500" />
          {error.message}
        </div>
      )}

      <div className="glass-panel p-6 min-h-[500px]">
        {loading && !alerts ? (
           <div className="flex justify-center py-12"><div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div></div>
        ) : activeAlerts.length === 0 && resolvedAlerts.length === 0 ? (
          <div className="text-center py-20 text-text-secondary">
            <div className="w-16 h-16 rounded-full bg-emerald-500/10 flex items-center justify-center mx-auto mb-4">
               <CheckCircle2 className="w-8 h-8 text-emerald-500 opacity-80" />
            </div>
            <h3 className="text-lg font-medium text-emerald-400 mb-1">No alerts found</h3>
            <p>Your system is operating normally within defined thresholds.</p>
          </div>
        ) : (
          <div className="space-y-8">
            {activeAlerts.length > 0 && (
              <div>
                <h3 className="font-semibold text-rose-400 mb-4 flex items-center gap-2">
                  <AlertCircle className="w-4 h-4" /> Active Alerts ({activeAlerts.length})
                </h3>
                <div className="space-y-3">
                  {activeAlerts.map(alert => (
                    <div key={alert.id} className="p-4 rounded-lg border border-rose-500/30 bg-rose-500/10 flex flex-col md:flex-row md:items-center justify-between gap-4">
                      <div>
                        <div className="flex items-center gap-3 mb-2">
                          <span className={cn("px-2 py-0.5 text-[10px] font-bold tracking-wider rounded bg-rose-500 text-white uppercase")}>
                            {alert.severity}
                          </span>
                          <span className="font-semibold text-rose-100">{alert.ruleName}</span>
                        </div>
                        <div className="text-sm text-rose-200/70 flex flex-wrap items-center gap-x-4 gap-y-2">
                          <span>Service: <span className="text-rose-200 font-medium">{alert.serviceId}</span></span>
                          <span className="flex items-center gap-1"><Clock className="w-3.5 h-3.5" /> {new Date(alert.createdAt).toLocaleString()}</span>
                        </div>
                      </div>
                      <button 
                        onClick={() => resolveAlert(alert.id)}
                        className="px-4 py-2 bg-rose-500/20 hover:bg-rose-500/40 text-rose-200 rounded-md text-sm font-medium transition-colors border border-rose-500/30 whitespace-nowrap"
                      >
                        Acknowledge & Resolve
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {resolvedAlerts.length > 0 && (
              <div>
                <h3 className="font-semibold text-text-secondary mb-4 flex items-center gap-2">
                  <CheckCircle2 className="w-4 h-4" /> Resolved Alerts History
                </h3>
                <div className="space-y-3">
                  {resolvedAlerts.map(alert => (
                    <div key={alert.id} className="p-4 rounded-lg border border-white/5 bg-black/20 flex flex-col md:flex-row md:items-center justify-between gap-4 opacity-70 hover:opacity-100 transition-opacity">
                      <div>
                        <div className="flex items-center gap-3 mb-2">
                          <span className="font-semibold text-text-primary">{alert.ruleName}</span>
                          <span className="text-[10px] px-1.5 py-0.5 rounded border border-emerald-500/30 text-emerald-400 uppercase tracking-wider">
                            RESOLVED
                          </span>
                        </div>
                        <div className="text-sm text-text-secondary flex flex-wrap items-center gap-x-4 gap-y-2">
                          <span>Service: {alert.serviceId}</span>
                          <span>Triggered: {new Date(alert.createdAt).toLocaleString()}</span>
                          <span className="text-emerald-400">Resolved: {alert.resolvedAt ? new Date(alert.resolvedAt).toLocaleString() : 'N/A'}</span>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
