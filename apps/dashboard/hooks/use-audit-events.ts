import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";
import { allowMockFallback } from "@/lib/live-mode";
import { MOCK_AUDIT_EVENTS } from "@/lib/mock-data";

export function useAuditEvents(
  systemId?: string,
  options?: { refetchInterval?: number | false },
) {
  return useQuery({
    queryKey: ["audit-events", systemId],
    queryFn: () => api.audit.list(systemId),
    placeholderData: allowMockFallback() ? MOCK_AUDIT_EVENTS : undefined,
    refetchInterval: options?.refetchInterval ?? false,
  });
}
