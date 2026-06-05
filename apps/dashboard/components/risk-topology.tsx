"use client";

import type { AiSystem } from "@/lib/types";
import { normaliseDecision } from "@/lib/utils";
import { cn } from "@/lib/utils";

const RISK_BAR_COLOR: Record<string, string> = {
  high: "bg-red-500",
  limited: "bg-amber-500",
  minimal: "bg-emerald-500",
  prohibited: "bg-red-800",
};

const DECISION_DOT: Record<string, string> = {
  Pass: "bg-emerald-500",
  Review: "bg-amber-500",
  Blocked: "bg-red-500",
};

interface RiskTopologyProps {
  systems: AiSystem[];
  filter: "all" | "high" | "limited" | "minimal";
}

export function RiskTopology({ systems, filter }: RiskTopologyProps) {
  const filtered = systems.filter((s) => filter === "all" || s.riskClass === filter);

  return (
    <div className="space-y-1">
      {/* Column headers */}
      <div className="grid grid-cols-[1fr_100px_80px_80px_90px] gap-3 px-3 pb-2 border-b border-border">
        <span className="text-xs font-medium text-muted-foreground">System</span>
        <span className="text-xs font-medium text-muted-foreground">Evidence</span>
        <span className="text-xs font-medium text-muted-foreground text-right">Eval</span>
        <span className="text-xs font-medium text-muted-foreground text-right">Risk</span>
        <span className="text-xs font-medium text-muted-foreground text-right">Decision</span>
      </div>

      {filtered.map((system) => {
        const decision = normaliseDecision(system.releaseDecision);
        const barColor = RISK_BAR_COLOR[system.riskClass] ?? "bg-muted-foreground";
        const dotColor = DECISION_DOT[decision] ?? "bg-muted-foreground";

        return (
          <div
            key={system.id}
            className="grid grid-cols-[1fr_100px_80px_80px_90px] gap-3 items-center px-3 py-2.5 rounded-lg hover:bg-muted/40 transition-colors"
          >
            {/* System name */}
            <div className="min-w-0">
              <p className="text-sm font-medium truncate">{system.name}</p>
              <p className="text-xs text-muted-foreground mt-0.5 capitalize">{system.riskClass}-risk</p>
            </div>

            {/* Evidence bar */}
            <div className="flex items-center gap-2">
              <div className="flex-1 h-1.5 rounded-full bg-muted overflow-hidden">
                <div
                  className={cn("h-full rounded-full", barColor)}
                  style={{ width: `${system.evidenceCoverage}%` }}
                />
              </div>
              <span className="text-xs text-muted-foreground w-8 text-right flex-shrink-0">
                {system.evidenceCoverage}%
              </span>
            </div>

            {/* Eval score */}
            <div className="text-right">
              <span
                className={cn(
                  "text-sm font-semibold",
                  system.evalScore >= 85
                    ? "text-emerald-600 dark:text-emerald-400"
                    : system.evalScore >= 70
                    ? "text-amber-600 dark:text-amber-400"
                    : "text-red-600 dark:text-red-400"
                )}
              >
                {system.evalScore}%
              </span>
            </div>

            {/* Risk class */}
            <div className="text-right">
              <span className="text-xs text-muted-foreground capitalize">{system.riskClass}</span>
            </div>

            {/* Decision */}
            <div className="flex items-center justify-end gap-1.5">
              <span className={cn("w-2 h-2 rounded-full flex-shrink-0", dotColor)} />
              <span className="text-xs font-medium">{decision}</span>
            </div>
          </div>
        );
      })}

      {filtered.length === 0 && (
        <div className="py-10 text-center text-sm text-muted-foreground">
          No systems match this filter.
        </div>
      )}
    </div>
  );
}
