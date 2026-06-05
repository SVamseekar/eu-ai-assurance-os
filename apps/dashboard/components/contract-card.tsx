import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { cn, contractStatusColor } from "@/lib/utils";
import type { DataContract, DriftEvent } from "@/lib/types";

interface ContractCardProps {
  contract: DataContract;
  driftEvents: DriftEvent[];
}

export function ContractCard({ contract, driftEvents }: ContractCardProps) {
  const openEvents = driftEvents.filter((e) => e.status === "OPEN");

  return (
    <Card
      className={cn(
        contract.status === "BREACH" && "border-red-300 dark:border-red-800",
        contract.status === "WARNING" && "border-amber-300 dark:border-amber-800"
      )}
    >
      <CardHeader className="pb-2">
        <div className="flex items-center justify-between">
          <span className="text-xs text-muted-foreground font-bold">{contract.owner}</span>
          <Badge
            variant="outline"
            className={cn("font-bold text-xs", contractStatusColor(contract.status))}
          >
            {contract.status}
          </Badge>
        </div>
        <h3 className="font-bold text-sm font-mono">{contract.name}</h3>
        <p className="text-xs text-muted-foreground">
          v{contract.version} · {contract.coverage}% coverage
        </p>
      </CardHeader>
      {openEvents.length > 0 && (
        <CardContent>
          <div className="space-y-2">
            {openEvents.map((event) => (
              <div
                key={event.id}
                className="border-l-2 border-red-400 pl-3 text-xs text-muted-foreground"
              >
                <span className="font-bold text-foreground">{event.field ?? "schema"}</span>
                {" — "}
                {event.description}
              </div>
            ))}
          </div>
        </CardContent>
      )}
    </Card>
  );
}
