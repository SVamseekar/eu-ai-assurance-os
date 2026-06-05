import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";
import type { RiskClass } from "@/lib/types";

interface RiskBadgeProps {
  risk: RiskClass;
  className?: string;
}

export function RiskBadge({ risk, className }: RiskBadgeProps) {
  return (
    <Badge
      variant="outline"
      className={cn(
        "font-bold capitalize",
        risk === "high" &&
          "border-red-300 text-red-700 dark:border-red-700 dark:text-red-400",
        risk === "limited" &&
          "border-amber-300 text-amber-700 dark:border-amber-700 dark:text-amber-400",
        risk === "minimal" &&
          "border-emerald-300 text-emerald-700 dark:border-emerald-700 dark:text-emerald-400",
        risk === "prohibited" &&
          "border-red-500 bg-red-50 text-red-800 dark:bg-red-950 dark:text-red-300",
        className
      )}
    >
      {risk}
    </Badge>
  );
}
