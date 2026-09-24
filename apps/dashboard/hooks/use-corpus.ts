import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { CorpusView } from "@/lib/types";

export function useCorpus() {
  return useQuery<CorpusView>({
    queryKey: ["corpus"],
    queryFn: () => api.corpus.current(),
  });
}
