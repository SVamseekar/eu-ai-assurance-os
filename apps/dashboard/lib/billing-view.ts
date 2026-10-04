import type { BillingSummary } from "./types";

const DAY_MS = 24 * 60 * 60 * 1000;

export function daysLeft(iso: string | null, now: number = Date.now()): number | null {
  if (!iso) return null;
  const ms = new Date(iso).getTime() - now;
  return Number.isNaN(ms) ? null : Math.max(0, Math.ceil(ms / DAY_MS));
}

/** The one banner line the shell shows for the current billing state, or null when there is nothing to say. */
export function billingBanner(b: BillingSummary, now: number = Date.now()): string | null {
  if (b.plan === "DEMO") return null;
  if (b.status === "ON_HOLD") {
    const days = daysLeft(b.graceUntil, now);
    return `Your last payment failed. Update your payment method${days === null ? "" : ` within ${days} day${days === 1 ? "" : "s"}`} to keep your plan.`;
  }
  if (b.status === "CANCELLED") {
    const days = daysLeft(b.currentPeriodEnd ?? b.graceUntil, now);
    return `Your plan is cancelled${days === null ? "" : ` and ends in ${days} day${days === 1 ? "" : "s"}`}. You can resubscribe at any time.`;
  }
  if (b.plan === "TRIAL") {
    const days = daysLeft(b.trialEndsAt, now);
    return days === null ? null : `Your free trial ends in ${days} day${days === 1 ? "" : "s"}. Upgrade to keep your systems editable.`;
  }
  return null;
}

/** Percent of a limit used, capped at 100; null when the limit is unlimited. */
export function usagePercent(used: number, limit: number): number | null {
  if (limit < 0) return null;
  if (limit === 0) return 100;
  return Math.min(100, Math.round((used / limit) * 100));
}
