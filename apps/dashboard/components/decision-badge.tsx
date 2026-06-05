import { cn } from "@/lib/utils";

interface DecisionBadgeProps {
  decision: "Pass" | "Review" | "Blocked";
  className?: string;
}

export function DecisionBadge({ decision, className }: DecisionBadgeProps) {
  return (
    <span
      className={cn(
        "inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-semibold",
        decision === "Pass" &&
          "bg-emerald-50 text-emerald-700 dark:bg-emerald-950/50 dark:text-emerald-400",
        decision === "Review" &&
          "bg-amber-50 text-amber-700 dark:bg-amber-950/50 dark:text-amber-400",
        decision === "Blocked" &&
          "bg-red-50 text-red-700 dark:bg-red-950/50 dark:text-red-400",
        className
      )}
    >
      <span
        className={cn(
          "w-1.5 h-1.5 rounded-full flex-shrink-0",
          decision === "Pass" && "bg-emerald-500",
          decision === "Review" && "bg-amber-500",
          decision === "Blocked" && "bg-red-500"
        )}
      />
      {decision}
    </span>
  );
}
