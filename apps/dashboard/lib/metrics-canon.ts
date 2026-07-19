/**
 * Public-facing product facts for the landing metrics strip.
 *
 * Final freeze (customer counts, social proof) is Part 2.
 * Do not invent customer or deployment counts here.
 */
export type MetricChip = {
  label: string;
  value: string;
  /** Optional longer explanation for aria / title */
  detail?: string;
};

export const landingMetricChips: MetricChip[] = [
  {
    label: "Release decisions",
    value: "PASS · REVIEW · BLOCKED",
    detail: "Deterministic release gate outcomes from evidence, evals, and contracts",
  },
  {
    label: "Risk classes",
    value: "MINIMAL · LIMITED · HIGH · PROHIBITED",
    detail: "EU AI Act–oriented risk classification tiers",
  },
  {
    label: "Core domains",
    value: "Registry · Evidence · Evals · Contracts · Approvals",
    detail: "Product surface for AI system governance",
  },
  {
    label: "Evidence stack",
    value: "RAG + citations",
    detail: "Local-hash embeddings in dev; DJL/ONNX all-MiniLM on postgres",
  },
  {
    label: "Sector packs",
    value: "Insurance · HR · Finance",
    detail: "SPI + vertical pack stubs (Part 15)",
  },
  {
    label: "Audit posture",
    value: "Append-only ledger",
    detail: "Hash-chained audit events with retention hooks",
  },
];
