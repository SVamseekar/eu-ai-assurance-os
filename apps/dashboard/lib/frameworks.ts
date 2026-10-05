import { PLANS } from "@/lib/pricing";

export type FrameworkStatus = "live" | "coming-soon";

export interface Framework {
  name: string;
  status: FrameworkStatus;
}

function featureLive(prefix: string): boolean {
  return PLANS.some((p) => p.features.some((f) => f.label.startsWith(prefix) && f.available));
}

/** Coverage shown on the EU AI Act page. Crosswalk status follows the pricing feature flags. */
export const FRAMEWORKS: Framework[] = [
  { name: "EU AI Act (Regulation (EU) 2024/1689)", status: "live" },
  { name: "Digital Omnibus on AI (Regulation (EU) 2026/1744)", status: "live" },
  { name: "NIST AI RMF crosswalk", status: featureLive("NIST AI RMF") ? "live" : "coming-soon" },
  { name: "ISO/IEC 42001 crosswalk", status: featureLive("ISO/IEC 42001") ? "live" : "coming-soon" },
  { name: "Colorado SB 26-189 checklist", status: "coming-soon" },
  { name: "Australian Privacy Act (ADM transparency)", status: "coming-soon" },
];
