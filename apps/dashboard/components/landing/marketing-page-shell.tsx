import type { ReactNode } from "react";
import Link from "next/link";

import { LandingFooter } from "@/components/landing/landing-footer";
import { LandingHeader } from "@/components/landing/landing-header";

type MarketingPageShellProps = {
  children: ReactNode;
  jsonLd?: unknown;
};

export function MarketingPageShell({ children, jsonLd }: MarketingPageShellProps) {
  return (
    <>
      {jsonLd ? (
        <script
          type="application/ld+json"
          // eslint-disable-next-line react/no-danger
          dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }}
        />
      ) : null}
      <a
        href="#main-content"
        className="sr-only focus:not-sr-only focus:absolute focus:left-4 focus:top-4 focus:z-50 focus:rounded-lg focus:bg-background focus:px-3 focus:py-2 focus:text-sm focus:shadow-md focus:outline-none focus:ring-2 focus:ring-ring"
      >
        Skip to content
      </a>
      <LandingHeader />
      <main id="main-content">{children}</main>
      <LandingFooter />
    </>
  );
}

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
    <section className="border-b border-border">
      <div className="mx-auto max-w-6xl px-4 py-12 sm:px-6 sm:py-16">
        {crumbs?.length ? (
          <nav aria-label="Breadcrumb" className="mb-6 text-sm text-muted-foreground">
            <ol className="flex flex-wrap items-center gap-1.5">
              {crumbs.map((crumb, index) => {
                const last = index === crumbs.length - 1;
                return (
                  <li key={crumb.href} className="flex items-center gap-1.5">
                    {index > 0 ? <span aria-hidden>/</span> : null}
                    {last ? (
                      <span className="text-foreground">{crumb.label}</span>
                    ) : (
                      <Link href={crumb.href} className="hover:text-foreground">
                        {crumb.label}
                      </Link>
                    )}
                  </li>
                );
              })}
            </ol>
          </nav>
        ) : null}
        {eyebrow ? (
          <p className="text-xs font-semibold uppercase tracking-wider text-primary">
            {eyebrow}
          </p>
        ) : null}
        <h1 className="mt-2 max-w-3xl font-heading text-3xl font-semibold tracking-tight sm:text-4xl">
          {title}
        </h1>
        <p className="mt-4 max-w-2xl text-lg text-muted-foreground">{description}</p>
      </div>
    </section>
  );
}
