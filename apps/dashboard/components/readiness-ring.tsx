"use client";

import { cn } from "@/lib/utils";
import type { CertificationReadinessStatus } from "@/lib/types";

interface ReadinessRingProps {
  score: number;
  status: CertificationReadinessStatus;
  size?: number;
  className?: string;
}

const STATUS_COLOR: Record<CertificationReadinessStatus, string> = {
  NOT_READY: "oklch(0.57 0.22 25)",
  GAPS: "oklch(0.72 0.17 55)",
  READY_FOR_REVIEW: "oklch(0.62 0.18 160)",
};

const STATUS_LABEL: Record<CertificationReadinessStatus, string> = {
  NOT_READY: "Not ready",
  GAPS: "Gaps remain",
  READY_FOR_REVIEW: "Ready for review",
};

/**
 * Circular readiness score ring. Labels certification readiness only —
 * never implies legal certification.
 */
export function ReadinessRing({ score, status, size = 88, className }: ReadinessRingProps) {
  const r = 36;
  const circ = 2 * Math.PI * r;
  const clamped = Math.max(0, Math.min(100, score));
  const stroke = (clamped / 100) * circ;
  const color = STATUS_COLOR[status] ?? STATUS_COLOR.GAPS;

  return (
    <div className={cn("flex flex-col items-center gap-1.5", className)}>
      <div className="relative" style={{ width: size, height: size }}>
        <svg viewBox="0 0 100 100" className="w-full h-full -rotate-90">
          <circle
            cx="50"
            cy="50"
            r={r}
            fill="transparent"
            stroke="var(--border)"
            strokeWidth="8"
          />
          <circle
            cx="50"
            cy="50"
            r={r}
            fill="transparent"
            stroke={color}
            strokeWidth="8"
            strokeLinecap="round"
            strokeDasharray={`${stroke} ${circ}`}
          />
        </svg>
        <div className="absolute inset-0 flex flex-col items-center justify-center">
          <span className="text-lg font-bold tabular-nums leading-none">{clamped}</span>
          <span className="text-[9px] text-muted-foreground mt-0.5">/ 100</span>
        </div>
      </div>
      <span
        className={cn(
          "text-[10px] font-semibold rounded-full px-2 py-0.5",
          status === "NOT_READY" && "bg-red-50 text-red-700 dark:bg-red-950/40 dark:text-red-300",
          status === "GAPS" && "bg-amber-50 text-amber-800 dark:bg-amber-950/40 dark:text-amber-300",
          status === "READY_FOR_REVIEW" &&
            "bg-emerald-50 text-emerald-800 dark:bg-emerald-950/40 dark:text-emerald-300"
        )}
      >
        {STATUS_LABEL[status]}
      </span>
    </div>
  );
}
