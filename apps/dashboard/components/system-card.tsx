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
    <div className="rounded-xl border border-border p-4 bg-muted/30">
      {/* Top row */}
      <div className="flex items-start justify-between gap-3 mb-4">
        <div className="min-w-0">
          <h3 className="text-sm font-semibold leading-snug mb-1.5">{system.name}</h3>
          <div className="flex items-center gap-2">
            <RiskBadge risk={system.riskClass} />
            <span className="text-xs text-muted-foreground">{system.owner}</span>
          </div>
        </div>
        <DecisionBadge decision={decision} className="flex-shrink-0" />
      </div>

      {/* Risk basis */}
      <p className="text-xs text-muted-foreground leading-relaxed mb-4 line-clamp-2">
        {system.riskBasis}
      </p>

      {/* Evidence bar */}
      <div className="space-y-1.5">
        <div className="flex justify-between items-center">
          <span className="text-xs text-muted-foreground">Evidence coverage</span>
          <span className="text-xs font-semibold">{system.evidenceCoverage}%</span>
        </div>
        <div className="h-1 rounded-full bg-muted overflow-hidden">
          <div
            className="h-full rounded-full bg-primary transition-all"
            style={{ width: `${system.evidenceCoverage}%` }}
          />
        </div>
      </div>

      {/* Open gaps */}
      {system.openGaps.length > 0 && (
        <div className="mt-3 pt-3 border-t border-border">
          <p className="text-xs text-muted-foreground">
            <span className="text-amber-600 dark:text-amber-400 font-medium">
              {system.openGaps.length} gap{system.openGaps.length !== 1 ? "s" : ""}
            </span>
            {" — "}
            {system.openGaps[0]}
            {system.openGaps.length > 1 && (
              <span className="text-muted-foreground"> +{system.openGaps.length - 1} more</span>
            )}
          </p>
        </div>
      )}
    </div>
  );
}
