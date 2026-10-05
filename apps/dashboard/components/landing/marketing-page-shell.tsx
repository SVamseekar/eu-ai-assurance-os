import type { ReactNode } from "react";

import { DeadlineBar } from "@/components/marketing/deadline-bar";
import { Band, Container, PageHeader, type Tone } from "@/components/marketing/primitives";
import { SiteFooter } from "@/components/marketing/site-footer";
import { SiteHeader } from "@/components/marketing/site-header";

type MarketingPageShellProps = {
  children: ReactNode;
  jsonLd?: unknown;
  /** Colour of the first band on the page; the header starts transparent over dark tops. */
  tone?: Tone;
};

export function MarketingPageShell({ children, jsonLd, tone = "light" }: MarketingPageShellProps) {
  return (
    <div className="mk-light flex min-h-full flex-col bg-white font-sans text-ink [&_.font-heading]:font-sans">
      {jsonLd ? (
        <script
          type="application/ld+json"
          // eslint-disable-next-line react/no-danger
          dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }}
        />
      ) : null}
      <a
        href="#main-content"
        className="sr-only focus:not-sr-only focus:absolute focus:left-4 focus:top-4 focus:z-[60] focus:rounded-lg focus:bg-white focus:px-3 focus:py-2 focus:text-sm focus:text-ink focus:shadow-md focus:outline-none focus:ring-2 focus:ring-brand"
      >
        Skip to content
      </a>
      <DeadlineBar />
      <SiteHeader tone={tone} />
      <main id="main-content" className="flex-1">
        {children}
      </main>
      <SiteFooter />
    </div>
  );
}

/** Light page header band for content pages (design board item 15). */
export function PageIntro({
  eyebrow,
  title,
  description,
  crumbs,
}: {
  eyebrow?: string;
  title: string;
  description: string;
  crumbs?: { href: string; label: string }[];
}) {
  return (
    <Band tone="light" muted className="border-b border-line">
      <Container className="pt-10 pb-12 sm:pt-12 sm:pb-16">
        <PageHeader tone="light" eyebrow={eyebrow} title={title} description={description} crumbs={crumbs} />
      </Container>
    </Band>
  );
}
