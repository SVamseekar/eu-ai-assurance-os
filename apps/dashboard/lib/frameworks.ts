import { PLANS } from "@/lib/pricing";

export type FrameworkStatus = "live" | "coming-soon";

export interface Framework {
  name: string;
  /** Badge label for compact rows. */
  short: string;
  status: FrameworkStatus;
}

function featureLive(prefix: string): boolean {
  return PLANS.some((p) => p.features.some((f) => f.label.startsWith(prefix) && f.available));
}

/** True when a pricing feature with this label prefix is available in at least one plan. */
export function frameworkFlag(prefix: string): boolean {
  return featureLive(prefix);
}

const nistLive = featureLive("NIST AI RMF");

/**
 * Framework coverage shown on the home page and the EU AI Act page. Nothing is "live" unless the matching
 * feature works today: crosswalk status follows the pricing feature flags, packs not yet built are "coming soon".
 */
export const FRAMEWORKS: Framework[] = [
  { name: "EU AI Act (Regulation (EU) 2024/1689)", short: "EU AI Act", status: "live" },
  { name: "Digital Omnibus on AI (Regulation (EU) 2026/1744)", short: "Digital Omnibus", status: "live" },
  { name: "NIST AI RMF crosswalk", short: "NIST AI RMF", status: nistLive ? "live" : "coming-soon" },
  {
    name: "ISO/IEC 42001 crosswalk",
    short: "ISO/IEC 42001",
    status: featureLive("ISO/IEC 42001") ? "live" : "coming-soon",
  },
  { name: "Texas TRAIGA (NIST AI RMF safe harbor)", short: "Texas TRAIGA", status: nistLive ? "live" : "coming-soon" },
  { name: "Colorado SB 26-189 checklist", short: "Colorado SB 26-189", status: "coming-soon" },
  {
    name: "Australian Privacy Act (ADM transparency)",
    short: "AU Privacy Act ADM",
    status: featureLive("AU automated-decision") ? "live" : "coming-soon",
  },
  { name: "Australian Guidance for AI Adoption", short: "AU Guidance for AI Adoption", status: "coming-soon" },
  { name: "NZ Biometric Processing Privacy Code", short: "NZ Biometric Code", status: "coming-soon" },
];
