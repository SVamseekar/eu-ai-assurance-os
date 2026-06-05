import { RiskBadge } from "./risk-badge";
import { DecisionBadge } from "./decision-badge";
import { normaliseDecision } from "@/lib/utils";
import type { AiSystem } from "@/lib/types";

interface SystemCardProps {
  system: AiSystem;
}

export function SystemCard({ system }: SystemCardProps) {
  const decision = normaliseDecision(system.releaseDecision);

  return (
    <div className="bg-card rounded-xl border border-border p-5 hover:shadow-sm transition-shadow">
      <div className="flex items-start justify-between gap-3 mb-3">
        <div className="min-w-0">
          <div className="flex items-center gap-2 flex-wrap mb-1.5">
            <RiskBadge risk={system.riskClass} />
            <span className="text-xs text-muted-foreground">{system.owner}</span>
          </div>
          <h3 className="font-semibold text-sm leading-snug">{system.name}</h3>
        </div>
        <DecisionBadge decision={decision} className="flex-shrink-0 mt-0.5" />
      </div>

      <p className="text-xs text-muted-foreground mb-4 leading-relaxed line-clamp-2">
        {system.riskBasis}
      </p>

      <div>
        <div className="flex items-center justify-between mb-1.5">
          <span className="text-xs text-muted-foreground">Evidence coverage</span>
          <span className="text-xs font-semibold">{system.evidenceCoverage}%</span>
        </div>
        <div className="h-1.5 rounded-full bg-muted overflow-hidden">
          <div
            className="h-full rounded-full bg-primary transition-all"
            style={{ width: `${system.evidenceCoverage}%` }}
          />
        </div>
      </div>

      {system.openGaps.length > 0 && (
        <p className="text-xs text-muted-foreground mt-3 truncate">
          <span className="text-amber-600 dark:text-amber-400 font-medium">
            {system.openGaps.length} gap{system.openGaps.length !== 1 ? "s" : ""}:
          </span>{" "}
          {system.openGaps[0]}
          {system.openGaps.length > 1 && ` +${system.openGaps.length - 1} more`}
        </p>
      )}
    </div>
  );
}
