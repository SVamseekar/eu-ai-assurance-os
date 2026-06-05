"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useAuditEvents } from "@/hooks/use-audit-events";
import { MOCK_AUDIT_EVENTS } from "@/lib/mock-data";
import { formatDate } from "@/lib/utils";

export default function AuditPage() {
  const { data: events = MOCK_AUDIT_EVENTS } = useAuditEvents();

  return (
    <Card>
      <CardHeader className="pb-3">
        <CardTitle>Immutable Audit Ledger</CardTitle>
        <p className="text-sm text-muted-foreground">
          Append-only entries for evidence checks, approvals, overrides, and release decisions.
        </p>
      </CardHeader>
      <CardContent>
        <ol className="space-y-2">
          {events.map((event) => (
            <li
              key={event.id}
              className="border border-border rounded-lg px-4 py-3 bg-muted/20"
            >
              <time className="block text-xs text-muted-foreground mb-1">
                {formatDate(event.createdAt)}
              </time>
              <p className="text-sm font-semibold">{event.eventType}</p>
              {event.resourceId && (
                <p className="text-xs text-muted-foreground mt-0.5">
                  {event.resourceType} · {event.resourceId}
                </p>
              )}
            </li>
          ))}
        </ol>
      </CardContent>
    </Card>
  );
}
