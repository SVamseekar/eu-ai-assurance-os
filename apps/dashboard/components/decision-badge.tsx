import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";

interface DecisionBadgeProps {
  decision: "Pass" | "Review" | "Blocked";
  className?: string;
}

export function DecisionBadge({ decision, className }: DecisionBadgeProps) {
  return (
    <Badge
      variant="outline"
      className={cn(
        "font-bold",
        decision === "Pass" &&
          "border-emerald-300 text-emerald-700 dark:border-emerald-700 dark:text-emerald-400",
        decision === "Review" &&
          "border-amber-300 text-amber-700 dark:border-amber-700 dark:text-amber-400",
        decision === "Blocked" &&
          "border-red-300 text-red-700 dark:border-red-700 dark:text-red-400",
        className
      )}
    >
      {decision}
    </Badge>
  );
}
