import Link from "next/link";
import {
  ClipboardCheck,
  Database,
  FileSearch,
  FileSignature,
  FlaskConical,
  GitPullRequestArrow,
  Layers,
  Lock,
  Network,
  UserCheck,
  type LucideIcon,
} from "lucide-react";

import { EuFlag, GithubIcon } from "@/components/marketing/brand-icons";
import { DemoButton } from "@/components/marketing/demo-button";
import { Band, ButtonLink, Container, PhotoSlot } from "@/components/marketing/primitives";
import { FRAMEWORKS } from "@/lib/frameworks";
import { marketingMedia } from "@/lib/marketing-media";
import { cn } from "@/lib/utils";

/** Night sky with a glowing planet horizon; the supplied Europe-at-night photo replaces it when set. */
export function HeroBackdrop() {
  return (
    <>
    <PhotoSlot
      slot={marketingMedia.heroEurope}
      priority
      sizes="100vw"
      className="pointer-events-none absolute inset-0"
      fallback={
        <div className="mk-night absolute inset-0 overflow-hidden" aria-hidden="true">
          {/* Earth horizon sweeping from lower left to upper right, as in storyboard frame 01. */}
          <div className="absolute top-[38%] -left-[30%] aspect-square w-[150%] rotate-[-14deg] rounded-full bg-[radial-gradient(circle_at_60%_8%,#24379a_0%,#0e1a52_22%,#060d2a_45%,#050a1c_65%)] shadow-[0_-40px_140px_-30px_rgb(84_112_255/0.7)] ring-1 ring-[#7f93ff]/50 lg:top-[30%] lg:-left-[10%] lg:w-[120%]" />
          <div className="absolute top-[22%] right-[6%] h-80 w-80 rounded-full bg-[#2b4bff]/20 blur-3xl" />
        </div>
      }
    />
      {/* Darken the text side and the bottom edge so copy and the proof row stay readable over the photo. */}
      <div
        aria-hidden="true"
        className="pointer-events-none absolute inset-0 bg-navy-950/55 lg:hidden"
      />
      <div
        aria-hidden="true"
        className="pointer-events-none absolute inset-0 bg-[linear-gradient(90deg,rgb(5_10_28/0.92)_0%,rgb(5_10_28/0.6)_45%,rgb(5_10_28/0.1)_75%),linear-gradient(0deg,rgb(5_10_28/0.9)_0%,transparent_30%)]"
      />
    </>
  );
}

export type Feature = { icon: LucideIcon; title: string; detail: string; href?: string };

export const productFeatures: Feature[] = [
  { icon: Layers, title: "AI System Registry", detail: "Track every AI feature", href: "/product#registry" },
  { icon: Network, title: "Risk & Control Mapping", detail: "EU AI Act, proposals you accept", href: "/product#mapping" },
  { icon: FileSearch, title: "Evidence Management", detail: "Search with citations", href: "/product/evidence" },
  { icon: FlaskConical, title: "Evaluations", detail: "Thresholds and gates", href: "/product/evaluations" },
  { icon: Database, title: "Data Contracts", detail: "Drift detection", href: "/product/data-contracts" },
  { icon: ClipboardCheck, title: "Approvals", detail: "Multi-stage sign-off", href: "/product#approvals" },
  { icon: GitPullRequestArrow, title: "Release Gate", detail: "CI/CD integration", href: "/product/release-gate" },
  { icon: FileSignature, title: "Audit & Evidence Packs", detail: "Signed and verifiable", href: "/product#packs" },
];

/** Storyboard frame 02: icon grid under the product screenshot. */
export function FeatureGrid({ features = productFeatures }: { features?: Feature[] }) {
  return (
    <ul className="grid grid-cols-2 gap-x-6 gap-y-10 md:grid-cols-4">
      {features.map((f) => {
        const inner = (
          <>
            <span className="mx-auto flex h-14 w-14 items-center justify-center rounded-full bg-brand-soft text-brand ring-8 ring-brand-soft/40 transition-colors group-hover:bg-brand group-hover:text-white">
              <f.icon className="h-6 w-6" aria-hidden="true" />
            </span>
            <span className="mt-4 block text-[15px] font-semibold text-ink">{f.title}</span>
            <span className="mt-1 block text-sm text-ink-muted">{f.detail}</span>
          </>
        );
        return (
          <li key={f.title} className="text-center">
            {f.href ? (
              <Link href={f.href} className="group block rounded-xl p-2 focus-visible:outline-2 focus-visible:outline-brand">
                {inner}
              </Link>
            ) : (
              <div className="p-2">{inner}</div>
            )}
          </li>
        );
      })}
    </ul>
  );
}

/** Four proof points across the bottom of the hero (storyboard frame 01). */
export function HeroProofRow({ items }: { items: Feature[] }) {
  return (
    <ul className="grid grid-cols-2 gap-6 border-t border-white/10 pt-8 lg:grid-cols-4">
      {items.map((item) => (
        <li key={item.title} className="flex flex-col gap-3">
          <span className="flex h-12 w-12 shrink-0 items-center justify-center rounded-full border border-white/10 bg-white/5 text-periwinkle">
            <item.icon className="h-5 w-5" aria-hidden="true" />
          </span>
          <span>
            <span className="block text-sm font-semibold text-white">{item.title}</span>
            <span className="block text-[13px] text-on-dark-muted">{item.detail}</span>
          </span>
        </li>
      ))}
    </ul>
  );
}

const trustBadges = [
  { icon: <EuFlag className="h-9 w-12 rounded" />, title: "EU hosted", detail: "Data stays in Europe" },
  { icon: <GithubIcon className="h-8 w-8 text-ink" />, title: "Open-source CLI", detail: "evgraph-cli 0.1.3" },
  { icon: <FileSignature className="h-8 w-8 text-brand" aria-hidden="true" />, title: "Signed evidence packs", detail: "RS256 + public JWKS" },
  { icon: <Lock className="h-8 w-8 text-brand" aria-hidden="true" />, title: "No LLM on your data", detail: "By default" },
  { icon: <UserCheck className="h-8 w-8 text-brand" aria-hidden="true" />, title: "Human-accepted mappings", detail: "You stay in control" },
];

/** Design board item 13. */
export function TrustBadges({ className }: { className?: string }) {
  return (
    <ul className={cn("grid grid-cols-2 gap-6 sm:grid-cols-3 lg:grid-cols-5", className)}>
      {trustBadges.map((b) => (
        <li key={b.title} className="flex items-center gap-3">
          <span className="flex h-12 w-12 shrink-0 items-center justify-center">{b.icon}</span>
          <span>
            <span className="block text-sm font-semibold text-ink">{b.title}</span>
            <span className="block text-xs text-ink-muted">{b.detail}</span>
          </span>
        </li>
      ))}
    </ul>
  );
}

/** Framework coverage with live / coming-soon badges (EU AI Act page "Regulatory sources"). */
export function FrameworkList() {
  return (
    <ul className="divide-y divide-line">
      {FRAMEWORKS.map((f) => (
        <li key={f.name} className="flex items-center justify-between gap-4 py-3 text-sm">
          <span className="text-ink/85">{f.name}</span>
          <span
            className={cn(
              "shrink-0 rounded-full px-2.5 py-0.5 text-xs font-semibold",
              f.status === "live" ? "bg-pass-soft text-pass" : "bg-mist text-ink-muted",
            )}
          >
            {f.status === "live" ? "Live" : "Coming soon"}
          </span>
        </li>
      ))}
    </ul>
  );
}

/** Closing call to action for conversion pages: one compact strip, text left and actions right. */
export function CtaBand({
  title = "Before AI ships, prove it is ready.",
  description = "Free plan, no card. Gate your first AI system in CI today, or explore the live demo workspace.",
}: {
  title?: string;
  description?: string;
}) {
  return (
    <Band tone="dark" className="mk-night border-t border-white/10">
      <Container className="flex flex-col gap-6 py-10 lg:flex-row lg:items-center lg:justify-between">
        <div className="max-w-xl">
          <h2 className="text-2xl font-bold tracking-tight text-balance text-white sm:text-3xl">{title}</h2>
          <p className="mt-2 text-on-dark-muted">{description}</p>
        </div>
        <div className="flex flex-wrap items-center gap-3">
          <ButtonLink href="/signup" variant="primary" arrow>
            Start free
          </ButtonLink>
          <DemoButton />
          <Link href="/request-demo" className="px-2 text-sm font-semibold text-periwinkle hover:underline">
            Book an enterprise demo
          </Link>
        </div>
      </Container>
    </Band>
  );
}
