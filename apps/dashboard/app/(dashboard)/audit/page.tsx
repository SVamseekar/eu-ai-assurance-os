"use client";

import { useAuditEvents } from "@/hooks/use-audit-events";
import { MOCK_AUDIT_EVENTS } from "@/lib/mock-data";
import { formatDate } from "@/lib/utils";
import { ShieldCheck, FileText, AlertTriangle, CheckCircle } from "lucide-react";

const EVENT_ICON: Record<string, React.ElementType> = {
  RELEASE_GATE_CALCULATED: ShieldCheck,
  EVIDENCE_INDEXED: FileText,
  EVIDENCE_QUERIED: FileText,
  DRIFT_EVENT_CREATED: AlertTriangle,
  CONTRACT_UPDATED: CheckCircle,
};

const EVENT_COLOR: Record<string, string> = {
  RELEASE_GATE_CALCULATED: "text-indigo-600 bg-indigo-50 dark:text-indigo-400 dark:bg-indigo-950/50",
  EVIDENCE_INDEXED: "text-sky-600 bg-sky-50 dark:text-sky-400 dark:bg-sky-950/50",
  EVIDENCE_QUERIED: "text-sky-600 bg-sky-50 dark:text-sky-400 dark:bg-sky-950/50",
  DRIFT_EVENT_CREATED: "text-amber-600 bg-amber-50 dark:text-amber-400 dark:bg-amber-950/50",
  CONTRACT_UPDATED: "text-emerald-600 bg-emerald-50 dark:text-emerald-400 dark:bg-emerald-950/50",
};

export default function AuditPage() {
  const { data: events = MOCK_AUDIT_EVENTS } = useAuditEvents();

  return (
    <div className="bg-card rounded-xl border border-border divide-y divide-border overflow-hidden">
      {events.map((event) => {
        const Icon = EVENT_ICON[event.eventType] ?? ShieldCheck;
        const colorCls = EVENT_COLOR[event.eventType] ?? "text-muted-foreground bg-muted";
        return (
          <div key={event.id} className="flex items-start gap-4 px-5 py-4 hover:bg-muted/20 transition-colors">
            <div className={`w-8 h-8 rounded-lg grid place-items-center flex-shrink-0 mt-0.5 ${colorCls}`}>
              <Icon className="w-4 h-4" />
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-sm font-semibold leading-snug">{event.eventType.replace(/_/g, " ")}</p>
              {event.resourceId && (
                <p className="text-xs text-muted-foreground mt-0.5">
                  {event.resourceType} · <span className="font-mono">{event.resourceId}</span>
                </p>
              )}
            </div>
            <time className="text-xs text-muted-foreground flex-shrink-0 mt-0.5">
              {formatDate(event.createdAt)}
            </time>
          </div>
        );
      })}
    </div>
  );
}
