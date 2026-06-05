import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";

export function useEvidenceDocuments(systemId: string | undefined) {
  return useQuery({
    queryKey: ["evidence-documents", systemId],
    queryFn: () => api.evidence.documents(systemId!),
    enabled: !!systemId,
    placeholderData: [],
  });
}
