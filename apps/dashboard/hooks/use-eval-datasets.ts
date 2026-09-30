import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";

export function useEvalDatasets() {
  return useQuery({ queryKey: ["eval-datasets"], queryFn: api.evals.datasets });
}
