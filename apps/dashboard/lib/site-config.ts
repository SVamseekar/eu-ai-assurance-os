export const siteConfig = {
  name: "EU AI Assurance OS",
  shortName: "Assurance OS",
  description:
    "Governance control plane for teams shipping AI systems in the EU market. Validate releases against EU AI Act controls with risk classification, cited evidence, eval gates, data-contract drift monitoring, and audit-ready approvals.",
  url:
    process.env.NEXT_PUBLIC_SITE_URL?.trim() ??
    "https://euassuranceai.souravamseekar.com",
  locale: "en_GB",
  supportEmail: "euassuranceai@souravamseekar.com",
  ownerName: "Marti Soura Vamseekar",
  githubUrl: "https://github.com/souravamseekarmarti/eu-ai-assurance-os",
  portfolioUrl: "https://souravamseekar.com",
  legalLastUpdated: "20 July 2026",
};

/** Authenticated dashboard routes — excluded from sitemap, disallowed in robots.txt */
export const dashboardRoutes = [
  "/command",
  "/systems",
  "/approvals",
  "/evidence",
  "/evals",
  "/contracts",
  "/audit",
  "/readiness",
  "/reg-monitor",
  "/login",
] as const;

/** Public marketing routes included in sitemap */
export const publicRoutes = [
  { path: "/", changeFrequency: "weekly" as const, priority: 1 },
  { path: "/request-demo", changeFrequency: "monthly" as const, priority: 0.9 },
  { path: "/privacy", changeFrequency: "yearly" as const, priority: 0.4 },
  { path: "/terms", changeFrequency: "yearly" as const, priority: 0.4 },
  { path: "/refunds", changeFrequency: "yearly" as const, priority: 0.4 },
  { path: "/disclaimer", changeFrequency: "yearly" as const, priority: 0.4 },
] as const;

export const landingNavLinks = [
  { href: "#capabilities", label: "Product" },
  { href: "#how-it-works", label: "How it works" },
  { href: "#personas", label: "Who it's for" },
  { href: "#faq", label: "FAQ" },
];

export const appRoutes = [
  { href: "/command", label: "Dashboard" },
  { href: "/systems", label: "AI Systems" },
  { href: "/approvals", label: "Approvals" },
  { href: "/evidence", label: "Evidence" },
  { href: "/evals", label: "Eval Gates" },
  { href: "/contracts", label: "Contracts" },
  { href: "/audit", label: "Audit Log" },
] as const;

export function isAnalyticsConfigured(): boolean {
  return Boolean(process.env.NEXT_PUBLIC_GA_MEASUREMENT_ID?.trim());
}
