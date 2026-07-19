import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { RegMonitorFeed } from "@/lib/types";
import { MOCK_REG_MONITOR_FEED } from "@/lib/mock-data";

export function useRegMonitorItems() {
  return useQuery<RegMonitorFeed>({
    queryKey: ["reg-monitor", "items"],
    queryFn: () => api.regMonitor.items(),
    placeholderData: MOCK_REG_MONITOR_FEED,
  });
}

export function useRegMonitorRelevant(systemId: string | null | undefined) {
  return useQuery<RegMonitorFeed>({
    queryKey: ["reg-monitor", "relevant", systemId],
    queryFn: () => api.regMonitor.relevant(systemId!),
    enabled: Boolean(systemId),
    placeholderData: systemId
      ? {
          ...MOCK_REG_MONITOR_FEED,
          items: MOCK_REG_MONITOR_FEED.items.filter(
            (i) => i.relevanceReason || i.impactHints.some((h) => h.controlCode)
          ),
        }
      : undefined,
  });
}

export function useMarkRegItemReviewed() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ itemId, notes }: { itemId: string; notes?: string }) =>
      api.regMonitor.markReviewed(itemId, notes),
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: ["reg-monitor"] });
    },
  });
}
