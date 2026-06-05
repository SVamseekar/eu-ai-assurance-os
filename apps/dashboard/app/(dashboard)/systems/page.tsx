"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { SystemCard } from "@/components/system-card";
import { useSystems } from "@/hooks/use-systems";
import { MOCK_SYSTEMS } from "@/lib/mock-data";

export default function SystemsPage() {
  const { data: systems = MOCK_SYSTEMS } = useSystems();

  return (
    <Card>
      <CardHeader className="pb-3">
        <CardTitle>AI System Registry</CardTitle>
        <p className="text-sm text-muted-foreground">
          Inventory for providers, deployers, owners, use purpose, and risk basis.
        </p>
      </CardHeader>
      <CardContent>
        <div className="grid grid-cols-3 gap-4">
          {systems.map((system) => (
            <SystemCard key={system.id} system={system} />
          ))}
        </div>
      </CardContent>
    </Card>
  );
}
