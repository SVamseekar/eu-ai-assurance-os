import Link from "next/link";
import { ArrowRight, ClipboardCheck, FileSignature, GitPullRequestArrow, Landmark } from "lucide-react";

import { MarketingPageShell } from "@/components/landing/marketing-page-shell";
import { CodeTabs } from "@/components/marketing/code-tabs";
import { DemoButton } from "@/components/marketing/demo-button";
import { DashboardMock, ReadinessPanel } from "@/components/marketing/mockups";
import { PricingTeaser } from "@/components/marketing/pricing-teaser";
import {
  Band,
  ButtonLink,
  Container,
  Eyebrow,
  SectionHeading,
  TextLink,
} from "@/components/marketing/primitives";
import { CtaBand, HeroBackdrop, HeroProofRow } from "@/components/marketing/sections";
import { gateSnippets } from "@/lib/gate-snippets";
import { PLANS } from "@/lib/pricing";
import { marketingMetadata } from "@/lib/seo";
import { siteConfig } from "@/lib/site-config";
import { useCases } from "@/lib/use-cases";

const description =
  "Assurance OS reads the evidence your pipeline already produces — model cards, eval runs, data contracts, approvals — fails the release in CI when something is missing, and exports a signed evidence pack for your buyers and auditors.";

export const metadata = marketingMetadata({
  title: "Release gates and signed evidence packs for AI",
  description,
  path: "/",
  absoluteTitle: `${siteConfig.shortName} — release gates and signed evidence packs for AI`,
  keywords: [
    "EU AI Act software",
    "AI release gate",
    "AI evidence pack",
    "AI system registry",
    "Article 50",
    "Annex III high-risk",
  ],
});

function jsonLd() {
  return {
    "@context": "https://schema.org",
    "@graph": [
      {
        "@type": "Organization",
        name: siteConfig.shortName,
        url: siteConfig.url,
        email: siteConfig.supportEmail,
      },
      {
        "@type": "SoftwareApplication",
        name: siteConfig.shortName,
        applicationCategory: "BusinessApplication",
        operatingSystem: "Web",
        description,
        url: siteConfig.url,
        offers: PLANS.filter((p) => p.monthly !== null).map((p) => ({
          "@type": "Offer",
          name: p.name,
          price: p.monthly,
          priceCurrency: "USD",
        })),
      },
    ],
  };
}

const heroProof = [
  { icon: Landmark, title: "Mapped to the EU AI Act", detail: "Article 50 and beyond" },
  { icon: GitPullRequestArrow, title: "Fail-closed release gate", detail: "CI/CD enforcement" },
  { icon: FileSignature, title: "Cryptographic audit trail", detail: "Signed evidence packs" },
  { icon: ClipboardCheck, title: "Open-source foundation", detail: "evgraph CLI, transparent checks" },
];

const steps = [
  {
    title: "Register the system",
    body: "Add the AI feature with its owner, purpose, data sources and risk class. Mappings to EU AI Act controls stay proposals until a person accepts them.",
  },
  {
    title: "Gate the release in CI",
    body: "One call from your pipeline reads evidence, eval scores, data-contract health and approvals, and returns PASS, REVIEW or BLOCKED.",
  },
  {
    title: "Export the signed pack",
    body: "Hand buyers and auditors an RS256-signed evidence pack they can verify against the public JWKS.",
  },
];

export default function LandingPage() {
  return (
    <MarketingPageShell tone="dark" jsonLd={jsonLd()}>
      {/* Storyboard 01 — hero: fits one screen, proof row pinned to its bottom edge */}
      <Band tone="dark" className="-mt-16 pt-16">
        <HeroBackdrop />
        <Container className="relative flex flex-col pt-10 pb-8 sm:pt-12 lg:min-h-[calc(100svh-6.5rem)] lg:pb-10">
          <div className="grid flex-1 items-center gap-10 lg:grid-cols-[1.5fr_1fr]">
            <div>
              <p className="inline-flex items-center gap-2 rounded-full border border-white/10 bg-white/5 px-3 py-1 text-xs font-medium text-[#c9d1ff]">
                Open source <span aria-hidden="true">·</span> Release gates for AI features
              </p>
              <h1 className="mt-5 max-w-2xl text-4xl leading-[1.05] font-bold tracking-tight text-balance text-white sm:text-5xl lg:text-6xl">
                Ship AI features with evidence, <span className="text-periwinkle">not promises.</span>
              </h1>
              <p className="mt-5 max-w-xl text-base leading-relaxed text-white/80 sm:text-lg">
                A release gate and evidence record for AI features. Assurance OS checks model cards, eval runs, data
                contracts and approvals, fails the release in CI when something is missing, and exports a signed
                evidence pack.
              </p>
              <div className="mt-8 flex flex-wrap items-start gap-3">
                <ButtonLink href="/signup" variant="primary" arrow>
                  Start free
                </ButtonLink>
                <DemoButton />
              </div>
              <p className="mt-4 text-sm text-on-dark-muted">Free plan · no card · evidence and readiness, not legal advice</p>
            </div>
            <div className="flex flex-col items-center lg:items-end">
              <ReadinessPanel />
            </div>
          </div>
          <div className="mt-12">
            <HeroProofRow items={heroProof} />
          </div>
        </Container>
      </Band>

      {/* Storyboard 02 — product overview */}
      <Band tone="light" muted>
        <Container className="py-20 sm:py-28">
          <div className="flex flex-col gap-6 lg:flex-row lg:items-end lg:justify-between">
            <SectionHeading
              tone="light"
              eyebrow="Product"
              title="Everything a release decision needs, in one place."
              description="Turn regulatory requirements into practical, auditable actions. Connect risk, evidence, evaluations, data contracts and approvals into a single release decision."
            />
            <div className="flex shrink-0 flex-wrap gap-3">
              <ButtonLink href="/product" variant="primary" arrow>
                Explore product
              </ButtonLink>
              <ButtonLink href={siteConfig.githubUrl} variant="outline" external>
                View on GitHub
              </ButtonLink>
            </div>
          </div>
          <div className="mt-12">
            <DashboardMock />
          </div>
        </Container>
      </Band>

      {/* Storyboard 03 — how it works (summary; full flow on /how-it-works) */}
      <Band tone="dark" grid>
        <Container className="py-20 sm:py-28">
          <div className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
            <SectionHeading
              tone="dark"
              eyebrow="How it works"
              title="Three steps from commit to signed evidence."
              description="Five signals. One decision. Built for engineering, compliance and legal teams."
            />
            <TextLink href="/how-it-works" tone="dark">
              See the full flow
            </TextLink>
          </div>
          <ol className="mt-12 grid gap-5 md:grid-cols-3">
            {steps.map((step, i) => (
              <li key={step.title} className="rounded-2xl border border-white/10 bg-white/[0.03] p-6">
                <span className="flex h-11 w-11 items-center justify-center rounded-full bg-periwinkle text-base font-bold text-navy-950 shadow-[0_0_30px_-6px_rgb(123_140_255/0.8)]">
                  {i + 1}
                </span>
                <h3 className="mt-5 text-lg font-semibold text-white">{step.title}</h3>
                <p className="mt-2 text-on-dark-muted">{step.body}</p>
              </li>
            ))}
          </ol>
        </Container>
      </Band>

      {/* Storyboard 09 — developer section */}
      <Band tone="dark" className="border-t border-white/5 bg-navy-900">
        <Container className="py-20 sm:py-28">
          <SectionHeading
            tone="dark"
            eyebrow="For developers"
            title="One call from your pipeline."
            description="Simple API. Clear exit codes. Works with GitHub Actions, GitLab CI and any CI/CD system."
          />
          <div className="mt-10">
            <CodeTabs tabs={gateSnippets} />
          </div>
          <div className="mt-6 flex flex-col gap-4 rounded-2xl border border-white/10 bg-white/[0.03] p-5 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <p className="text-sm font-semibold text-white">Check evidence files locally with the open-source CLI</p>
              <pre className="mt-2 overflow-x-auto font-mono text-[13px] text-[#c9d4ff]">
                <code>{`pip install evgraph-cli\nevgraph scan model_card.json approval.json deployment.json --gate --strict`}</code>
              </pre>
            </div>
            <div className="flex shrink-0 flex-wrap gap-3">
              <ButtonLink href="/product/release-gate" variant="outlineOnDark">
                Exit codes and details
              </ButtonLink>
              <ButtonLink href="/signup" variant="primary" arrow>
                Get an API key
              </ButtonLink>
            </div>
          </div>
        </Container>
      </Band>

      {/* Storyboard 05 — use cases */}
      <Band tone="light">
        <Container className="py-20 sm:py-28">
          <SectionHeading
            tone="light"
            eyebrow="Use cases"
            title="One source of truth for every team."
            description="Different teams, one shared source of truth."
          />
          <ul className="mt-10 grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
            {useCases.map((c) => (
              <li key={c.id}>
                <Link
                  href={`/who-its-for#${c.id}`}
                  className="group flex h-full flex-col rounded-2xl border border-line bg-white p-6 transition-shadow hover:border-brand/40 hover:shadow-lg hover:shadow-navy-950/5"
                >
                  <span className="text-xs font-bold uppercase tracking-[0.08em] text-brand">{c.tab}</span>
                  <span className="mt-2 text-lg font-semibold text-ink">{c.tagline}</span>
                  <span className="mt-2 flex-1 text-sm text-ink-muted">{c.points[0]}.</span>
                  <span className="mt-4 inline-flex items-center gap-1 text-sm font-semibold text-brand">
                    See the use case
                    <ArrowRight className="h-4 w-4 transition-transform group-hover:translate-x-0.5" aria-hidden="true" />
                  </span>
                </Link>
              </li>
            ))}
          </ul>
        </Container>
      </Band>

      {/* Storyboard 10 — pricing teaser */}
      <Band tone="light" muted>
        <Container className="py-20 sm:py-28">
          <div className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
            <SectionHeading
              tone="light"
              eyebrow="Pricing"
              title="Start free. Upgrade when you gate more systems."
              description="Start free, scale as you grow. No hidden fees."
            />
            <TextLink href="/pricing" tone="light">
              Compare plans
            </TextLink>
          </div>
          <div className="mt-10">
            <PricingTeaser />
          </div>
          <p className="mt-6 text-sm text-ink-muted">
            Enterprise plans with an order form and onboarding assistance are{" "}
            <Link href="/request-demo" className="font-semibold text-brand hover:underline">
              available on request
            </Link>
            .
          </p>
        </Container>
      </Band>

      <CtaBand />
    </MarketingPageShell>
  );
}
