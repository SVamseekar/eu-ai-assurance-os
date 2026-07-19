"use client";

import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { ApprovalWorkflow } from "@/lib/types";

export function useOpenWorkflows() {
  return useQuery<ApprovalWorkflow[]>({
    queryKey: ["workflows", "open"],
    queryFn: () => api.workflows.open(),
    staleTime: 30_000,
    retry: 1,
    // Offline/demo: empty list rather than erroring the command page
    placeholderData: [],
  });
}
