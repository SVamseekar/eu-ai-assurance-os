import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";
import { MOCK_DRIFT_EVENTS } from "@/lib/mock-data";
import { isLiveEntityId } from "@/lib/ids";

export function useDriftEvents(contractId: string) {
  return useQuery({
    queryKey: ["drift-events", contractId],
    queryFn: () => api.contracts.driftEvents(contractId),
    placeholderData: MOCK_DRIFT_EVENTS.filter((e) => e.contractId === contractId),
    enabled: isLiveEntityId(contractId),
  });
}
