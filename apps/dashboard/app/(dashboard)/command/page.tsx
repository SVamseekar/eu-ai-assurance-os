"use client";

import { useState } from "react";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { RiskTopology } from "@/components/risk-topology";
import { ReleaseGateTable } from "@/components/release-gate-table";
import { useSystems } from "@/hooks/use-systems";
import { MOCK_SYSTEMS } from "@/lib/mock-data";
import { normaliseDecision } from "@/lib/utils";
import { TrendingUp, TrendingDown, Minus } from "lucide-react";
import { cn } from "@/lib/utils";

export default function CommandPage() {
  const { data: systems = MOCK_SYSTEMS } = useSystems();
  const [riskFilter, setRiskFilter] = useState<"all" | "high" | "limited" | "minimal">("all");

  const blocked = systems.filter((s) => normaliseDecision(s.releaseDecision) === "Blocked").length;
  const review = systems.filter((s) => normaliseDecision(s.releaseDecision) === "Review").length;
  const highRisk = systems.filter((s) => s.riskClass === "high").length;
  const totalGaps = systems.reduce((sum, s) => sum + s.openGaps.length, 0);
  const avgEval = Math.round(systems.reduce((sum, s) => sum + s.evalScore, 0) / systems.length);
  const avgEvidence = Math.round(systems.reduce((sum, s) => sum + s.evidenceCoverage, 0) / systems.length);
  const auditCompleteness = Math.min(98, avgEvidence + 5);

  const metrics = [
    {
      label: "Total AI Systems",
      value: systems.length,
      sub: `${highRisk} high-risk registered`,
      trend: "neutral" as const,
    },
    {
      label: "Open Control Gaps",
      value: totalGaps,
      sub: blocked > 0 ? `${blocked} blocking release` : "No blockers",
      trend: (blocked > 0 ? "down" : "up") as "up" | "down" | "neutral",
    },
    {
      label: "Avg. Eval Score",
      value: `${avgEval}%`,
      sub: avgEval >= 85 ? "Above 85% target" : "Below 85% target",
      trend: (avgEval >= 85 ? "up" : "down") as "up" | "down" | "neutral",
    },
    {
      label: "Audit Completeness",
      value: `${auditCompleteness}%`,
      sub: `${review} systems under review`,
      trend: "up" as const,
    },
  ];

  return (
    <div className="space-y-5">
      {/* Metric cards — PrimeStay/Invoice Autopilot style */}
      <div className="grid grid-cols-4 gap-4">
        {metrics.map((m) => (
          <div key={m.label} className="bg-card rounded-2xl border border-border p-5">
            <p className="text-xs text-muted-foreground mb-2">{m.label}</p>
            <p className="text-2xl font-bold tracking-tight mb-1.5">{m.value}</p>
            <div className="flex items-center gap-1.5">
              {m.trend === "up" && <TrendingUp className="w-3 h-3 text-emerald-500" />}
              {m.trend === "down" && <TrendingDown className="w-3 h-3 text-red-500" />}
              {m.trend === "neutral" && <Minus className="w-3 h-3 text-muted-foreground" />}
              <p
                className={cn(
                  "text-xs",
                  m.trend === "up" && "text-emerald-600 dark:text-emerald-400",
                  m.trend === "down" && "text-red-600 dark:text-red-400",
                  m.trend === "neutral" && "text-muted-foreground"
                )}
              >
                {m.sub}
              </p>
            </div>
          </div>
        ))}
      </div>

      {/* Risk readiness section */}
      <div className="bg-card rounded-2xl border border-border">
        <div className="flex items-center justify-between px-5 pt-5 pb-4 border-b border-border">
          <div>
            <p className="text-sm font-semibold">Release Readiness</p>
            <p className="text-xs text-muted-foreground mt-0.5">
              Evidence coverage, eval score, and release decision per system.
            </p>
          </div>
          <Select value={riskFilter} onValueChange={(v) => v && setRiskFilter(v as typeof riskFilter)}>
            <SelectTrigger className="w-32 h-8 text-xs">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All systems</SelectItem>
              <SelectItem value="high">High-risk</SelectItem>
              <SelectItem value="limited">Limited-risk</SelectItem>
              <SelectItem value="minimal">Minimal-risk</SelectItem>
            </SelectContent>
          </Select>
        </div>
        <div className="px-2 py-2">
          <RiskTopology systems={systems} filter={riskFilter} />
        </div>
      </div>

      {/* Release gate table */}
      <div className="bg-card rounded-2xl border border-border">
        <div className="px-5 pt-5 pb-4 border-b border-border">
          <p className="text-sm font-semibold">Release Gate</p>
          <p className="text-xs text-muted-foreground mt-0.5">
            Combined compliance evidence, eval regression, data drift, and human oversight.
          </p>
        </div>
        <div className="p-5">
          <ReleaseGateTable systems={systems} />
        </div>
      </div>
    </div>
  );
}
