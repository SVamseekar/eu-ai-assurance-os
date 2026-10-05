import { DEADLINES, type Deadline } from "@/lib/deadlines";

/** Shapes returned by the public determination endpoints (see PublicDeterminationController). */
export type CheckQuestion = {
  id: string;
  label: string;
  help: string;
  type: "select" | "boolean" | "boolean_unknown";
  required: boolean;
  options: { value: string; label: string }[];
};

export type Questionnaire = { rulesetVersion: string; disclaimer: string; questions: CheckQuestion[] };

export type CheckObligation = {
  ruleCode: string;
  title: string;
  applicability: "APPLICABLE" | "UNCERTAIN" | "NOT_APPLICABLE";
  basis: string;
  legalRefs: string | null;
  severity: string | null;
};

export type CheckResult = {
  obligations: CheckObligation[];
  riskSuggestion: { suggestedRiskClass: "PROHIBITED" | "HIGH" | "LIMITED" | "MINIMAL"; rationale: string };
  rulesetVersion: string;
  disclaimer: string;
};

/** Form values are strings; the rule engine expects booleans for yes/no answers. Blank answers are left out. */
export function toAnswers(form: Record<string, string>): Record<string, string | boolean> {
  const out: Record<string, string | boolean> = {};
  for (const [key, value] of Object.entries(form)) {
    if (value === "") continue;
    out[key] = value === "true" ? true : value === "false" ? false : value;
  }
  return out;
}

/** Rules that carry Article 50 duties (GPAI transparency is a different regime and has its own dates). */
const TRANSPARENCY = /^(ART50_|TRANSPARENCY_NATURAL_PERSONS)/;

/** The dated duties most relevant to a result, from lib/deadlines.ts. */
export function relevantDeadlines(result: CheckResult, list: Deadline[] = DEADLINES): Deadline[] {
  const ids = new Set<string>();
  const cls = result.riskSuggestion.suggestedRiskClass;
  if (cls === "HIGH") ids.add("eu-annex-iii");
  const transparency = result.obligations.some(
    (o) => o.applicability !== "NOT_APPLICABLE" && TRANSPARENCY.test(o.ruleCode),
  );
  if (cls === "LIMITED" || transparency) {
    ids.add("eu-art50");
    ids.add("eu-art50-2");
  }
  return list.filter((d) => ids.has(d.id));
}

export const riskClassCopy: Record<CheckResult["riskSuggestion"]["suggestedRiskClass"], { label: string; summary: string }> = {
  PROHIBITED: {
    label: "Possibly prohibited",
    summary: "One or more answers match an Article 5 prohibited-practice screen. Get legal review before going further.",
  },
  HIGH: {
    label: "Likely high-risk",
    summary: "Answers point to an Annex III high-risk use. Documentation, oversight, testing and record-keeping duties apply.",
  },
  LIMITED: {
    label: "Transparency duties",
    summary: "Answers point to Article 50 transparency duties, such as telling people they are talking to an AI system.",
  },
  MINIMAL: {
    label: "Minimal risk",
    summary: "No specific AI Act duties found from these answers. Article 4 AI literacy still applies to providers and deployers.",
  },
};
