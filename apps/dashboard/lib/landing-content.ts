import {
  Server,
  ShieldCheck,
  FileSearch,
  FlaskConical,
  GitBranch,
  ClipboardCheck,
  BadgeCheck,
  ScanSearch,
  type LucideIcon,
} from "lucide-react";

export type Capability = {
  icon: LucideIcon;
  title: string;
  description: string;
};

export const capabilities: Capability[] = [
  {
    icon: Server,
    title: "AI System Registry",
    description:
      "Record every AI system with owner, purpose, risk class, deployment context, vendor/model info, data sources, and release status in one place.",
  },
  {
    icon: ShieldCheck,
    title: "Risk Classification",
    description:
      "A guided workflow records risk class, rationale, affected users, sector, decision impact, and the controls each tier requires.",
  },
  {
    icon: FileSearch,
    title: "Evidence RAG",
    description:
      "Get cited answers from policy docs, DPIAs, model cards, vendor docs, incident records, and data contracts — no more digging through shared drives.",
  },
  {
    icon: FlaskConical,
    title: "Eval Gates",
    description:
      "Store eval datasets, model and prompt versions, scores, thresholds, and guard metrics that feed directly into the release decision.",
  },
  {
    icon: GitBranch,
    title: "Data Contract Monitor",
    description:
      "Track input schemas, semantic contracts, lineage, and drift events — with severity and remediation state visible before release.",
  },
  {
    icon: ClipboardCheck,
    title: "Approval Workflow & Audit Ledger",
    description:
      "Route blocked or in-review systems through owner, compliance, legal, and human-oversight approvals, with an append-only audit trail of every decision.",
  },
  {
    icon: BadgeCheck,
    title: "Certification readiness automation",
    description:
      "Weighted readiness score (0–100) and structured gap report toward conformity documentation — never a legal certificate or notified-body attestation.",
  },
  {
    icon: ScanSearch,
    title: "Evgraph promotion & dataset gates",
    description:
      "Export the model-card, approval, and dataset-manifest files the Evgraph library already reads. This repository does not vendor Evgraph. scan-promotion --gate --strict and scan-dataset-manifest --gate fail closed when timestamps or licenses are missing.",
  },
];

export type HowItWorksStep = {
  title: string;
  description: string;
};

export const howItWorksSteps: HowItWorksStep[] = [
  {
    title: "Register the system",
    description:
      "Add the AI system with its owner, purpose, deployment context, and data sources.",
  },
  {
    title: "Classify risk & attach evidence",
    description:
      "Record the risk tier and rationale, then attach DPIAs, model cards, vendor docs, and policy evidence.",
  },
  {
    title: "Run Evgraph, evals, and contracts",
    description:
      "Evgraph (a separate PyPI library) scans promotion timestamps and dataset licenses. Assurance OS scores evals and blocks on open BREACH drift. Missing public timestamps are INCONCLUSIVE, not invented.",
  },
  {
    title: "Get a release decision",
    description:
      "Receive a PASS, REVIEW, or BLOCKED decision with the controls behind it, and export a sealed evidence pack (JSON + PDF) that includes the Evgraph artifacts.",
  },
];

export type Persona = {
  role: string;
  description: string;
};

export const personas: Persona[] = [
  {
    role: "AI Engineering Lead",
    description:
      "Get release gates for model and prompt changes, wired to eval scores and thresholds you control.",
  },
  {
    role: "Compliance Officer",
    description:
      "See evidence that every AI system meets its regulatory obligations, with citations you can hand to an auditor.",
  },
  {
    role: "Legal Counsel",
    description:
      "Get documented risk classifications, oversight records, and audit trails for every release decision.",
  },
  {
    role: "Data Platform Lead",
    description:
      "Monitor data-contract drift and lineage so a broken upstream schema can't silently block or sink a release.",
  },
  {
    role: "Product Owner",
    description:
      "Get a clear PASS, REVIEW, or BLOCKED decision before launch — with the reasons, not just a status.",
  },
  {
    role: "Auditor",
    description:
      "Review an immutable record of who approved what, when, and on what evidence.",
  },
];

export const trustBadges: string[] = [
  "EU AI Act-aligned controls",
  "Fail-closed Evgraph artifacts",
  "Tenant data isolation",
  "Encryption in transit & at rest",
  "Append-only audit ledger",
  "Not a notified body",
];

export type FaqItem = {
  question: string;
  answer: string;
};

export type PublicClaimsMarketing = {
  slug: string;
  legalName: string;
  hq: string;
  hook: string;
  quote: string;
  sourceTitle: string;
  sourceUrl: string;
  evgraphNote: string;
};

/** Public marketing copy only. Named orgs are not customers. */
export const publicClaimsMarketing: PublicClaimsMarketing[] = [
  {
    slug: "getsafe",
    legalName: "Getsafe GmbH",
    hq: "Heidelberg, Germany",
    hook: "Press dated 5 June 2025: AI agents already handle claims, advice, and contract close.",
    quote:
      "Our AI agents already handle a large part of claims settlement, consulting, and contract conclusion.",
    sourceTitle: "Getsafe relies on AI instead of licenses",
    sourceUrl:
      "https://www.hellogetsafe.com/en-de/press-releases/getsafe-relies-on-ai-instead-of-licenses",
    evgraphNote:
      "Public pages do not publish an approval timestamp or dataset license. Promotion --strict is INCONCLUSIVE; dataset-manifest-complete is not met.",
  },
  {
    slug: "auxmoney",
    legalName: "auxmoney GmbH",
    hq: "Düsseldorf, Germany",
    hook: "COO writing, December 2025: machine learning in credit-risk, >90% automation.",
    quote:
      "Das Ergebnis ist ein durchgängig digitaler Prozess mit über 90 Prozent End-to-End-Automatisierungsgrad. Machine Learning ist seit Jahren fester Bestandteil unserer Arbeit.",
    sourceTitle: "Kreditwürdigkeit mit KI im Detail verstehen",
    sourceUrl:
      "https://www.it-finanzmagazin.de/granular-statt-pauschal-kreditwuerdigkeit-im-detail-verstehen-237084/",
    evgraphNote:
      "Public pages do not publish an approval timestamp or dataset license. Same fail-closed result.",
  },
  {
    slug: "softgarden",
    legalName: "softgarden ltd.",
    hq: "Berlin, Germany",
    hook: "Product pages: AI Matching highlights applicants; a recruiter decides.",
    quote:
      "On request, the feature compares the requirements of the job posting with the content of incoming applications … AI then highlights promising candidates.",
    sourceTitle: "AI recruitment solutions — softgarden",
    sourceUrl: "https://softgarden.com/en/ai-recruitment/",
    evgraphNote:
      "No public approval or deploy timestamp. Highlighting applicants is employment-shaped; this is not a legal classification.",
  },
  {
    slug: "retorio",
    legalName: "Retorio GmbH",
    hq: "Munich, Germany",
    hook: "Publishes an AI Act stance and describes HR selection / development.",
    quote:
      "Retorio is used in HR to support companies in the selection and development of employees.",
    sourceTitle: "The AI Act — Retorio",
    sourceUrl: "https://www.retorio.com/en/ai-act",
    evgraphNote:
      "A published legal stance is not a machine-checkable approval or eval pack. This teaser does not adjudicate Article 5.",
  },
];

export const homeDestinations = [
  {
    href: "/product",
    title: "Product",
    description:
      "Registry, risk class, cited evidence, eval gates, contracts, approvals, and a sealed pack — one release decision.",
  },
  {
    href: "/how-it-works",
    title: "How it works",
    description:
      "Register → classify → run Evgraph, evals, and contracts → PASS / REVIEW / BLOCKED.",
  },
  {
    href: "/request-demo",
    title: "Talk to us",
    description:
      "Scoped readiness work for one named system. Quote after a short call — not a self-serve subscription.",
  },
  {
    href: "/method",
    title: "Method",
    description:
      "Public pages in, fail-closed checks out. Named EU firms are teasers reconstructed from cited pages — not customers.",
  },
] as const;

export const faqItems: FaqItem[] = [
  {
    question: "How do you charge?",
    answer:
      "Like other EU AI Act governance vendors, we do not publish a list price. Work is scoped to one named AI system after a short call, then we send a written quote. It is not a notified-body certificate and not a self-serve subscription.",
  },
  {
    question: "Are the named EU companies on this site customers?",
    answer:
      "No. Public-claims teasers reconstruct model cards from pages those organisations already published. They are not customers, not legal findings, and not accusations of non-compliance. Annex III high-risk duties apply from 2 December 2027; Article 50 has applied since 2 August 2026.",
  },
  {
    question: "What is Evgraph, and is it in this repository?",
    answer:
      "Evgraph is a separate BSD-licensed Python library on PyPI. This product does not vendor it. Assurance OS exports the JSON and CSV files the library already reads, and the sprint runs those scans with --gate (and --strict on promotion). Missing timestamps stay INCONCLUSIVE — we do not invent approved_at.",
  },
  {
    question: "What is public in the GitHub repo versus private to the operator?",
    answer:
      "The MIT repository is product source, template DPA/MSA/order form, and sourced public-claims packs. Operator lab files (.local), git worktrees, .env secrets, outreach queues, invoices, and customer tenant data are not in git. Hosted postgres must keep ASSURANCE_PUBLIC_CLAIMS=false.",
  },
  {
    question: "What is an EU AI Act risk classification?",
    answer:
      "It's the tier (e.g. minimal, limited, high) assigned to an AI system based on its sector, decision impact, and affected users. The tier determines which controls and evidence are required before release.",
  },
  {
    question: "What is an evidence pack?",
    answer:
      "An evidence pack is a deterministic, exportable bundle of the documents, citations, eval results, and approvals behind a release decision — built for audit review.",
  },
  {
    question: "How does an eval gate determine release readiness?",
    answer:
      "Each AI system has eval runs scored against thresholds for faithfulness, bias, refusal behavior, accuracy, latency, and cost. The latest completed run must meet its threshold for the release gate to pass.",
  },
  {
    question: "What counts as a data contract drift event?",
    answer:
      "A drift event is recorded when an input data source no longer matches its agreed schema or semantic contract. An open breach-severity drift event blocks the release gate until it's resolved.",
  },
  {
    question: "Who needs to approve a high-risk AI system release?",
    answer:
      "High-risk systems route through owner, compliance, and legal approval, and require documented human-oversight evidence before the release gate can pass.",
  },
  {
    question: "Does this replace a legal determination of EU AI Act obligations?",
    answer:
      "No. EU AI Assurance OS is a control plane that organizes evidence, evals, and approvals against EU AI Act-style obligations — it doesn't provide legal certification or a final legal determination.",
  },
];
