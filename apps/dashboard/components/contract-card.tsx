import { cn } from "@/lib/utils";
import type { DataContract, DriftEvent } from "@/lib/types";
import { AlertTriangle, CheckCircle2, XCircle } from "lucide-react";

interface ContractCardProps {
  contract: DataContract;
  driftEvents: DriftEvent[];
}

const STATUS_CONFIG = {
  BREACH: {
    icon: XCircle,
    label: "Breach",
    card: "border-red-200 dark:border-red-800",
    badge: "bg-red-50 text-red-700 dark:bg-red-950/50 dark:text-red-400",
    iconColor: "text-red-500",
  },
  WARNING: {
    icon: AlertTriangle,
    label: "Warning",
    card: "border-amber-200 dark:border-amber-800",
    badge: "bg-amber-50 text-amber-700 dark:bg-amber-950/50 dark:text-amber-400",
    iconColor: "text-amber-500",
  },
  HEALTHY: {
    icon: CheckCircle2,
    label: "Healthy",
    card: "border-border",
    badge: "bg-emerald-50 text-emerald-700 dark:bg-emerald-950/50 dark:text-emerald-400",
    iconColor: "text-emerald-500",
  },
};

export function ContractCard({ contract, driftEvents }: ContractCardProps) {
  const openEvents = driftEvents.filter((e) => e.status === "OPEN");
  const cfg = STATUS_CONFIG[contract.status] ?? STATUS_CONFIG.HEALTHY;
  const Icon = cfg.icon;

  return (
    <div className={cn("bg-card rounded-xl border p-5", cfg.card)}>
      <div className="flex items-start justify-between gap-3 mb-3">
        <div className="min-w-0 flex-1">
          <span className="text-xs text-muted-foreground block mb-1">{contract.owner}</span>
          <h3 className="font-semibold text-sm font-mono leading-snug">{contract.name}</h3>
          <p className="text-xs text-muted-foreground mt-1">
            v{contract.version} · {contract.coverage}% field coverage
          </p>
        </div>
        <span className={cn("inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold flex-shrink-0", cfg.badge)}>
          <Icon className={cn("w-3 h-3", cfg.iconColor)} />
          {cfg.label}
        </span>
      </div>

      {openEvents.length > 0 && (
        <div className="space-y-2 mt-3 pt-3 border-t border-border">
          {openEvents.map((event) => (
            <div key={event.id} className="flex gap-2.5 text-xs">
              <div className="w-1 rounded-full bg-red-400 flex-shrink-0 mt-0.5" />
              <div>
                <span className="font-semibold text-foreground">{event.field ?? "schema"}</span>
                <span className="text-muted-foreground"> — {event.description}</span>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
