"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { 
  LayoutDashboard, 
  Server, 
  Activity, 
  AlignLeft, 
  GitCommit, 
  Network,
  AlertTriangle,
  Siren,
  BrainCircuit,
  Share2
} from "lucide-react";
import { cn } from "@/lib/utils";

const navItems = [
  { name: "Overview", href: "/", icon: LayoutDashboard },
  { name: "Services", href: "/services", icon: Server },
  { name: "Metrics", href: "/metrics", icon: Activity },
  { name: "Logs", href: "/logs", icon: AlignLeft },
  { name: "Traces", href: "/traces", icon: GitCommit },
  { name: "Dependencies", href: "/dependencies", icon: Network },
  { name: "Alerts", href: "/alerts", icon: AlertTriangle },
  { name: "Incidents", href: "/incidents", icon: Siren },
  { name: "Intelligence", href: "/intelligence", icon: BrainCircuit },
  { name: "System Architecture", href: "/architecture", icon: Share2 },
];

export function Sidebar() {
  const pathname = usePathname();

  return (
    <div className="w-64 border-r border-white/5 bg-surface/50 h-full flex flex-col">
      <div className="p-6">
        <h1 className="text-2xl font-bold bg-gradient-to-r from-blue-400 to-indigo-500 bg-clip-text text-transparent">
          ApexObserve
        </h1>
        <p className="text-xs text-text-secondary mt-1 font-medium tracking-wide uppercase">Intelligent Observability</p>
      </div>

      <nav className="flex-1 px-4 space-y-1 overflow-y-auto">
        {navItems.map((item) => {
          const isActive = pathname === item.href || (pathname.startsWith(item.href) && item.href !== "/");
          return (
            <Link
              key={item.name}
              href={item.href}
              className={cn(
                "flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all duration-200",
                isActive 
                  ? "bg-primary/10 text-primary" 
                  : "text-text-secondary hover:text-text-primary hover:bg-white/5"
              )}
            >
              <item.icon className={cn("w-4 h-4", isActive ? "text-primary" : "text-text-secondary")} />
              {item.name}
            </Link>
          );
        })}
      </nav>

      <div className="p-4 border-t border-white/5">
        <div className="flex items-center gap-2 glass-panel px-3 py-2 rounded-lg bg-black/20 text-xs">
          <span className="w-2 h-2 rounded-full bg-success animate-pulse"></span>
          <span className="text-text-secondary">System Online</span>
        </div>
      </div>
    </div>
  );
}
