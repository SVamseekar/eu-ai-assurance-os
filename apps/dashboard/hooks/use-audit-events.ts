import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";
import { MOCK_AUDIT_EVENTS } from "@/lib/mock-data";

export function useAuditEvents(systemId?: string) {
  return useQuery({
    queryKey: ["audit-events", systemId],
    queryFn: () => api.audit.list(systemId),
    placeholderData: MOCK_AUDIT_EVENTS,
    refetchInterval: 15_000,
  });
}
