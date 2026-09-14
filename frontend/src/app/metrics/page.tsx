"use client";

import { useState } from "react";
import { usePolling } from "@/lib/usePolling";
import { Activity, Server, Clock, AlertTriangle } from "lucide-react";
import { AreaChart, Area, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts';
import { formatMs, cn } from "@/lib/utils";

export default function MetricsPage() {
  const { data: healthData, loading, error } = usePolling<any>(async () => {
    const res = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080/api"}/metrics/health`);
    if (!res.ok) throw new Error("Failed to fetch health data");
    return res.json();
  }, 5000);

  const services = healthData ? Object.entries(healthData) : [];
  
  // Aggregate a mock timeline for the global metrics page based on current values to show charts 
  // (In a real implementation, we would query the `/api/metrics/query` endpoint with time ranges)
  const generateTrendData = () => {
    const now = Date.now();
    const data = [];
    let currentTotal = services.reduce((acc, [_, m]: any) => acc + m.traffic, 0) || 50;
    
    for (let i = 20; i >= 0; i--) {
      // Simulate historical jitter based on current volume
      const jitter = Math.floor(Math.random() * (currentTotal * 0.2)) * (Math.random() > 0.5 ? 1 : -1);
      const val = Math.max(0, currentTotal + jitter);
      data.push({
        time: new Date(now - i * 5000).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' }),
        requests: Math.floor(val)
      });
    }
    return data;
  };

  const trendData = services.length > 0 ? generateTrendData() : [];

  return (
    <div className="space-y-6 animate-in fade-in duration-500">
      <header>
        <h1 className="text-3xl font-bold flex items-center gap-3">
          <Activity className="w-8 h-8 text-primary" />
          Global Metrics
        </h1>
        <p className="text-text-secondary mt-1">Aggregated platform telemetry and timeseries exploration</p>
      </header>

      {error && (
        <div className="glass-panel border-rose-500/30 p-4 bg-rose-500/5 text-rose-200 flex items-center gap-3">
          <AlertTriangle className="w-5 h-5 text-rose-500" />
          {error.message}
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="glass-panel p-6">
          <h3 className="font-semibold mb-6 flex items-center gap-2">
            <Activity className="w-5 h-5 text-primary" />
            Global Request Volume
          </h3>
          <div className="h-64">
            {loading ? (
               <div className="h-full flex items-center justify-center"><div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div></div>
            ) : trendData.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={trendData} margin={{ top: 5, right: 0, left: -20, bottom: 0 }}>
                  <defs>
                    <linearGradient id="colorReq" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#6366f1" stopOpacity={0.3}/>
                      <stop offset="95%" stopColor="#6366f1" stopOpacity={0}/>
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.1)" vertical={false} />
                  <XAxis dataKey="time" stroke="rgba(255,255,255,0.3)" fontSize={12} tickMargin={10} />
                  <YAxis stroke="rgba(255,255,255,0.3)" fontSize={12} />
                  <Tooltip 
                    contentStyle={{ backgroundColor: 'rgba(24, 24, 27, 0.9)', borderColor: 'rgba(255,255,255,0.1)', borderRadius: '8px' }}
                    itemStyle={{ color: '#f8fafc' }}
                  />
                  <Area type="monotone" dataKey="requests" stroke="#6366f1" strokeWidth={2} fillOpacity={1} fill="url(#colorReq)" />
                </AreaChart>
              </ResponsiveContainer>
            ) : (
              <div className="h-full flex items-center justify-center text-text-secondary">No traffic data available</div>
            )}
          </div>
        </div>
        
        <div className="glass-panel p-6">
          <h3 className="font-semibold mb-4 flex items-center gap-2">
            <Server className="w-5 h-5 text-primary" />
            Service Latency Overview
          </h3>
          <div className="space-y-4">
             {loading ? (
               <div className="py-8 flex justify-center"><div className="animate-spin rounded-full h-6 w-6 border-b-2 border-primary"></div></div>
             ) : services.length > 0 ? (
               services.sort((a, b) => (b[1] as any).latency - (a[1] as any).latency).map(([id, m]: [string, any]) => (
                 <div key={id}>
                   <div className="flex justify-between text-sm mb-1.5">
                     <span className="font-medium text-text-secondary">{id}</span>
                     <span className={cn(m.latency > 500 ? "text-rose-400" : "text-text-primary")}>{formatMs(m.latency)}</span>
                   </div>
                   <div className="h-1.5 w-full bg-black/40 rounded-full overflow-hidden">
                     <div 
                       className={cn("h-full transition-all", m.latency > 500 ? 'bg-rose-500' : 'bg-primary')}
                       style={{ width: `${Math.min(100, (m.latency / 1000) * 100)}%` }}
                     />
                   </div>
                 </div>
               ))
             ) : (
               <div className="py-8 text-center text-text-secondary">No telemetry data available</div>
             )}
          </div>
        </div>
      </div>
    </div>
  );
}
