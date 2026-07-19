export const COMPANY_SIZES = [
  "1–49",
  "50–249",
  "250–999",
  "1,000–4,999",
  "5,000+",
] as const;

export const INDUSTRIES = [
  "Insurance",
  "Financial services",
  "Healthcare",
  "HR / workforce tech",
  "Public sector",
  "Retail / e-commerce",
  "Manufacturing",
  "Technology / SaaS",
  "Professional services",
  "Other",
] as const;

export const EU_COUNTRIES = [
  "Austria",
  "Belgium",
  "Bulgaria",
  "Croatia",
  "Cyprus",
  "Czechia",
  "Denmark",
  "Estonia",
  "Finland",
  "France",
  "Germany",
  "Greece",
  "Hungary",
  "Ireland",
  "Italy",
  "Latvia",
  "Lithuania",
  "Luxembourg",
  "Malta",
  "Netherlands",
  "Poland",
  "Portugal",
  "Romania",
  "Slovakia",
  "Slovenia",
  "Spain",
  "Sweden",
  "United Kingdom",
  "Switzerland",
  "Norway",
  "Other / multi-country",
] as const;

export const AI_SYSTEMS_COUNTS = [
  "Planning first system",
  "1–3 in production or pilot",
  "4–10",
  "11–25",
  "26+",
] as const;

export const HIGH_RISK_OPTIONS = [
  "Yes — one or more high-risk use cases",
  "Possibly — under review",
  "No — limited / minimal risk only",
  "Not sure yet",
] as const;

export const CURRENT_TOOLING = [
  "Spreadsheets / shared drives",
  "Generic GRC / policy tools",
  "MLOps / model registry only",
  "Custom internal tooling",
  "None yet",
] as const;

export const PRIMARY_INTERESTS = [
  "AI system registry & risk classification",
  "Evidence RAG with citations",
  "Eval gates & release decisions",
  "Data contracts & drift",
  "Approval workflows & audit ledger",
  "Certification readiness (score + gaps)",
  "Regulatory change monitoring",
  "Sector packs (insurance / HR / finance)",
] as const;

export const TIMELINES = [
  "Exploring (3+ months)",
  "Evaluating this quarter",
  "Need a pilot in 30–60 days",
  "Urgent (regulatory deadline)",
] as const;

export const REFERRAL_SOURCES = [
  "Search",
  "GitHub",
  "Portfolio / personal site",
  "LinkedIn",
  "Conference / talk",
  "Colleague / referral",
  "Other",
] as const;
