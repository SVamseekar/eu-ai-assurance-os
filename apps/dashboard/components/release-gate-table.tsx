import { RiskBadge } from "./risk-badge";
import { DecisionBadge } from "./decision-badge";
import { normaliseDecision, cn } from "@/lib/utils";
import type { AiSystem } from "@/lib/types";

interface ReleaseGateTableProps {
  systems: AiSystem[];
}

export function ReleaseGateTable({ systems }: ReleaseGateTableProps) {
  return (
    <div className="overflow-x-auto">
      <table className="w-full text-sm">
        <thead>
          <tr className="border-b border-border">
            <th className="text-left pb-3 text-xs font-medium text-muted-foreground pr-4">System</th>
            <th className="text-left pb-3 text-xs font-medium text-muted-foreground pr-4">Risk</th>
            <th className="text-left pb-3 text-xs font-medium text-muted-foreground pr-4">Evidence</th>
            <th className="text-left pb-3 text-xs font-medium text-muted-foreground pr-4">Eval</th>
            <th className="text-left pb-3 text-xs font-medium text-muted-foreground pr-4">Contract</th>
            <th className="text-left pb-3 text-xs font-medium text-muted-foreground">Decision</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-border">
          {systems.map((system) => {
            const decision = normaliseDecision(system.releaseDecision);
            return (
              <tr key={system.id} className="hover:bg-muted/30 transition-colors">
                <td className="py-3.5 pr-4">
                  <p className="font-medium text-sm">{system.name}</p>
                  <p className="text-xs text-muted-foreground mt-0.5">{system.owner}</p>
                </td>
                <td className="py-3.5 pr-4">
                  <RiskBadge risk={system.riskClass} />
                </td>
                <td className="py-3.5 pr-4">
                  <div className="flex items-center gap-2.5">
                    <div className="w-20 h-1 rounded-full bg-muted overflow-hidden">
                      <div
                        className="h-full rounded-full bg-primary"
                        style={{ width: `${system.evidenceCoverage}%` }}
                      />
                    </div>
                    <span className="text-xs text-muted-foreground">{system.evidenceCoverage}%</span>
                  </div>
                </td>
                <td className="py-3.5 pr-4">
                  <span
                    className={cn(
                      "text-sm font-medium",
                      system.evalScore >= 85
                        ? "text-emerald-600 dark:text-emerald-400"
                        : "text-amber-600 dark:text-amber-400"
                    )}
                  >
                    {system.evalScore}%
                  </span>
                </td>
                <td className="py-3.5 pr-4">
                  <span
                    className={cn(
                      "inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium",
                      system.dataContractStatus === "BREACH" &&
                        "bg-red-50 text-red-700 dark:bg-red-950/50 dark:text-red-400",
                      system.dataContractStatus === "WARNING" &&
                        "bg-amber-50 text-amber-700 dark:bg-amber-950/50 dark:text-amber-400",
                      system.dataContractStatus === "HEALTHY" &&
                        "bg-emerald-50 text-emerald-700 dark:bg-emerald-950/50 dark:text-emerald-400"
                    )}
                  >
                    {system.dataContractStatus}
                  </span>
                </td>
                <td className="py-3.5">
                  <DecisionBadge decision={decision} />
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
