import type { AiSystem } from "./types";

export const PUBLIC_CLAIMS_PREFIX = "public-claims:";

export function publicClaimsSlug(
  system: Pick<AiSystem, "dataSources"> | null | undefined,
): string | null {
  const hit = system?.dataSources?.find((source) => source.startsWith(PUBLIC_CLAIMS_PREFIX));
  return hit ? hit.slice(PUBLIC_CLAIMS_PREFIX.length) : null;
}

export function isPublicClaimsSystem(system: Pick<AiSystem, "dataSources"> | null | undefined): boolean {
  return publicClaimsSlug(system) !== null;
}

export interface PublicClaimsSource {
  title: string;
  url: string;
  retrievedAt: string;
  quote: string;
}

export interface PublicClaimsTeaser {
  slug: string;
  legalName: string;
  hq: string;
  systemName: string;
  purpose: string;
  riskClass: string;
  riskBasis: string;
  sector: string;
  vendorName: string;
  modelName: string;
  registeredSystemId: string | null;
  releaseDecision: string | null;
  sources: PublicClaimsSource[];
  openGaps: string[];
}

export interface PublicClaimsIndex {
  disclaimer: string;
  retrievedAt: string;
  seedingEnabled: boolean;
  library: string;
  howtoPromotion: string;
  howtoDataset: string;
  systems: PublicClaimsTeaser[];
}

export interface PublicClaimsArtifacts {
  slug: string;
  disclaimer: string;
  library: string;
  howtoPromotion: string;
  howtoDataset: string;
  model_card: Record<string, unknown>;
  approval: Record<string, unknown>;
  deployment: Record<string, unknown>;
  dataset_manifest_csv: string;
}
