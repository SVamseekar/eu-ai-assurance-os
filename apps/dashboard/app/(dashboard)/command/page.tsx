"use client";

import { useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
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
import { Server, ShieldX, FlaskConical, FileCheck } from "lucide-react";
import { cn } from "@/lib/utils";

export default function CommandPage() {
  const { data: systems = MOCK_SYSTEMS } = useSystems();
  const [riskFilter, setRiskFilter] = useState<"all" | "high" | "limited" | "minimal">("all");

  const blocked = systems.filter((s) => normaliseDecision(s.releaseDecision) === "Blocked").length;
  const highRisk = systems.filter((s) => s.riskClass === "high").length;
  const totalGaps = systems.reduce((sum, s) => sum + s.openGaps.length, 0);
  const avgEval = Math.round(systems.reduce((sum, s) => sum + s.evalScore, 0) / systems.length);
  const avgEvidence = Math.round(
    systems.reduce((sum, s) => sum + s.evidenceCoverage, 0) / systems.length
  );
  const auditCompleteness = Math.min(98, avgEvidence + 5);

  const metrics = [
    {
      label: "AI Systems",
      value: systems.length,
      sub: `${highRisk} high-risk`,
      subColor: highRisk > 0 ? "text-amber-600 dark:text-amber-400" : "text-muted-foreground",
      icon: Server,
      iconBg: "bg-indigo-50 dark:bg-indigo-950/50",
      iconColor: "text-indigo-600 dark:text-indigo-400",
    },
    {
      label: "Open Control Gaps",
      value: totalGaps,
      sub: `${blocked} release blockers`,
      subColor: blocked > 0 ? "text-red-600 dark:text-red-400" : "text-muted-foreground",
      icon: ShieldX,
      iconBg: blocked > 0 ? "bg-red-50 dark:bg-red-950/50" : "bg-muted",
      iconColor: blocked > 0 ? "text-red-600 dark:text-red-400" : "text-muted-foreground",
    },
    {
      label: "Eval Gate Pass Rate",
      value: `${avgEval}%`,
      sub: avgEval >= 85 ? "above target" : "below 85% target",
      subColor:
        avgEval >= 85 ? "text-emerald-600 dark:text-emerald-400" : "text-amber-600 dark:text-amber-400",
      icon: FlaskConical,
      iconBg: avgEval >= 85 ? "bg-emerald-50 dark:bg-emerald-950/50" : "bg-amber-50 dark:bg-amber-950/50",
      iconColor:
        avgEval >= 85 ? "text-emerald-600 dark:text-emerald-400" : "text-amber-600 dark:text-amber-400",
    },
    {
      label: "Audit Completeness",
      value: `${auditCompleteness}%`,
      sub: "ready for review",
      subColor: "text-emerald-600 dark:text-emerald-400",
      icon: FileCheck,
      iconBg: "bg-emerald-50 dark:bg-emerald-950/50",
      iconColor: "text-emerald-600 dark:text-emerald-400",
    },
  ];

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-4 gap-4">
        {metrics.map((m) => {
          const Icon = m.icon;
          return (
            <div key={m.label} className="bg-card rounded-xl border border-border p-5">
              <div className="flex items-start justify-between mb-4">
                <p className="text-sm text-muted-foreground font-medium">{m.label}</p>
                <div className={cn("w-8 h-8 rounded-lg grid place-items-center flex-shrink-0", m.iconBg)}>
                  <Icon className={cn("w-4 h-4", m.iconColor)} />
                </div>
              </div>
              <p className="text-3xl font-bold tracking-tight">{m.value}</p>
              <p className={cn("text-xs mt-1.5 font-medium", m.subColor)}>{m.sub}</p>
            </div>
          );
        })}
      </div>

      <Card>
        <CardHeader className="flex-row items-center justify-between pb-0">
          <div>
            <CardTitle className="text-base">Risk Topology</CardTitle>
            <p className="text-sm text-muted-foreground mt-0.5">
              Systems plotted by evidence coverage and eval score. Size indicates open gaps.
            </p>
          </div>
          <Select value={riskFilter} onValueChange={(v) => v && setRiskFilter(v as typeof riskFilter)}>
            <SelectTrigger className="w-36 h-8 text-xs">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All systems</SelectItem>
              <SelectItem value="high">High-risk only</SelectItem>
              <SelectItem value="limited">Limited-risk</SelectItem>
              <SelectItem value="minimal">Minimal-risk</SelectItem>
            </SelectContent>
          </Select>
        </CardHeader>
        <CardContent className="pt-4">
          <RiskTopology systems={systems} filter={riskFilter} />
        </CardContent>
      </Card>

      <Card>
        <CardHeader className="pb-0">
          <CardTitle className="text-base">Release Gate</CardTitle>
          <p className="text-sm text-muted-foreground mt-0.5">
            Combined compliance evidence, eval regression, data drift, and human oversight.
          </p>
        </CardHeader>
        <CardContent className="pt-4">
          <ReleaseGateTable systems={systems} />
        </CardContent>
      </Card>
    </div>
  );
}
