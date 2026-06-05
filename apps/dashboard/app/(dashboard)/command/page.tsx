"use client";

import { useState } from "react";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
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
import { normaliseDecision, cn } from "@/lib/utils";
import { TrendingUp, TrendingDown, Minus } from "lucide-react";

export default function CommandPage() {
  const { data: systems = MOCK_SYSTEMS } = useSystems();
  const [riskFilter, setRiskFilter] = useState<"all" | "high" | "limited" | "minimal">("all");

  const blocked = systems.filter((s) => normaliseDecision(s.releaseDecision) === "Blocked").length;
  const review  = systems.filter((s) => normaliseDecision(s.releaseDecision) === "Review").length;
  const highRisk = systems.filter((s) => s.riskClass === "high").length;
  const totalGaps = systems.reduce((sum, s) => sum + s.openGaps.length, 0);
  const avgEval = Math.round(systems.reduce((sum, s) => sum + s.evalScore, 0) / systems.length);
  const avgEvidence = Math.round(systems.reduce((sum, s) => sum + s.evidenceCoverage, 0) / systems.length);

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
      value: `${Math.min(98, avgEvidence + 5)}%`,
      sub: `${review} systems under review`,
      trend: "up" as const,
    },
  ];

  return (
    <div className="space-y-5">
      {/* Stat cards */}
      <div className="grid grid-cols-4 gap-4">
        {metrics.map((m) => (
          <Card key={m.label}>
            <CardContent className="pt-5 pb-4">
              <p className="text-xs text-muted-foreground mb-3">{m.label}</p>
              <p className="text-2xl font-bold tracking-tight mb-2">{m.value}</p>
              <div className="flex items-center gap-1.5">
                {m.trend === "up"      && <TrendingUp   className="w-3 h-3 text-emerald-500 shrink-0" />}
                {m.trend === "down"    && <TrendingDown  className="w-3 h-3 text-red-500 shrink-0" />}
                {m.trend === "neutral" && <Minus         className="w-3 h-3 text-muted-foreground shrink-0" />}
                <p className={cn(
                  "text-xs",
                  m.trend === "up"      && "text-emerald-600 dark:text-emerald-400",
                  m.trend === "down"    && "text-red-600 dark:text-red-400",
                  m.trend === "neutral" && "text-muted-foreground"
                )}>
                  {m.sub}
                </p>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      {/* Release readiness */}
      <Card>
        <CardHeader className="flex-row items-start justify-between gap-4">
          <div>
            <CardTitle>Release Readiness</CardTitle>
            <CardDescription className="mt-0.5">
              Evidence coverage, eval score, and release decision per system.
            </CardDescription>
          </div>
          <Select value={riskFilter} onValueChange={(v) => v && setRiskFilter(v as typeof riskFilter)}>
            <SelectTrigger className="w-32 h-8 text-xs shrink-0">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All systems</SelectItem>
              <SelectItem value="high">High-risk</SelectItem>
              <SelectItem value="limited">Limited-risk</SelectItem>
              <SelectItem value="minimal">Minimal-risk</SelectItem>
            </SelectContent>
          </Select>
        </CardHeader>
        <CardContent>
          <RiskTopology systems={systems} filter={riskFilter} />
        </CardContent>
      </Card>

      {/* Release gate table */}
      <Card>
        <CardHeader>
          <CardTitle>Release Gate</CardTitle>
          <CardDescription>
            Combined compliance evidence, eval regression, data drift, and human oversight.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <ReleaseGateTable systems={systems} />
        </CardContent>
      </Card>
    </div>
  );
}
