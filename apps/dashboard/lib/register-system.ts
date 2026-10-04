import { api } from "./api";
import { toApiRiskClass, type RiskAnswers } from "./risk-class";
import { deriveRegistration } from "./system-registration";
import type { RiskClass } from "./types";

export interface RegisterSystemInput {
  name: string;
  owner: string;
  purpose: string;
  sector: string;
  answers: RiskAnswers;
}

/** Creates the system and records the confirmed classification with its basis. */
export async function registerSystem(input: RegisterSystemInput): Promise<{ id: string; riskClass: RiskClass }> {
  const { riskClass, riskBasis } = deriveRegistration(input.answers, input.sector);
  const apiRisk = toApiRiskClass(riskClass);
  const created = await api.systems.create({
    name: input.name.trim(),
    owner: input.owner.trim(),
    purpose: input.purpose.trim(),
    riskClass: apiRisk,
    riskBasis,
    deploymentRegion: "EU",
    sector: input.sector || undefined,
  });
  await api.systems.classify(created.id, {
    riskClass: apiRisk,
    basis: riskBasis,
    humanOversightRequired: riskClass === "high",
    sector: input.sector || undefined,
  });
  return { id: created.id, riskClass };
}
