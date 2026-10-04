/** Single source of the prices and limits the dashboard shows. Dodo charges them; keep both in sync. */
export type PlanCode = "FREE" | "TEAM" | "BUSINESS" | "ENTERPRISE";

export interface PlanFeature {
  label: string;
  available: boolean;
}

export interface PlanDisplay {
  code: PlanCode;
  name: string;
  monthly: number | null;
  yearly: number | null;
  gatedSystems: number | null;
  editors: number | null;
  gateRunsPerMonth: number | null;
  features: PlanFeature[];
}

const SOON = " (coming soon)";

// Feature availability at launch. Flip `available` when the matching feature ships.
const SIGNED_PDF: PlanFeature = { label: "Signed PDF evidence packs", available: true };
const NIST: PlanFeature = { label: "NIST AI RMF crosswalk" + SOON, available: false };
const ISO: PlanFeature = { label: "ISO/IEC 42001 crosswalk (control titles)" + SOON, available: false };
const QUESTIONNAIRE: PlanFeature = { label: "Questionnaire answer export" + SOON, available: false };
const MLFLOW: PlanFeature = { label: "MLflow integration" + SOON, available: false };
const AU_ADM: PlanFeature = { label: "AU automated-decision disclosure pack" + SOON, available: false };
const SSO: PlanFeature = { label: "Google/Microsoft SSO linking" + SOON, available: false };

export const PLANS: PlanDisplay[] = [
  {
    code: "FREE",
    name: "Free",
    monthly: 0,
    yearly: 0,
    gatedSystems: 1,
    editors: 3,
    gateRunsPerMonth: 300,
    features: [],
  },
  {
    code: "TEAM",
    name: "Team",
    monthly: 79,
    yearly: 790,
    gatedSystems: 3,
    editors: 10,
    gateRunsPerMonth: null,
    features: [SIGNED_PDF, NIST],
  },
  {
    code: "BUSINESS",
    name: "Business",
    monthly: 299,
    yearly: 2990,
    gatedSystems: 15,
    editors: null,
    gateRunsPerMonth: null,
    features: [SIGNED_PDF, NIST, SSO, ISO, QUESTIONNAIRE, MLFLOW, AU_ADM],
  },
  {
    code: "ENTERPRISE",
    name: "Enterprise",
    monthly: null,
    yearly: null,
    gatedSystems: null,
    editors: null,
    gateRunsPerMonth: null,
    features: [SIGNED_PDF, NIST, SSO, ISO, QUESTIONNAIRE, MLFLOW, AU_ADM],
  },
];

export function formatPrice(amount: number | null): string {
  if (amount === null) return "Custom";
  return `$${amount.toLocaleString("en-US")}`;
}

export function formatLimit(limit: number | null): string {
  return limit === null ? "Unlimited" : String(limit);
}
