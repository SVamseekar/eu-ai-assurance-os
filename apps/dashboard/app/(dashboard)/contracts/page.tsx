"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { ContractCard } from "@/components/contract-card";
import { LineageGraph } from "@/components/lineage-graph";
import { useContracts } from "@/hooks/use-contracts";
import { useSystems } from "@/hooks/use-systems";
import { MOCK_CONTRACTS, MOCK_DRIFT_EVENTS, MOCK_SYSTEMS } from "@/lib/mock-data";
import { useQueries } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { DriftEvent } from "@/lib/types";

export default function ContractsPage() {
  const { data: systems = MOCK_SYSTEMS } = useSystems();
  const { data: contracts = MOCK_CONTRACTS } = useContracts();

  const driftQueries = useQueries({
    queries: contracts.map((contract) => ({
      queryKey: ["drift-events", contract.id],
      queryFn: () => api.contracts.driftEvents(contract.id),
      placeholderData: MOCK_DRIFT_EVENTS.filter((e) => e.contractId === contract.id),
    })),
  });

  const allDriftEvents: DriftEvent[] = driftQueries.flatMap((q) => q.data ?? []);

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-2 gap-4">
        {contracts.map((contract) => {
          const events = allDriftEvents.filter((e) => e.contractId === contract.id);
          return <ContractCard key={contract.id} contract={contract} driftEvents={events} />;
        })}
      </div>

      <Card>
        <CardHeader className="pb-0">
          <CardTitle className="text-base">Data Lineage</CardTitle>
          <p className="text-sm text-muted-foreground mt-0.5">
            Data sources → contracts → AI systems. Animated edges indicate active drift.
          </p>
        </CardHeader>
        <CardContent className="pt-4">
          <LineageGraph systems={systems} contracts={contracts} />
        </CardContent>
      </Card>
    </div>
  );
}
