import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { ProposalList } from "@/lib/types";
import { isLiveEntityId } from "@/lib/ids";

export function useProposals(systemId: string | null | undefined) {
  return useQuery<ProposalList>({
    queryKey: ["proposals", systemId],
    queryFn: () => api.proposals.list(systemId!),
    enabled: isLiveEntityId(systemId),
  });
}

export function useProposalActions(systemId: string) {
  const qc = useQueryClient();
  const invalidate = () => {
    void qc.invalidateQueries({ queryKey: ["proposals", systemId] });
  };
  const create = useMutation({
    mutationFn: (payload: {
      relation: string;
      provisionKey?: string;
      excerpt?: string;
      corpusVersion?: string;
    }) => api.proposals.create(systemId, payload),
    onSuccess: invalidate,
  });
  const accept = useMutation({
    mutationFn: (proposalId: string) => api.proposals.accept(systemId, proposalId),
    onSuccess: invalidate,
  });
  const reject = useMutation({
    mutationFn: (proposalId: string) => api.proposals.reject(systemId, proposalId),
    onSuccess: invalidate,
  });
  const mapDocuments = useMutation({
    mutationFn: (documents: { title: string; text: string }[]) =>
      api.proposals.mapDocuments(systemId, documents),
    onSuccess: invalidate,
  });
  return { create, accept, reject, mapDocuments };
}
