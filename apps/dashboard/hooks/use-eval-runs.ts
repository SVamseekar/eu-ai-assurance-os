import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";

export function useEvalOperations() {
  return useQuery({
    queryKey: ["eval-operations"],
    queryFn: api.evals.operations,
    refetchInterval: 10_000,
  });
}
