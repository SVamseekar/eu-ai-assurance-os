"use client";

import { SystemCard } from "@/components/system-card";
import { useSystems } from "@/hooks/use-systems";
import { MOCK_SYSTEMS } from "@/lib/mock-data";

export default function SystemsPage() {
  const { data: systems = MOCK_SYSTEMS } = useSystems();

  return (
    <div className="grid grid-cols-3 gap-4">
      {systems.map((system) => (
        <SystemCard key={system.id} system={system} />
      ))}
    </div>
  );
}
