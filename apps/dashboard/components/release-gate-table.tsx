import { RiskBadge } from "./risk-badge";
import { DecisionBadge } from "./decision-badge";
import { normaliseDecision } from "@/lib/utils";
import type { AiSystem } from "@/lib/types";
import { cn } from "@/lib/utils";

interface ReleaseGateTableProps {
  systems: AiSystem[];
}

export function ReleaseGateTable({ systems }: ReleaseGateTableProps) {
  return (
    <div className="overflow-x-auto rounded-xl border border-border">
      <table className="w-full text-sm">
        <thead>
          <tr className="bg-muted/40 border-b border-border">
            <th className="text-left px-4 py-3 text-xs font-semibold text-muted-foreground uppercase tracking-wide">
              System
            </th>
            <th className="text-left px-4 py-3 text-xs font-semibold text-muted-foreground uppercase tracking-wide">
              Risk class
            </th>
            <th className="text-left px-4 py-3 text-xs font-semibold text-muted-foreground uppercase tracking-wide">
              Evidence
            </th>
            <th className="text-left px-4 py-3 text-xs font-semibold text-muted-foreground uppercase tracking-wide">
              Eval score
            </th>
            <th className="text-left px-4 py-3 text-xs font-semibold text-muted-foreground uppercase tracking-wide">
              Data contract
            </th>
            <th className="text-left px-4 py-3 text-xs font-semibold text-muted-foreground uppercase tracking-wide">
              Decision
            </th>
          </tr>
        </thead>
        <tbody>
          {systems.map((system, i) => {
            const decision = normaliseDecision(system.releaseDecision);
            return (
              <tr
                key={system.id}
                className={cn(
                  "border-b border-border last:border-0 hover:bg-muted/30 transition-colors",
                  i % 2 === 0 ? "bg-card" : "bg-muted/10"
                )}
              >
                <td className="px-4 py-3.5">
                  <p className="font-semibold text-sm">{system.name}</p>
                  <p className="text-xs text-muted-foreground mt-0.5">{system.owner}</p>
                </td>
                <td className="px-4 py-3.5">
                  <RiskBadge risk={system.riskClass} />
                  {system.riskBasis && (
                    <p className="text-xs text-muted-foreground mt-1 max-w-40 truncate">{system.riskBasis}</p>
                  )}
                </td>
                <td className="px-4 py-3.5">
                  <div className="flex items-center gap-2">
                    <div className="w-16 h-1.5 rounded-full bg-muted overflow-hidden">
                      <div
                        className="h-full rounded-full bg-primary"
                        style={{ width: `${system.evidenceCoverage}%` }}
                      />
                    </div>
                    <span className="text-sm font-medium">{system.evidenceCoverage}%</span>
                  </div>
                </td>
                <td className="px-4 py-3.5">
                  <span
                    className={cn(
                      "font-semibold",
                      system.evalScore >= 85 ? "text-emerald-600 dark:text-emerald-400" : "text-amber-600 dark:text-amber-400"
                    )}
                  >
                    {system.evalScore}%
                  </span>
                </td>
                <td className="px-4 py-3.5">
                  <span
                    className={cn(
                      "inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold",
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
                <td className="px-4 py-3.5">
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
