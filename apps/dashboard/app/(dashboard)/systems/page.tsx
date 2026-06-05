"use client";

import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { SystemCard } from "@/components/system-card";
import { useSystems } from "@/hooks/use-systems";
import { MOCK_SYSTEMS } from "@/lib/mock-data";

export default function SystemsPage() {
  const { data: systems = MOCK_SYSTEMS } = useSystems();

  return (
    <Card>
      <CardHeader>
        <CardTitle>Registered Systems</CardTitle>
        <CardDescription>
          {systems.length} system{systems.length !== 1 ? "s" : ""} across all risk classes
        </CardDescription>
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
