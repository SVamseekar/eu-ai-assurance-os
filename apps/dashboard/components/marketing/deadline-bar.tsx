"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { ArrowRight, X } from "lucide-react";

import { EuFlag } from "@/components/marketing/brand-icons";
import { formatDeadlineDate, nextDeadline, type Deadline } from "@/lib/deadlines";

const STORAGE_KEY = "aos-deadline-bar-dismissed";

function remaining(target: Deadline, now: number) {
  const ms = Math.max(0, new Date(`${target.date}T00:00:00Z`).getTime() - now);
  const s = Math.floor(ms / 1000);
  return { days: Math.floor(s / 86400), hours: Math.floor((s % 86400) / 3600), mins: Math.floor((s % 3600) / 60), secs: s % 60 };
}

/** Design board item 3: next regulatory date with a live countdown. Dismissal is remembered per deadline. */
export function DeadlineBar() {
  // Picked at render so the server and the first client render agree; the countdown starts after mount.
  const [target] = useState(() => nextDeadline(new Date()));
  const [now, setNow] = useState<number | null>(null);
  const [dismissed, setDismissed] = useState(false);

  useEffect(() => {
    try {
      if (target && window.localStorage.getItem(STORAGE_KEY) === target.id) setDismissed(true);
    } catch {
      // Storage can be blocked; the bar then simply shows.
    }
    setNow(Date.now());
    const timer = setInterval(() => setNow(Date.now()), 1000);
    return () => clearInterval(timer);
  }, [target]);

  if (!target || dismissed) return null;

  const parts = now === null ? null : remaining(target, now);
  const units: [keyof NonNullable<typeof parts>, string][] = [
    ["days", "Days"],
    ["hours", "Hours"],
    ["mins", "Mins"],
    ["secs", "Secs"],
  ];

  function dismiss() {
    setDismissed(true);
    try {
      if (target) window.localStorage.setItem(STORAGE_KEY, target.id);
    } catch {
      // Ignore: dismissal lasts for this page view only.
    }
  }

  return (
    <aside aria-label="Regulatory deadline" className="border-b border-white/10 bg-navy-900 text-white">
      <div className="mx-auto flex max-w-7xl items-center gap-3 px-4 py-1.5 sm:px-6 lg:px-8">
        <EuFlag className="hidden h-5 w-7 shrink-0 rounded-[3px] sm:block" />
        <p className="min-w-0 flex-1 text-[13px] leading-snug">
          <span className="font-semibold">{target.law}</span>
          <span className="text-on-dark-muted"> · </span>
          {target.label} applies from {formatDeadlineDate(target.date)}
          <span className="mx-3 hidden text-white/20 md:inline" aria-hidden="true">
            |
          </span>
          <Link
            href="/tools/ai-act-deadlines"
            className="ml-2 inline-flex items-center gap-1 font-semibold text-periwinkle hover:underline md:ml-0"
          >
            See key deadlines
            <ArrowRight className="h-3.5 w-3.5" aria-hidden="true" />
          </Link>
        </p>
        <div className="hidden items-center gap-1.5 md:flex" aria-hidden={parts === null}>
          {units.map(([key, label]) => (
            <div
              key={key}
              className="flex w-10 flex-col items-center rounded border border-white/10 bg-white/[0.04] py-0.5"
            >
              <span className="text-[13px] font-semibold tabular-nums leading-none">
                {parts ? String(parts[key]).padStart(key === "days" ? 1 : 2, "0") : "–"}
              </span>
              <span className="text-[9px] leading-tight uppercase tracking-wider text-on-dark-muted">{label}</span>
            </div>
          ))}
        </div>
        <button
          type="button"
          onClick={dismiss}
          className="inline-flex h-7 w-7 shrink-0 items-center justify-center rounded-md text-on-dark-muted hover:bg-white/10 hover:text-white"
          aria-label="Dismiss deadline notice"
        >
          <X className="h-4 w-4" />
        </button>
      </div>
    </aside>
  );
}
