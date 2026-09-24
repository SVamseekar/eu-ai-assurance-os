import {
  Server,
  ShieldCheck,
  FileSearch,
  FlaskConical,
  GitBranch,
  ClipboardCheck,
  BadgeCheck,
  ScanSearch,
  Library,
  ListChecks,
  type LucideIcon,
} from "lucide-react";

/** Primary-law sources cited on marketing pages. Not legal advice. */
export const actSources = {
  aiAct: {
    label: "Regulation (EU) 2024/1689",
    href: "https://eur-lex.europa.eu/eli/reg/2024/1689/oj",
  },
  omnibus: {
    label: "Regulation (EU) 2026/1744",
    href: "https://eur-lex.europa.eu/eli/reg/2026/1744/oj",
  },
} as const;

export const actTimeline = [
  {
    date: "2 August 2026",
    status: "In force",
    title: "Article 50 transparency",
    body: "Disclose AI interaction when it is not obvious, mark synthetic content, and inform people exposed to certain recognition systems. The high-risk deferral did not move this date.",
  },
  {
    date: "2 December 2027",
    status: "Upcoming",
    title: "Annex III high-risk duties",
    body: "Standalone high-risk systems — including many employment, credit, and essential-services uses — after the Digital Omnibus on AI.",
  },
  {
    date: "2 August 2028",
    status: "Upcoming",
    title: "Annex I embedded high-risk",
    body: "AI embedded in regulated products listed in Annex I. A separate track from standalone Annex III systems.",
  },
] as const;

export const releaseDecisionMeanings = [
  {
    decision: "PASS",
    meaning:
      "Cited evidence, the eval, and contracts clear the bar, and no in-force WARNING, APPROVAL_REQUIRED, or BLOCKING control holds the release. The sealed pack can be exported.",
  },
  {
    decision: "REVIEW",
    meaning:
      "An in-force WARNING on an otherwise clear gate, or an unsigned APPROVAL_REQUIRED control, holds the release for a person. Owner, compliance, or legal still have to act.",
  },
  {
    decision: "BLOCKED",
    meaning:
      "A hard stop: an in-force BLOCKING control, an open contract BREACH, a prohibited class, or missing high-risk oversight. A missing approval timestamp stays inconclusive. The scan does not invent the field.",
  },
] as const;

export const gateInputs = [
  {
    title: "Cited evidence",
    body: "Indexed policy docs, DPIAs, model cards, and vendor files. Coverage below the bar does not pass.",
  },
  {
    title: "Eval scores",
    body: "Latest completed run against the system’s thresholds. Miss the threshold and the gate does not pass.",
  },
  {
    title: "Data contracts",
    body: "Open BREACH drift on an input schema blocks release until it is closed.",
  },
  {
    title: "Promotion files",
    body: "A pinned evgraph-cli 0.1.2 scan checks approval-before-deploy and dataset license. The evidence pack reports that same gap. Missing fields are not invented.",
  },
] as const;

export const productNotThis = [
  {
    title: "Not a notified body",
    body: "We do not assess conformity for placing a high-risk system on the market, and we do not issue a certificate or CE mark.",
  },
  {
    title: "Not legal advice",
    body: "Risk class and obligation maps are assisted records you own. Counsel still signs the legal determination.",
  },
  {
    title: "Not a public price list",
    body: "Work is scoped after a demo, for named systems, with a written quote. There is no self-serve checkout.",
  },
] as const;

export const methodSteps = [
  {
    title: "Read what is already public",
    description:
      "Press notes, product pages, and AI Act stance pages the organisation published. Every example on this site cites a URL.",
  },
  {
    title: "Rebuild only what those pages support",
    description:
      "A model card can often be reconstructed. An approval timestamp or dataset license usually cannot — public pages rarely publish them.",
  },
  {
    title: "Run the same fail-closed checks",
    description:
      "The pinned evgraph-cli 0.1.2 scan used on a customer system. The evidence pack and the live scan report the same current gap. A missing approval timestamp stays inconclusive. We do not fill the field.",
  },
] as const;

export const methodLimits = [
  "We do not invent an approval timestamp or dataset license.",
  "We do not treat a published legal stance as a release record.",
  "We do not call a company non-compliant or illegal.",
  "Named organisations are examples, not customers.",
] as const;

export const typicalSectors = [
  {
    title: "Insurance",
    body: "Claims, advice, and contract-close assistants that already appear in public product copy.",
  },
  {
    title: "Consumer credit",
    body: "Scoring and automation in lending — high-stakes if the use is Annex III-shaped.",
  },
  {
    title: "Recruitment and HR",
    body: "Matching, ranking, and selection tools. Employment-shaped uses need extra care; this is not a legal class.",
  },
  {
    title: "Internal assistants",
    body: "Support RAG and chat that still need Article 50 disclosure even when they are not high-risk.",
  },
] as const;

export const packContents = [
  "Release decision and the controls behind it",
  "Cited evidence snapshot and eval run",
  "Contract status and open drift",
  "Approvals and human-oversight records",
  "Evgraph promotion and dataset artifacts",
  "Annex IV-shaped checklist — not a legal instrument",
] as const;

export const quotingSteps = [
  {
    title: "Demo one named system",
    body: "A short call on the system you actually ship — owner, purpose, data, and where it runs.",
  },
  {
    title: "Written quote",
    body: "Fees, term, and scope in writing. Nothing is billed from a public page.",
  },
  {
    title: "Readiness work, then a pack",
    body: "Register, classify, run the gates, export the sealed pack, and readout with the people who own the release.",
  },
] as const;

export type RelatedLink = {
  href: string;
  title: string;
  description: string;
};

export const relatedByPath: Record<string, RelatedLink[]> = {
  "/product": [
    {
      href: "/how-it-works",
      title: "How it works",
      description: "Four steps from register to PASS, REVIEW, or BLOCKED.",
    },
    {
      href: "/method",
      title: "Method",
      description: "What a fail-closed check sees on public pages.",
    },
    {
      href: "/faq",
      title: "FAQ",
      description: "Dates, risk class, evidence packs, and how to start.",
    },
  ],
  "/how-it-works": [
    {
      href: "/product",
      title: "Product",
      description: "Registry, evidence, evals, contracts, and the sealed pack.",
    },
    {
      href: "/who-its-for",
      title: "Who it's for",
      description: "Engineering, compliance, legal, data, product, and audit.",
    },
    {
      href: "/request-demo",
      title: "Request a demo",
      description: "Walk through a release decision on one named system.",
    },
  ],
  "/who-its-for": [
    {
      href: "/product",
      title: "Product",
      description: "What sits in the control plane.",
    },
    {
      href: "/how-it-works",
      title: "How it works",
      description: "The path from register to sealed pack.",
    },
    {
      href: "/pricing",
      title: "How we work",
      description: "Demo first, then a written quote.",
    },
  ],
  "/method": [
    {
      href: "/faq",
      title: "FAQ",
      description: "Are named firms customers? What is Evgraph?",
    },
    {
      href: "/product",
      title: "Product",
      description: "The same gates, used on your systems.",
    },
    {
      href: "/disclaimer",
      title: "Disclaimer",
      description: "Not a notified body and not a legal finding.",
    },
  ],
  "/faq": [
    {
      href: "/method",
      title: "Method",
      description: "How public-claims examples are built.",
    },
    {
      href: "/how-it-works",
      title: "How it works",
      description: "What PASS, REVIEW, and BLOCKED mean.",
    },
    {
      href: "/pricing",
      title: "How we work",
      description: "Commercial work after a demo — not a price list.",
    },
  ],
  "/pricing": [
    {
      href: "/request-demo",
      title: "Request a demo",
      description: "Start with one named AI system.",
    },
    {
      href: "/product",
      title: "Product",
      description: "What the control plane actually does.",
    },
    {
      href: "/faq",
      title: "FAQ",
      description: "Certification, GRC tools, and how to start.",
    },
  ],
  "/request-demo": [
    {
      href: "/product",
      title: "Product",
      description: "What you will see in the walkthrough.",
    },
    {
      href: "/how-it-works",
      title: "How it works",
      description: "The four steps behind a release decision.",
    },
    {
      href: "/pricing",
      title: "How we work",
      description: "Quote after the call, not on this page.",
    },
  ],
};

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
      "The AI system register: owner, purpose, risk class, deployment context, vendor and model, data sources, and release status — the inventory EU AI Act work actually starts from.",
  },
  {
    icon: ShieldCheck,
    title: "Risk Classification",
    description:
      "Record prohibited, high, limited, or minimal risk against a pinned legal corpus, with rationale, affected users, sector, and decision impact. The tier sets which controls must be on file before release.",
  },
  {
    icon: Library,
    title: "Pinned legal corpus",
    description:
      "Risk is classified against a pinned legal corpus. The pin is the text the classification reads. It is not a live scrape of the statute, and it is not a legal verdict.",
  },
  {
    icon: ListChecks,
    title: "Proposal queue",
    description:
      "Control mappings are proposals. A person accepts them. They are not auto-applied. Until that acceptance, the mapping stays in the queue.",
  },
  {
    icon: FileSearch,
    title: "Evidence RAG",
    description:
      "Cited answers from DPIAs, model cards, vendor docs, incident records, and data contracts. Technical documentation stays attached to the system, not lost in a shared drive.",
  },
  {
    icon: FlaskConical,
    title: "Eval Gates",
    description:
      "Datasets, model and prompt versions, scores, and thresholds feed the gate. The latest completed run must meet the bar or the release does not pass.",
  },
  {
    icon: GitBranch,
    title: "Data Contract Monitor",
    description:
      "Input schemas, lineage, and drift events with severity. An open breach-severity event blocks the release until it is remediated.",
  },
  {
    icon: ClipboardCheck,
    title: "Approval Workflow & Audit Ledger",
    description:
      "Route REVIEW and BLOCKED systems through owner, compliance, legal, and human-oversight sign-off. Every decision is written to a hash-chained audit ledger.",
  },
  {
    icon: BadgeCheck,
    title: "Certification readiness automation",
    description:
      "A 0–100 readiness score and gap report toward Annex IV-shaped documentation. It is a worklist, not a certificate and not a notified-body attestation.",
  },
  {
    icon: ScanSearch,
    title: "Pinned Evgraph scan",
    description:
      "The pack runs evgraph-cli 0.1.2, the same library as the public Evidence Graph. Approval-before-deploy and dataset license are checked on the files you already have. Missing timestamps stay inconclusive. We do not invent them.",
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
      "Add the AI system with owner, purpose, deployment context, and data sources. This is the register the rest of the gate reads from.",
  },
  {
    title: "Classify risk & attach evidence",
    description:
      "Record prohibited, high, limited, or minimal risk against a pinned legal corpus, and why. Attach DPIAs, model cards, vendor docs, and the human-oversight record high-risk systems need.",
  },
  {
    title: "Scan, eval, and check contracts",
    description:
      "A pinned Evgraph 0.1.2 scan, the eval score, and any open data-contract breach feed one decision. Corpus-backed mapping proposals stay in a queue until a person accepts them. Missing files do not pass.",
  },
  {
    title: "Get a release decision",
    description:
      "Take PASS, REVIEW, or BLOCKED with the controls behind it. Export a sealed evidence pack (JSON + PDF hash) whose current gaps match the live Evgraph scan.",
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
      "Ship model and prompt changes behind a release gate. In-force controls use INFORMATIONAL, WARNING, APPROVAL_REQUIRED, or BLOCKING. A failed eval is REVIEW or BLOCKED, not a Slack thread.",
  },
  {
    role: "Compliance Officer",
    description:
      "See which obligations a pinned legal corpus maps onto the system. Those mappings are proposals until a person accepts them. The citations are for an auditor. This is not a legal determination.",
  },
  {
    role: "Legal Counsel",
    description:
      "Documented risk class, oversight records, and a hash-chained trail of who approved what. Counsel still owns the legal opinion.",
  },
  {
    role: "Data Platform Lead",
    description:
      "See schema drift before release. An open BREACH on an upstream contract is a hard stop, not a surprise in production.",
  },
  {
    role: "Product Owner",
    description:
      "A single PASS, REVIEW, or BLOCKED before launch. The sealed evidence pack and a live evgraph-cli 0.1.2 scan report the same current gap.",
  },
  {
    role: "Auditor",
    description:
      "Review who approved what, when, and on which evidence pack. The ledger is append-only.",
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
      "Public pages do not publish an approval timestamp or dataset license, so that gap stays inconclusive. The scan does not invent the field.",
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
      "Public pages do not publish an approval timestamp or dataset license, so that gap stays inconclusive. The scan does not invent the field.",
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
      "No public approval timestamp, so that field stays inconclusive. Ranking applicants is employment-shaped. This is not a legal classification.",
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
      "A published legal stance is not the same as an approval record or eval pack. This example does not adjudicate Article 5.",
  },
];

export const homeDestinations = [
  {
    href: "/product",
    title: "Product",
    description:
      "EU AI Act control plane: system register, risk class, cited evidence, eval gates, contracts, approvals, sealed pack.",
  },
  {
    href: "/how-it-works",
    title: "How it works",
    description:
      "Register the system, classify risk against a pinned corpus, queue mapping proposals for a person to accept, then take PASS, REVIEW, or BLOCKED.",
  },
  {
    href: "/method",
    title: "Method",
    description:
      "What a fail-closed check sees on pages a company already published. Named firms are examples, not customers.",
  },
  {
    href: "/faq",
    title: "FAQ",
    description:
      "Article 50 dates, Annex III high-risk, evidence packs, and how commercial work starts.",
  },
] as const;

export const faqItems: FaqItem[] = [
  {
    question: "What is the EU AI Act?",
    answer:
      "Regulation (EU) 2024/1689 is the EU’s risk-based law for placing and using AI systems on the Union market. Duties depend on risk class and on whether you are a provider or a deployer. This product organises evidence against those duties. It is not legal advice.",
  },
  {
    question: "When do high-risk AI duties apply?",
    answer:
      "Standalone Annex III high-risk obligations apply from 2 December 2027 under Regulation (EU) 2026/1744. AI embedded in Annex I products applies from 2 August 2028. Article 50 transparency has applied since 2 August 2026 and was not deferred.",
  },
  {
    question: "What is Article 50 of the EU AI Act?",
    answer:
      "Article 50 is the transparency layer: disclose AI interaction when it is not obvious, mark synthetic content in a machine-readable way, and inform people exposed to certain emotion-recognition or biometric systems. It can apply even when the system is not high-risk.",
  },
  {
    question: "What is an EU AI Act risk classification?",
    answer:
      "The tier assigned to a system — prohibited, high, limited, or minimal — from its intended use, read against a pinned legal corpus. Annex III lists use cases presumed high-risk. The tier sets which controls and evidence this product requires before release.",
  },
  {
    question: "Does this certify my AI system or replace a notified body?",
    answer:
      "No. EU AI Assurance OS is software for your own release governance. It is not a notified body, not a CE-mark issuer, and not a legal certificate. Counsel still owns the determination of obligations.",
  },
  {
    question: "How is this different from a GRC platform?",
    answer:
      "Most GRC tools inventory policies and collect evidence across many frameworks. This is a fail-closed release gate. In-force controls use INFORMATIONAL, WARNING, APPROVAL_REQUIRED, or BLOCKING. Missing cited evidence, a failed eval, or an open contract BREACH does not pass. A missing approval timestamp stays inconclusive.",
  },
  {
    question: "What is Evgraph?",
    answer:
      "A separate Python library. This product pins evgraph-cli 0.1.2 and runs it on the promotion files and dataset manifest. The pack and the scan must show the same current gap. If an approval timestamp or dataset license is missing, the finding stays inconclusive. We do not invent the field.",
  },
  {
    question: "What is an evidence pack?",
    answer:
      "A sealed, exportable bundle (JSON plus a hashed PDF) of the documents, citations, eval results, contract status, approvals, and Evgraph artifacts behind a release decision — built for audit review, not as a legal Annex IV filing.",
  },
  {
    question: "Are the named EU companies on this site customers?",
    answer:
      "No. Those examples are reconstructed from pages the organisations already published. They are not customers, not legal findings, and not accusations of non-compliance.",
  },
  {
    question: "Who needs to approve a high-risk AI system release?",
    answer:
      "Control mappings are proposals a person accepts. They are not auto-applied. High-risk systems also route through owner, compliance, and legal approval, and require documented human-oversight evidence before the release gate can pass.",
  },
  {
    question: "What counts as a data-contract drift event?",
    answer:
      "A drift event is recorded when an input source no longer matches its agreed schema or semantic contract. An open breach-severity event blocks the release gate until it is closed.",
  },
  {
    question: "How do you start?",
    answer:
      "Request a demo. We scope one named AI system and send a written quote. This is not a self-serve subscription and not a legal certificate.",
  },
];
