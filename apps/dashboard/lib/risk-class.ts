import type { RiskClass } from "./types";

export type ApiRiskClass = "MINIMAL" | "LIMITED" | "HIGH" | "PROHIBITED";

export function toApiRiskClass(riskClass: RiskClass): ApiRiskClass {
  return riskClass.toUpperCase() as ApiRiskClass;
}

export interface RiskAnswers {
  q_biometrics: boolean;
  q_essential: boolean;
  q_hr: boolean;
  q_interaction: boolean;
}

/** A suggestion only — the user confirms it, and the API records the basis. Not a legal determination. */
export function suggestRiskClass(answers: RiskAnswers): RiskClass {
  if (answers.q_biometrics || answers.q_essential || answers.q_hr) return "high";
  if (answers.q_interaction) return "limited";
  return "minimal";
}
