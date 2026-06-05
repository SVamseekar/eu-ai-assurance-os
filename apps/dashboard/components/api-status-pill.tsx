import { cn } from "@/lib/utils";

interface ApiStatusPillProps {
  online: boolean;
}

export function ApiStatusPill({ online }: ApiStatusPillProps) {
  return (
    <span
      className={cn(
        "inline-flex items-center gap-1.5 rounded-full border px-2.5 py-1 text-xs font-bold",
        online
          ? "border-emerald-300 text-emerald-700 dark:border-emerald-700 dark:text-emerald-400"
          : "border-border text-muted-foreground"
      )}
    >
      <span
        className={cn(
          "inline-block w-1.5 h-1.5 rounded-full",
          online ? "bg-emerald-500" : "bg-muted-foreground"
        )}
      />
      {online ? "API connected" : "Demo mode"}
    </span>
  );
}
