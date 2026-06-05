import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { RiskBadge } from "./risk-badge";
import { DecisionBadge } from "./decision-badge";
import { normaliseDecision } from "@/lib/utils";
import type { AiSystem } from "@/lib/types";

interface ReleaseGateTableProps {
  systems: AiSystem[];
}

export function ReleaseGateTable({ systems }: ReleaseGateTableProps) {
  return (
    <div className="overflow-x-auto border border-border rounded-lg">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>System</TableHead>
            <TableHead>Risk class</TableHead>
            <TableHead>Evidence</TableHead>
            <TableHead>Eval score</TableHead>
            <TableHead>Data contract</TableHead>
            <TableHead>Decision</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {systems.map((system) => {
            const decision = normaliseDecision(system.releaseDecision);
            return (
              <TableRow key={system.id}>
                <TableCell>
                  <p className="font-semibold">{system.name}</p>
                  <p className="text-xs text-muted-foreground">{system.owner}</p>
                </TableCell>
                <TableCell>
                  <RiskBadge risk={system.riskClass} />
                  <p className="text-xs text-muted-foreground mt-1">{system.riskBasis}</p>
                </TableCell>
                <TableCell>{system.evidenceCoverage}%</TableCell>
                <TableCell>{system.evalScore}%</TableCell>
                <TableCell>
                  <span
                    className={
                      system.dataContractStatus === "BREACH"
                        ? "text-red-600 dark:text-red-400 font-semibold"
                        : system.dataContractStatus === "WARNING"
                        ? "text-amber-600 dark:text-amber-400 font-semibold"
                        : "text-emerald-600 dark:text-emerald-400"
                    }
                  >
                    {system.dataContractStatus}
                  </span>
                </TableCell>
                <TableCell>
                  <DecisionBadge decision={decision} />
                </TableCell>
              </TableRow>
            );
          })}
        </TableBody>
      </Table>
    </div>
  );
}
