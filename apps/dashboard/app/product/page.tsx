import { CheckCircle2, ClipboardCheck, FileSignature, Layers, Network } from "lucide-react";

import { MarketingPageShell } from "@/components/landing/marketing-page-shell";
import { GithubIcon } from "@/components/marketing/brand-icons";
import { DashboardMock } from "@/components/marketing/mockups";
import {
  Band,
  ButtonLink,
  Container,
  IconTile,
  PageHeader,
  SectionHeading,
} from "@/components/marketing/primitives";
import { CtaBand, FeatureGrid } from "@/components/marketing/sections";
import { marketingMetadata, webPageJsonLd } from "@/lib/seo";
import { siteConfig } from "@/lib/site-config";

const title = "Product";
const description =
  "Register AI systems, map them to EU AI Act controls, collect cited evidence, evaluation results and approvals, and gate every release in CI with a signed evidence pack.";
const path = "/product";

export const metadata = marketingMetadata({
  title,
  description,
  path,
  keywords: ["AI system registry", "AI release gate", "AI evidence pack", "EU AI Act controls"],
});

const pillars = [
  {
    id: "registry",
    icon: Layers,
    title: "AI System Registry",
    body: "The register every later step reads from: owner, purpose, risk class, deployment context, vendor and model, data sources, affected users and release status.",
    points: ["Risk class with rationale", "Deployment context and data sources", "History of every change"],
  },
  {
    id: "mapping",
    icon: Network,
    title: "Risk & Control Mapping",
    body: "Systems are classified against a pinned EU AI Act corpus. Control mappings arrive as proposals; a person accepts them before they count.",
    points: ["Pinned legal corpus with EUR-Lex citations", "Proposal queue, never auto-applied", "Control modes: informational to blocking"],
  },
  {
    id: "approvals",
    icon: ClipboardCheck,
    title: "Approvals",
    body: "REVIEW and BLOCKED systems route through engineering, compliance and legal sign-off. Reviewer identity comes from the session, and every decision lands in a hash-chained ledger.",
    points: ["Multi-stage sign-off", "Append-only audit ledger", "Ledger verification endpoint"],
  },
  {
    id: "packs",
    icon: FileSignature,
    title: "Audit & Evidence Packs",
    body: "Export the evidence behind a release decision as canonical JSON plus PDF, signed with RS256 and verifiable against the public JWKS.",
    points: ["Signed, verifiable export", "Annex IV-shaped checklist", "Same gaps as the open-source evgraph scan"],
  },
];

export default function ProductPage() {
  return (
    <MarketingPageShell
      jsonLd={webPageJsonLd({
        name: `${title} — ${siteConfig.shortName}`,
        description,
        path,
        crumbs: [
          { name: "Home", path: "/" },
          { name: title, path },
        ],
      })}
    >
      <Band tone="light" muted>
        <Container className="pt-10 pb-20 sm:pt-12">
          <PageHeader
            tone="light"
            eyebrow="Product"
            title="An end-to-end assurance platform for AI features."
            description="Turn regulatory requirements into practical, auditable actions. Connect risk, evidence, evaluations, data contracts and approvals into a single release decision."
          >
            <div className="mt-8 flex flex-wrap gap-3">
              <ButtonLink href="/signup" variant="primary" arrow>
                Start free
              </ButtonLink>
              <ButtonLink href={siteConfig.githubUrl} variant="outline" external>
                <GithubIcon className="h-4 w-4" />
                View on GitHub
              </ButtonLink>
            </div>
          </PageHeader>
          <div className="mt-12">
            <DashboardMock />
          </div>
          <div className="mt-14">
            <FeatureGrid />
          </div>
        </Container>
      </Band>

      <Band tone="light">
        <Container className="py-20 sm:py-28">
          <SectionHeading
            tone="light"
            eyebrow="Inside the platform"
            title="Every signal a release decision needs."
            description="Evidence, evaluations, data contracts and release gates have their own pages. These are the pieces that connect them."
          />
          <div className="mt-12 grid gap-6 md:grid-cols-2">
            {pillars.map((p) => (
              <article id={p.id} key={p.id} className="scroll-mt-28 rounded-2xl border border-line bg-white p-6 sm:p-8">
                <IconTile icon={p.icon} className="bg-brand-soft text-brand" />
                <h2 className="mt-5 text-xl font-bold tracking-tight text-ink">{p.title}</h2>
                <p className="mt-2 text-ink-muted">{p.body}</p>
                <ul className="mt-5 space-y-2">
                  {p.points.map((point) => (
                    <li key={point} className="flex items-start gap-2 text-sm text-ink/85">
                      <CheckCircle2 className="mt-0.5 h-4 w-4 shrink-0 text-pass" aria-hidden="true" />
                      {point}
                    </li>
                  ))}
                </ul>
              </article>
            ))}
          </div>
        </Container>
      </Band>

      <CtaBand />
    </MarketingPageShell>
  );
}
