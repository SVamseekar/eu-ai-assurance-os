export const siteConfig = {
  name: "EU AI Assurance OS",
  shortName: "Assurance OS",
  description:
    "Fail-closed release governance for AI systems in the EU market. Register systems, classify risk, cite evidence, run eval and data-contract gates, and export a sealed pack. Evgraph (a separate library) checks promotion and dataset files. Not a notified body and not legal certification.",
  url:
    process.env.NEXT_PUBLIC_SITE_URL?.trim() ??
    "https://euassuranceai.souravamseekar.com",
  locale: "en_GB",
  supportEmail: "euassuranceai@souravamseekar.com",
  ownerName: "Marti Soura Vamseekar",
  githubUrl: "https://github.com/SVamseekar/eu-ai-assurance-os",
  portfolioUrl: "https://souravamseekar.com",
  legalLastUpdated: "15 September 2026",
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
  "/settings",
  "/public-claims",
  "/login",
] as const;

/** Public marketing routes included in sitemap */
export const publicRoutes = [
  { path: "/", changeFrequency: "weekly" as const, priority: 1 },
  { path: "/product", changeFrequency: "weekly" as const, priority: 0.9 },
  { path: "/how-it-works", changeFrequency: "weekly" as const, priority: 0.8 },
  { path: "/who-its-for", changeFrequency: "monthly" as const, priority: 0.7 },
  { path: "/pricing", changeFrequency: "monthly" as const, priority: 0.6 },
  { path: "/method", changeFrequency: "weekly" as const, priority: 0.8 },
  { path: "/faq", changeFrequency: "monthly" as const, priority: 0.7 },
  { path: "/request-demo", changeFrequency: "monthly" as const, priority: 0.9 },
  { path: "/privacy", changeFrequency: "yearly" as const, priority: 0.4 },
  { path: "/terms", changeFrequency: "yearly" as const, priority: 0.4 },
  { path: "/refunds", changeFrequency: "yearly" as const, priority: 0.4 },
  { path: "/disclaimer", changeFrequency: "yearly" as const, priority: 0.4 },
  { path: "/dpa", changeFrequency: "yearly" as const, priority: 0.4 },
  { path: "/msa", changeFrequency: "yearly" as const, priority: 0.4 },
  { path: "/order-form", changeFrequency: "yearly" as const, priority: 0.4 },
] as const;

export const landingNavLinks = [
  { href: "/product", label: "Product" },
  { href: "/how-it-works", label: "How it works" },
  { href: "/method", label: "Method" },
  { href: "/faq", label: "FAQ" },
] as const;

/** Primary app nav — keep in sync with sidebar + middleware protected prefixes */
export const appRoutes = [
  { href: "/command", label: "Dashboard" },
  { href: "/systems", label: "AI Systems" },
  { href: "/readiness", label: "Readiness" },
  { href: "/reg-monitor", label: "Reg Monitor" },
  { href: "/approvals", label: "Approvals" },
  { href: "/evidence", label: "Evidence" },
  { href: "/evals", label: "Eval Gates" },
  { href: "/contracts", label: "Contracts" },
  { href: "/audit", label: "Audit Log" },
  { href: "/settings", label: "Workspace" },
  { href: "/public-claims", label: "Public claims" },
] as const;

export function isAnalyticsConfigured(): boolean {
  return Boolean(process.env.NEXT_PUBLIC_GA_MEASUREMENT_ID?.trim());
}
