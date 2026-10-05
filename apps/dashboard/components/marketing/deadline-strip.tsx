import { ExternalLink } from "lucide-react";

import { DEADLINES, daysUntil, deadlineStatus, formatDeadlineDate, type Deadline } from "@/lib/deadlines";
import { cn } from "@/lib/utils";

/**
 * Every regulatory date as "law · label · date · Applies now / In N days", each with its official source.
 * Rendered on the server; pages that show it revalidate daily so "In N days" stays current.
 */
export function DeadlineStrip({ deadlines = DEADLINES, today = new Date() }: { deadlines?: Deadline[]; today?: Date }) {
  return (
    <ol className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
      {deadlines.map((d) => {
        const now = deadlineStatus(d, today) === "applies-now";
        return (
          <li key={d.id} className="flex flex-col rounded-xl border border-line bg-white p-4">
            <div className="flex items-center justify-between gap-2">
              <span className="text-xs font-semibold uppercase tracking-wide text-ink-muted">{d.law}</span>
              <span
                className={cn(
                  "rounded-full px-2 py-0.5 text-[11px] font-semibold",
                  now ? "bg-review-soft text-review" : "bg-brand-soft text-brand",
                )}
              >
                {now ? "Applies now" : `In ${daysUntil(d, today)} days`}
              </span>
            </div>
            <p className="mt-2 text-sm font-semibold text-ink">{d.label}</p>
            <p className="text-sm text-ink-muted">{formatDeadlineDate(d.date)}</p>
            <a
              href={d.source.url}
              target="_blank"
              rel="noopener noreferrer"
              className="mt-3 inline-flex items-center gap-1 text-xs font-medium text-brand hover:underline"
            >
              {d.source.title}
              <ExternalLink className="h-3 w-3" aria-label="(opens in a new tab)" />
            </a>
          </li>
        );
      })}
    </ol>
  );
}
