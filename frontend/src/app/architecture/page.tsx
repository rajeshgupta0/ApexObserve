"use client";

import { Share2, Database, MessageSquare, LineChart, Code2, Cpu } from "lucide-react";
import { cn } from "@/lib/utils";

export default function ArchitecturePage() {
  const components = [
    {
      title: "Demo Microservices",
      icon: Code2,
      color: "text-blue-400",
      bg: "bg-blue-400/10",
      description: "Generates real telemetry by processing simulated eCommerce traffic.",
      implemented: true
    },
    {
      title: "OpenTelemetry",
      icon: Share2,
      color: "text-purple-400",
      bg: "bg-purple-400/10",
      description: "Standardized instrumentation that collects metrics, logs, and traces.",
      implemented: true
    },
    {
      title: "Kafka Event Bus",
      icon: MessageSquare,
      color: "text-emerald-400",
      bg: "bg-emerald-400/10",
      description: "Moves telemetry reliably between ingestion and processing services asynchronously.",
      implemented: true
    },
    {
      title: "TimescaleDB (PostgreSQL)",
      icon: Database,
      color: "text-amber-400",
      bg: "bg-amber-400/10",
      description: "Stores and queries time-series observability data efficiently.",
      implemented: true
    },
    {
      title: "AI / ML Engine",
      icon: Cpu,
      color: "text-indigo-400",
      bg: "bg-indigo-400/10",
      description: "Analyzes traces to identify root causes and predict latency degradation.",
      implemented: true
    },
    {
      title: "Next.js Frontend",
      icon: LineChart,
      color: "text-rose-400",
      bg: "bg-rose-400/10",
      description: "Renders real-time telemetry into human-readable actionable insights.",
      implemented: true
    }
  ];

  return (
    <div className="max-w-5xl mx-auto space-y-12 animate-in fade-in duration-500 py-8">
      <header className="text-center space-y-4">
        <div className="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-primary/10 mb-2">
          <Share2 className="w-8 h-8 text-primary" />
        </div>
        <h1 className="text-4xl font-bold tracking-tight">How ApexObserve Works</h1>
        <p className="text-lg text-text-secondary max-w-2xl mx-auto">
          A modern, decoupled, event-driven observability pipeline built to handle distributed systems at scale.
        </p>
      </header>

      <div className="relative">
        {/* Connection Line */}
        <div className="absolute left-1/2 top-0 bottom-0 w-px bg-gradient-to-b from-primary/5 via-primary/20 to-primary/5 -translate-x-1/2 hidden md:block" />

        <div className="space-y-8 relative">
          {components.map((comp, i) => (
            <div key={comp.title} className={cn("flex flex-col md:flex-row items-center gap-8", i % 2 === 0 ? "md:flex-row-reverse" : "")}>
              <div className="flex-1 w-full md:w-auto text-center md:text-left">
                <div className={cn("glass-panel p-6 inline-block w-full max-w-md text-left transition-all hover:scale-[1.02]", i % 2 === 0 ? "md:mr-auto" : "md:ml-auto")}>
                  <div className="flex items-center gap-3 mb-3">
                    <div className={cn("p-2 rounded-lg", comp.bg)}>
                      <comp.icon className={cn("w-5 h-5", comp.color)} />
                    </div>
                    <h3 className="font-semibold text-lg">{comp.title}</h3>
                  </div>
                  <p className="text-text-secondary">{comp.description}</p>
                  
                  <div className="mt-4 pt-4 border-t border-white/5 flex items-center justify-between">
                    <span className="text-xs uppercase tracking-wider text-text-secondary font-medium">Status</span>
                    {comp.implemented ? (
                      <span className="px-2 py-1 rounded bg-emerald-500/10 text-emerald-400 text-xs font-semibold">IMPLEMENTED</span>
                    ) : (
                      <span className="px-2 py-1 rounded bg-white/5 text-text-secondary text-xs font-semibold">DESIGN CONCEPT</span>
                    )}
                  </div>
                </div>
              </div>
              
              <div className="w-12 h-12 rounded-full border-4 border-background bg-surface flex items-center justify-center relative z-10 shrink-0 shadow-xl hidden md:flex">
                <div className="w-3 h-3 rounded-full bg-primary" />
              </div>
              
              <div className="flex-1 hidden md:block" />
            </div>
          ))}
        </div>
      </div>
      
      <div className="glass-panel p-8 text-center mt-12 bg-primary/5 border-primary/20">
        <h3 className="text-xl font-semibold mb-2">Design Principles</h3>
        <p className="text-text-secondary max-w-3xl mx-auto">
          ApexObserve separates ingestion from processing. Telemetry is gathered via standardized OTel collectors, buffered in Kafka to prevent data loss during traffic spikes, and asynchronously processed into TimescaleDB for sub-millisecond query performance.
        </p>
      </div>
    </div>
  );
}
