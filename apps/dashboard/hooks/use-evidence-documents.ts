import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";
import { isLiveEntityId } from "@/lib/ids";

export function useEvidenceDocuments(systemId: string | undefined) {
  return useQuery({
    queryKey: ["evidence-documents", systemId],
    queryFn: () => api.evidence.documents(systemId!),
    enabled: isLiveEntityId(systemId),
    placeholderData: [],
  });
}
