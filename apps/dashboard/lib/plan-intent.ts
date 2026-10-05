/**
 * The paid plan a visitor picked on /pricing before signing up. It survives the email-verification round trip in
 * localStorage so the onboarding wizard can offer that plan's checkout as its last step.
 */
export type PlanIntent = { plan: "TEAM" | "BUSINESS"; interval: "MONTHLY" | "YEARLY" };

const KEY = "aos-plan-intent";
const MAX_AGE_MS = 7 * 24 * 60 * 60 * 1000;

/** Reads `?plan=TEAM|BUSINESS&interval=monthly|yearly`; anything else is no intent. */
export function parsePlanIntent(params: { get(name: string): string | null }): PlanIntent | null {
  const plan = params.get("plan")?.toUpperCase();
  if (plan !== "TEAM" && plan !== "BUSINESS") return null;
  const interval = params.get("interval")?.toUpperCase() === "YEARLY" ? "YEARLY" : "MONTHLY";
  return { plan, interval };
}

export function savePlanIntent(intent: PlanIntent, now = Date.now()): void {
  try {
    window.localStorage.setItem(KEY, JSON.stringify({ ...intent, at: now }));
  } catch {
    // Storage blocked: the wizard simply skips the upgrade offer.
  }
}

export function loadPlanIntent(now = Date.now()): PlanIntent | null {
  try {
    const raw = window.localStorage.getItem(KEY);
    if (!raw) return null;
    const data = JSON.parse(raw) as Partial<PlanIntent> & { at?: number };
    if (typeof data.at !== "number" || now - data.at > MAX_AGE_MS) return null;
    return parsePlanIntent(new URLSearchParams({ plan: data.plan ?? "", interval: data.interval ?? "" }));
  } catch {
    return null;
  }
}

export function clearPlanIntent(): void {
  try {
    window.localStorage.removeItem(KEY);
  } catch {
    // Nothing to clear.
  }
}
