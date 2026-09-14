"use client";

import { Clock, Search } from "lucide-react";

export function TopBar() {
  return (
    <div className="h-16 border-b border-white/5 bg-surface/30 backdrop-blur-md flex items-center justify-between px-6 sticky top-0 z-10">
      <div className="flex-1 max-w-xl">
        <div className="relative group">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-secondary group-focus-within:text-primary transition-colors" />
          <input 
            type="text" 
            placeholder="Search services, traces, errors..." 
            className="w-full bg-black/20 border border-white/10 rounded-lg pl-10 pr-4 py-2 text-sm focus:outline-none focus:border-primary/50 focus:ring-1 focus:ring-primary/50 transition-all placeholder:text-text-secondary/50"
          />
        </div>
      </div>
      
      <div className="flex items-center gap-4 ml-4">
        <div className="flex items-center gap-2 text-sm text-text-secondary bg-black/20 border border-white/10 px-3 py-1.5 rounded-lg">
          <Clock className="w-4 h-4" />
          <select className="bg-transparent border-none focus:outline-none cursor-pointer">
            <option value="5m">Last 5 minutes</option>
            <option value="15m">Last 15 minutes</option>
            <option value="1h">Last 1 hour</option>
            <option value="6h">Last 6 hours</option>
            <option value="24h">Last 24 hours</option>
          </select>
        </div>
      </div>
    </div>
  );
}
