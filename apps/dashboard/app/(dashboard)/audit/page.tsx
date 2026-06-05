"use client";

import { useAuditEvents } from "@/hooks/use-audit-events";
import { MOCK_AUDIT_EVENTS } from "@/lib/mock-data";
import { formatDate } from "@/lib/utils";
import { ShieldCheck, FileText, AlertTriangle, CheckCircle2 } from "lucide-react";

const EVENT_CONFIG: Record<string, { icon: React.ElementType; color: string; bg: string }> = {
  RELEASE_GATE_CALCULATED: {
    icon: ShieldCheck,
    color: "text-indigo-600 dark:text-indigo-400",
    bg: "bg-indigo-50 dark:bg-indigo-950/50",
  },
  EVIDENCE_INDEXED: {
    icon: FileText,
    color: "text-sky-600 dark:text-sky-400",
    bg: "bg-sky-50 dark:bg-sky-950/50",
  },
  EVIDENCE_QUERIED: {
    icon: FileText,
    color: "text-sky-600 dark:text-sky-400",
    bg: "bg-sky-50 dark:bg-sky-950/50",
  },
  DRIFT_EVENT_CREATED: {
    icon: AlertTriangle,
    color: "text-amber-600 dark:text-amber-400",
    bg: "bg-amber-50 dark:bg-amber-950/50",
  },
  CONTRACT_UPDATED: {
    icon: CheckCircle2,
    color: "text-emerald-600 dark:text-emerald-400",
    bg: "bg-emerald-50 dark:bg-emerald-950/50",
  },
};

const DEFAULT_CFG = {
  icon: ShieldCheck,
  color: "text-muted-foreground",
  bg: "bg-muted",
};

export default function AuditPage() {
  const { data: events = MOCK_AUDIT_EVENTS } = useAuditEvents();

  return (
    <div className="bg-card rounded-2xl border border-border overflow-hidden">
      <div className="px-5 pt-5 pb-4 border-b border-border">
        <p className="text-sm font-semibold">Immutable Audit Ledger</p>
        <p className="text-xs text-muted-foreground mt-0.5">
          Append-only record of evidence checks, approvals, overrides, and release decisions.
        </p>
      </div>
      <div className="divide-y divide-border">
        {events.map((event) => {
          const cfg = EVENT_CONFIG[event.eventType] ?? DEFAULT_CFG;
          const Icon = cfg.icon;
          return (
            <div key={event.id} className="flex items-start gap-4 px-5 py-4 hover:bg-muted/20 transition-colors">
              <div className={`w-8 h-8 rounded-lg flex items-center justify-center flex-shrink-0 mt-0.5 ${cfg.bg}`}>
                <Icon className={`w-4 h-4 ${cfg.color}`} />
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium">
                  {event.eventType.replace(/_/g, " ").toLowerCase().replace(/\b\w/g, (c) => c.toUpperCase())}
                </p>
                {event.resourceId && (
                  <p className="text-xs text-muted-foreground mt-0.5">
                    {event.resourceType}
                    {" · "}
                    <span className="font-mono">{event.resourceId}</span>
                  </p>
                )}
              </div>
              <time className="text-xs text-muted-foreground flex-shrink-0 mt-0.5 tabular-nums">
                {formatDate(event.createdAt)}
              </time>
            </div>
          );
        })}
      </div>
    </div>
  );
}
