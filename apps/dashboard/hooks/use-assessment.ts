import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { Assessment } from "@/lib/types";
import { isLiveEntityId } from "@/lib/ids";

export function useAssessment(systemId: string | null | undefined) {
  return useQuery<Assessment>({
    queryKey: ["assessment", systemId],
    queryFn: () => api.systems.assessment(systemId!),
    enabled: isLiveEntityId(systemId),
  });
}
