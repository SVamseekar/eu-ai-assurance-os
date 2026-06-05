import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
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
    <Card>
      <CardHeader className="pb-2">
        <div className="flex items-center gap-2 flex-wrap mb-1">
          <RiskBadge risk={system.riskClass} />
          <Badge variant="outline" className="text-muted-foreground">
            {system.owner}
          </Badge>
        </div>
        <h3 className="font-bold text-base">{system.name}</h3>
        <p className="text-sm text-muted-foreground">{system.riskBasis}</p>
      </CardHeader>
      <CardContent>
        <p className="text-xs font-bold uppercase text-muted-foreground mb-1.5">
          Assurance coverage
        </p>
        <div className="h-2 rounded-full bg-muted overflow-hidden mb-3">
          <div
            className="h-full rounded-full bg-gradient-to-r from-cyan-700 to-teal-600"
            style={{ width: `${system.evidenceCoverage}%` }}
          />
        </div>
        <div className="flex items-center justify-between gap-2">
          <p className="text-sm text-muted-foreground truncate">
            {system.openGaps.length ? system.openGaps.join("; ") : "No open gaps."}
          </p>
          <DecisionBadge decision={decision} />
        </div>
      </CardContent>
    </Card>
  );
}
