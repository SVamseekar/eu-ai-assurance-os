import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";
import { MOCK_SYSTEMS } from "@/lib/mock-data";

export function useSystems() {
  return useQuery({
    queryKey: ["systems"],
    queryFn: api.systems.list,
    placeholderData: MOCK_SYSTEMS,
  });
}
