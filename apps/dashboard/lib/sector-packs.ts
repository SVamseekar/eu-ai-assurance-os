/**
 * Client-side sector pack resolution (mirrors backend SectorPackRegistry keys).
 * Metrics claim: 3 sector packs + SPI — not all industries integrated.
 */

export const SECTOR_PACK_OPTIONS = [
  { value: "insurance", label: "Insurance / Claims", packId: "insurance" },
  { value: "hr", label: "HR / Employment", packId: "hr" },
  { value: "finance", label: "Financial services / KYC", packId: "finance" },
  { value: "healthcare", label: "Healthcare", packId: null },
  { value: "public_sector", label: "Public sector", packId: null },
  { value: "other", label: "Other", packId: null },
] as const;

export type SectorPackId = "insurance" | "hr" | "finance";

const PACK_KEYS: Record<SectorPackId, string[]> = {
  insurance: ["insurance", "claims", "insurer", "underwriting"],
  hr: ["hr", "employment", "human_resources", "recruiting", "recruitment"],
  finance: ["finance", "financial", "financial_services", "kyc", "banking", "fintech"],
};

export function resolveSectorPackId(sector?: string | null): SectorPackId | null {
  if (!sector || !sector.trim()) return null;
  const normalized = sector.trim().toLowerCase().replace(/\s+/g, "_");
  for (const [packId, keys] of Object.entries(PACK_KEYS) as [SectorPackId, string[]][]) {
    if (keys.includes(normalized) || normalized.includes(packId)) {
      return packId;
    }
    for (const key of keys) {
      if (normalized.includes(key)) return packId;
    }
  }
  return null;
}

export function sectorPackLabel(packId: SectorPackId): string {
  switch (packId) {
    case "insurance":
      return "Insurance";
    case "hr":
      return "HR";
    case "finance":
      return "Finance";
  }
}

export const SECTOR_PACKS_METRICS_LABEL = "3 sector packs + SPI";
