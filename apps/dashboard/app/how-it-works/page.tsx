import { CheckCircle2 } from "lucide-react";

import { MarketingPageShell } from "@/components/landing/marketing-page-shell";
import { CodeTabs } from "@/components/marketing/code-tabs";
import { SignalFlow } from "@/components/marketing/mockups";
import {
  Band,
  Container,
  DecisionTile,
  IllustrativeNote,
  PageHeader,
  SectionHeading,
} from "@/components/marketing/primitives";
import { CtaBand } from "@/components/marketing/sections";
import { exitCodes, gateSnippets } from "@/lib/gate-snippets";
import { releaseDecisionMeanings } from "@/lib/landing-content";
import { marketingMetadata, webPageJsonLd } from "@/lib/seo";
import { siteConfig } from "@/lib/site-config";

const title = "How it works";
const description =
  "Five signals. One decision. Register the AI system, assess risk, provide evidence, connect evaluations and data contracts, approve, and get PASS, REVIEW or BLOCKED in CI.";
const path = "/how-it-works";

export const metadata = marketingMetadata({
  title,
  description,
  path,
  keywords: ["how AI release gates work", "EU AI Act workflow", "PASS REVIEW BLOCKED"],
});

const steps = [
  { title: "Register", body: "Add your AI system and its intended use." },
  { title: "Assess", body: "Determine risk and applicable obligations against a pinned corpus." },
  { title: "Provide evidence", body: "Upload and link the documents each control needs." },
  { title: "Evaluate", body: "Connect your eval results and thresholds." },
  { title: "Monitor data contracts", body: "Detect schema and distribution drift." },
  { title: "Approve", body: "Complete human sign-off workflows." },
  { title: "Release", body: "Get a clear decision: PASS, REVIEW or BLOCKED." },
];

export default function HowItWorksPage() {
  return (
    <MarketingPageShell
      tone="dark"
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
      <Band tone="dark" grid className="-mt-16 pt-16">
        <Container className="pt-10 pb-20 sm:pt-12 sm:pb-24">
          <div className="grid gap-14 lg:grid-cols-[1fr_1.1fr]">
            <div>
              <PageHeader
                tone="dark"
                eyebrow="How it works"
                title="A clear path from code to an evidence-backed release."
                description="Turn complex regulatory requirements into a practical, evidence-based engineering process. Five signals, one decision."
              />
              <ol className="relative mt-10 space-y-6">
                <span
                  aria-hidden="true"
                  className="absolute top-5 bottom-5 left-[21px] w-px bg-gradient-to-b from-periwinkle/60 to-periwinkle/10"
                />
                {steps.map((step, i) => (
                  <li key={step.title} className="relative flex gap-5">
                    <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-full bg-[#c9d1ff] text-base font-bold text-navy-950 shadow-[0_0_30px_-6px_rgb(123_140_255/0.8)]">
                      {i + 1}
                    </span>
                    <div className="pt-1">
                      <h2 className="text-base font-semibold text-white">{step.title}</h2>
                      <p className="mt-0.5 text-sm text-on-dark-muted">{step.body}</p>
                    </div>
                  </li>
                ))}
              </ol>
            </div>
            <div className="flex flex-col justify-center">
              <SignalFlow />
              <IllustrativeNote tone="dark" />
            </div>
          </div>
          <div className="mt-14">
            <CodeTabs
              tabs={gateSnippets}
              status={
                <span className="inline-flex items-center gap-2 rounded-lg border border-pass/40 bg-[#0b2a1c] px-3 py-1.5 text-xs font-semibold text-[#4ade80]">
                  <CheckCircle2 className="h-4 w-4" aria-hidden="true" />
                  All mandatory controls satisfied
                </span>
              }
            />
          </div>
        </Container>
      </Band>

      <Band tone="light">
        <Container className="py-20 sm:py-24">
          <SectionHeading
            tone="light"
            eyebrow="Decisions"
            title="Three outcomes, each with its reasons."
            description="Every decision lists the controls behind it, so the next step is obvious."
          />
          <ul className="mt-10 grid gap-5 md:grid-cols-3">
            {releaseDecisionMeanings.map((d) => (
              <li key={d.decision}>
                <DecisionTile
                  decision={d.decision}
                  meta={`Exit code ${exitCodes.find((e) => e.decision === d.decision)?.code}`}
                  detail={d.meaning}
                  className="h-full"
                />
              </li>
            ))}
          </ul>
        </Container>
      </Band>

      <CtaBand />
    </MarketingPageShell>
  );
}
