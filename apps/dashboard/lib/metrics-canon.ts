/**
 * Public-facing product facts for the landing metrics strip.
 *
 * Source of truth: docs/METRICS_CANONICAL.md (measured 2026-07-20, tip c0d5cd4).
 * Do not invent customer counts, ARR, or deployment counts here.
 */

export type MetricChip = {
  label: string;
  value: string;
  /** Optional longer explanation for aria / title */
  detail?: string;
};

/** Measured scale line for optional copy (not a customer metric). */
export const measuredScaleLine =
  "~64 API endpoints · 190 automated tests · Flyway through V16 · Next.js 16 · Spring Boot 3.3";

export const landingMetricChips: MetricChip[] = [
  {
    label: "Release decisions",
    value: "PASS · REVIEW · BLOCKED",
    detail:
      "Deterministic release gate from evidence coverage, eval thresholds, and data-contract status",
  },
  {
    label: "Risk classes",
    value: "MINIMAL · LIMITED · HIGH · PROHIBITED",
    detail: "Guided EU AI Act–oriented risk classification (not ML auto-inference)",
  },
  {
    label: "API surface",
    value: "64 endpoints · 190 tests",
    detail: measuredScaleLine,
  },
  {
    label: "Evidence stack",
    value: "DJL/ONNX · pgvector HNSW",
    detail:
      "Cited-evidence RAG: all-MiniLM-L6-v2 via DJL/ONNX on postgres; local-hash in dev",
  },
  {
    label: "Audit & evals",
    value: "Hash-chained · HMAC callbacks",
    detail:
      "Hash-chained append-only audit ledger; HMAC-SHA-256 signed eval-result callbacks",
  },
  {
    label: "Readiness & packs",
    value: "Score + 3 sector packs",
    detail:
      "Certification readiness (0–100 + gaps) — not legal certification; insurance, HR, finance SPI packs",
  },
];

/** One-liner for footers / meta — keep in sync with METRICS_CANONICAL.md */
export const productOneLiner =
  "Governance control plane for EU AI Act release governance: guided risk, cited evidence, eval and contract gates, assisted obligations, and audit-ready packs — not legal certification.";
