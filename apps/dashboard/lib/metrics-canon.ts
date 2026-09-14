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
  "Product facts — not customer counts, and not certification status";

export const landingMetricChips: MetricChip[] = [
  {
    label: "Release decisions",
    value: "PASS · REVIEW · BLOCKED",
    detail:
      "Deterministic release gate from evidence coverage, eval thresholds, data-contract status, and promotion checks",
  },
  {
    label: "Risk classes",
    value: "MINIMAL · LIMITED · HIGH · PROHIBITED",
    detail: "Guided EU AI Act–oriented risk classification (not ML auto-inference)",
  },
  {
    label: "Evidence pack",
    value: "Sealed JSON + hashed PDF",
    detail:
      "Exportable citations, eval snapshot, contract status, approvals, and Evgraph artifacts",
  },
  {
    label: "Promotion checks",
    value: "Fail-closed Evgraph artifacts",
    detail:
      "Approval-before-deploy and dataset-license checks. Missing fields do not pass.",
  },
  {
    label: "Audit ledger",
    value: "Hash-chained · append-only",
    detail: "Hash-chained append-only audit ledger of every release decision",
  },
  {
    label: "Honest limit",
    value: "Not a notified body",
    detail:
      "Readiness score and sealed pack are work products — not legal certification",
  },
];

/** One-liner for footers / meta — keep in sync with METRICS_CANONICAL.md */
export const productOneLiner =
  "Governance control plane for EU AI Act release governance: guided risk, cited evidence, eval and contract gates, assisted obligations, and audit-ready packs — not legal certification.";
