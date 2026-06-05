import { cn } from "@/lib/utils";
import type { RiskClass } from "@/lib/types";

interface RiskBadgeProps {
  risk: RiskClass;
  className?: string;
}

export function RiskBadge({ risk, className }: RiskBadgeProps) {
  return (
    <span
      className={cn(
        "inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold capitalize",
        risk === "high" &&
          "bg-red-50 text-red-700 dark:bg-red-950/50 dark:text-red-400",
        risk === "limited" &&
          "bg-amber-50 text-amber-700 dark:bg-amber-950/50 dark:text-amber-400",
        risk === "minimal" &&
          "bg-emerald-50 text-emerald-700 dark:bg-emerald-950/50 dark:text-emerald-400",
        risk === "prohibited" &&
          "bg-red-100 text-red-900 dark:bg-red-900/60 dark:text-red-200",
        className
      )}
    >
      {risk}
    </span>
  );
}
