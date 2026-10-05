export type UseCase = {
  id: string;
  tab: string;
  tagline: string;
  heading: string;
  points: string[];
  /** Checklist shown on the visual beside the copy. */
  panelTitle: string;
  panelItems: string[];
  primary: { href: string; label: string };
  secondary: { href: string; label: string };
};

/** Solutions by team (storyboard frame 05). Every point describes shipped behaviour. */
export const useCases: UseCase[] = [
  {
    id: "engineering",
    tab: "Engineering",
    tagline: "Build with confidence",
    heading: "For engineering teams",
    points: [
      "Fail-closed release gate in CI/CD",
      "One HTTP call from GitHub Actions or any pipeline",
      "Blocks on missing evidence, low eval scores or open contract breaches",
      "Works with your existing eval harness",
    ],
    panelTitle: "GitHub Actions",
    panelItems: ["Fail-closed release gate", "Risk classification", "Evidence checks", "Evaluation thresholds", "Data contract health", "Human approvals"],
    primary: { href: "/product/release-gate", label: "See the release gate" },
    secondary: { href: "/how-it-works", label: "How it works" },
  },
  {
    id: "compliance",
    tab: "Compliance",
    tagline: "Turn obligations into actions",
    heading: "For compliance teams",
    points: [
      "See which EU AI Act obligations map onto each system",
      "Mappings stay proposals until a person accepts them",
      "Citations to a pinned legal corpus, ready for an auditor",
      "Gaps listed per system, not buried in a spreadsheet",
    ],
    panelTitle: "Obligation map",
    panelItems: ["Risk class with rationale", "Article 50 transparency", "Human oversight (Art. 14)", "Record keeping (Art. 12)", "Accepted mappings only", "Evidence per control"],
    primary: { href: "/eu-ai-act", label: "Explore EU AI Act coverage" },
    secondary: { href: "/method", label: "Read the method" },
  },
  {
    id: "legal",
    tab: "Legal",
    tagline: "Maintain accountability",
    heading: "For legal counsel",
    points: [
      "Documented risk class and oversight records per system",
      "Hash-chained trail of who approved what, and when",
      "Signed evidence packs you can verify independently",
      "Counsel keeps the legal determination",
    ],
    panelTitle: "Approval trail",
    panelItems: ["Engineering lead sign-off", "Compliance sign-off", "Legal sign-off", "Reviewer identity from the session", "Append-only ledger", "RS256-signed pack"],
    primary: { href: "/product#approvals", label: "See approvals" },
    secondary: { href: "/disclaimer", label: "Read the disclaimer" },
  },
  {
    id: "data",
    tab: "Data",
    tagline: "Ensure data quality and contracts",
    heading: "For data platform teams",
    points: [
      "Schema contracts for the inputs each system depends on",
      "Drift events recorded with severity",
      "An open breach stops the release, not production",
      "Lineage from source to system",
    ],
    panelTitle: "Contract health",
    panelItems: ["Schema contract", "Semantic contract", "Drift severity", "Breach blocks release", "Lineage graph", "Remediation history"],
    primary: { href: "/product/data-contracts", label: "See data contracts" },
    secondary: { href: "/how-it-works", label: "How it works" },
  },
  {
    id: "product",
    tab: "Product owners",
    tagline: "Ship responsible AI features",
    heading: "For product owners",
    points: [
      "One PASS, REVIEW or BLOCKED before launch",
      "Know exactly which control holds a release",
      "Evidence packs for buyers' security reviews",
      "A free plan to start with one system",
    ],
    panelTitle: "Launch readiness",
    panelItems: ["Release decision", "Blocking controls", "Owner per gap", "Evidence pack export", "Readiness score", "Deadline view"],
    primary: { href: "/pricing", label: "See pricing" },
    secondary: { href: "/product", label: "Product overview" },
  },
  {
    id: "audit",
    tab: "Audit",
    tagline: "Get audit-ready",
    heading: "For auditors and reviewers",
    points: [
      "Review who approved what, when, and on which evidence",
      "Ledger verification endpoint for the hash chain",
      "Evidence packs verifiable against the public JWKS",
      "Read-only auditor role",
    ],
    panelTitle: "Audit view",
    panelItems: ["Hash-chained ledger", "Chain verification", "Signed evidence pack", "Public JWKS", "Read-only access", "Export on request"],
    primary: { href: "/product", label: "Product overview" },
    secondary: { href: "/faq", label: "Read the FAQ" },
  },
];
