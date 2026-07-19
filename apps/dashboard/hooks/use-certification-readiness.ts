import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { CertificationReadiness } from "@/lib/types";
import { MOCK_CERTIFICATION_READINESS } from "@/lib/mock-data";

export function useCertificationReadiness(systemId: string | null | undefined) {
  return useQuery<CertificationReadiness>({
    queryKey: ["certification-readiness", systemId],
    queryFn: () => api.systems.certificationReadiness(systemId!),
    enabled: Boolean(systemId),
    placeholderData: systemId
      ? MOCK_CERTIFICATION_READINESS[systemId] ?? mockFallback(systemId)
      : undefined,
  });
}

function mockFallback(systemId: string): CertificationReadiness {
  return {
    systemId,
    systemName: "Demo system",
    score: 48,
    readinessStatus: "NOT_READY",
    productLabel: "Certification readiness automation",
    disclaimer:
      "Certification readiness automation produces a weighted readiness score (0–100) and a structured gap list. It does not issue certificates and is not legal advice.",
    generatedAt: new Date().toISOString(),
    releaseDecision: "blocked",
    dimensions: [],
    gaps: [
      {
        code: "DEMO_GAP",
        severity: "HIGH",
        message: "Connect the API for live certification readiness scoring",
        remediationHint: "Start the Spring Boot API and open a registered system.",
        dimension: "evidence",
      },
    ],
  };
}
