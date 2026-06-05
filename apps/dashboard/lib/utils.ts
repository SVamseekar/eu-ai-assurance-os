import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";
import type { RiskClass, DataContractStatus } from "./types";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export function normaliseDecision(raw: string): "Pass" | "Review" | "Blocked" {
  const lower = raw.toLowerCase();
  if (lower === "pass") return "Pass";
  if (lower === "review") return "Review";
  return "Blocked";
}

export function normaliseRisk(raw: string): RiskClass {
  return raw.toLowerCase() as RiskClass;
}

export function normaliseContractStatus(raw: string): DataContractStatus {
  return raw.toUpperCase() as DataContractStatus;
}

export function formatDate(iso: string): string {
  return new Date(iso).toLocaleString("en-GB", {
    day: "2-digit",
    month: "short",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

export function decisionColor(decision: "Pass" | "Review" | "Blocked"): string {
  return decision === "Pass"
    ? "text-emerald-600 dark:text-emerald-400"
    : decision === "Review"
    ? "text-amber-600 dark:text-amber-400"
    : "text-red-600 dark:text-red-400";
}

export function riskColor(risk: RiskClass): string {
  return risk === "high"
    ? "text-red-600 border-red-300 dark:border-red-800"
    : risk === "limited"
    ? "text-amber-600 border-amber-300 dark:border-amber-800"
    : "text-emerald-600 border-emerald-300 dark:border-emerald-800";
}

export function contractStatusColor(status: DataContractStatus): string {
  return status === "HEALTHY"
    ? "text-emerald-600 dark:text-emerald-400"
    : status === "WARNING"
    ? "text-amber-600 dark:text-amber-400"
    : "text-red-600 dark:text-red-400";
}
