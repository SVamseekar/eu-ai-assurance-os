import {
  Server,
  ShieldCheck,
  FileSearch,
  FlaskConical,
  GitBranch,
  ClipboardCheck,
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
    title: "Run eval gates & check contracts",
    description:
      "Score the model against faithfulness, bias, accuracy, and cost thresholds, and confirm data contracts have no open drift.",
  },
  {
    title: "Get a release decision",
    description:
      "Receive a PASS, REVIEW, or BLOCKED decision with the controls behind it, and export an evidence pack for audit.",
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
  "Tenant data isolation",
  "Encryption in transit & at rest",
  "Append-only audit ledger",
  "Deterministic evidence export",
];

export type FaqItem = {
  question: string;
  answer: string;
};

export const faqItems: FaqItem[] = [
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
