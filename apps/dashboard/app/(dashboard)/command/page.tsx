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

  return (
    <div className="space-y-4">
      <div className="grid grid-cols-4 gap-4">
        <Card>
          <CardContent className="pt-5">
            <p className="text-xs font-black uppercase text-muted-foreground">AI systems</p>
            <p className="text-4xl font-bold my-2">{systems.length}</p>
            <p className="text-sm text-muted-foreground">{highRisk} high-risk</p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-5">
            <p className="text-xs font-black uppercase text-muted-foreground">Open control gaps</p>
            <p className="text-4xl font-bold my-2">{totalGaps}</p>
            <p className="text-sm text-red-600 dark:text-red-400">{blocked} release blockers</p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-5">
            <p className="text-xs font-black uppercase text-muted-foreground">Eval gate pass rate</p>
            <p className="text-4xl font-bold my-2">{avgEval}%</p>
            <p className="text-sm text-amber-600 dark:text-amber-400">target 85%</p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-5">
            <p className="text-xs font-black uppercase text-muted-foreground">Audit completeness</p>
            <p className="text-4xl font-bold my-2">{Math.min(98, avgEvidence + 5)}%</p>
            <p className="text-sm text-emerald-600 dark:text-emerald-400">ready for review</p>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader className="flex-row items-center justify-between pb-3">
          <div>
            <CardTitle>Risk Topology</CardTitle>
            <p className="text-sm text-muted-foreground mt-1">
              Controls grouped by system risk, data criticality, and release readiness.
            </p>
          </div>
          <Select value={riskFilter} onValueChange={(v) => v && setRiskFilter(v as typeof riskFilter)}>
            <SelectTrigger className="w-40">
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

      <Card>
        <CardHeader className="pb-3">
          <CardTitle>Release Gate</CardTitle>
          <p className="text-sm text-muted-foreground">
            Combined view of compliance evidence, eval regression, data drift, and human oversight.
          </p>
        </CardHeader>
        <CardContent>
          <ReleaseGateTable systems={systems} />
        </CardContent>
      </Card>
    </div>
  );
}
