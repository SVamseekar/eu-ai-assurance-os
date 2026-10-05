import type { ReactNode } from "react";
import { CheckCircle2 } from "lucide-react";

import { MarketingPageShell } from "@/components/landing/marketing-page-shell";
import {
  Band,
  ButtonLink,
  Container,
  PageHeader,
  SectionHeading,
  type Tone,
} from "@/components/marketing/primitives";
import { CtaBand } from "@/components/marketing/sections";
import { webPageJsonLd } from "@/lib/seo";
import { siteConfig } from "@/lib/site-config";

export type DetailPoint = { title: string; body: string };

/** Layout for /product/* pages (storyboard frames 06–09): header band with a product visual, then detail points. */
export function ProductDetailPage({
  tone,
  path,
  name,
  eyebrow,
  title,
  description,
  visual,
  aside,
  pointsTitle,
  points,
  primary,
}: {
  tone: Tone;
  path: string;
  name: string;
  /** Storyboard 09 has no eyebrow; omit it there. */
  eyebrow?: string;
  title: string;
  description: string;
  visual: ReactNode;
  aside?: ReactNode;
  pointsTitle: string;
  points: DetailPoint[];
  primary: { href: string; label: string };
}) {
  return (
    <MarketingPageShell
      tone={tone}
      jsonLd={webPageJsonLd({
        name: `${name} — ${siteConfig.shortName}`,
        description,
        path,
        crumbs: [
          { name: "Home", path: "/" },
          { name: "Product", path: "/product" },
          { name, path },
        ],
      })}
    >
      <Band tone={tone} muted grid className={tone === "dark" ? "-mt-16 pt-16" : undefined}>
        <Container className="pt-10 pb-20 sm:pt-12 sm:pb-24">
          {/* Storyboard 06–09: heading, then the product visual inside the first screen; actions follow it. */}
          {aside ? (
            // Storyboard 09: heading and code on the left, the legend card on the right spanning both.
            <div className="grid gap-8 lg:grid-cols-[1.8fr_1fr]">
              <div className="min-w-0">
                <PageHeader
                  tone={tone}
                  eyebrow={eyebrow}
                  title={title}
                  description={description}
                  className="max-w-none [&_h1]:mt-0 [&_h1]:sm:text-[2.6rem]"
                />
                <div className="mt-8">{visual}</div>
              </div>
              <div className="flex">{aside}</div>
            </div>
          ) : (
            <>
              <PageHeader tone={tone} eyebrow={eyebrow} title={title} description={description} />
              <div className="mt-10">{visual}</div>
            </>
          )}
          <div className="mt-10 flex flex-wrap gap-3">
            <ButtonLink href={primary.href} variant="primary" arrow>
              {primary.label}
            </ButtonLink>
            <ButtonLink href="/signup" variant={tone === "dark" ? "outlineOnDark" : "outline"}>
              Start free
            </ButtonLink>
          </div>
        </Container>
      </Band>

      <Band tone="light">
        <Container className="py-20 sm:py-24">
          <SectionHeading tone="light" title={pointsTitle} />
          <ul className="mt-10 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {points.map((p) => (
              <li key={p.title} className="rounded-2xl border border-line bg-white p-6">
                <CheckCircle2 className="h-6 w-6 text-pass" aria-hidden="true" />
                <h3 className="mt-4 font-semibold text-ink">{p.title}</h3>
                <p className="mt-2 text-sm text-ink-muted">{p.body}</p>
              </li>
            ))}
          </ul>
        </Container>
      </Band>

      <CtaBand />
    </MarketingPageShell>
  );
}
