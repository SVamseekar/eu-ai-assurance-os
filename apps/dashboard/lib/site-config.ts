export const siteConfig = {
  name: "EU AI Assurance OS",
  shortName: "Assurance OS",
  description:
    "Governance control plane for teams shipping AI systems in the EU market. Validate releases against EU AI Act controls with risk classification, cited evidence, eval gates, data-contract drift monitoring, and audit-ready approvals.",
  url:
    process.env.NEXT_PUBLIC_SITE_URL?.trim() ??
    "https://euassuranceai.souravamseekar.com",
  locale: "en_GB",
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
  "/login",
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
