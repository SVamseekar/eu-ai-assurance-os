import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";
import { MOCK_CONTRACTS } from "@/lib/mock-data";

export function useContracts(systemId?: string) {
  return useQuery({
    queryKey: ["contracts", systemId],
    queryFn: () => api.contracts.list(systemId),
    placeholderData: MOCK_CONTRACTS,
  });
}
