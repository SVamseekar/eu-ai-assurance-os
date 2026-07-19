import Link from "next/link";

import {
  isAnalyticsConfigured,
  landingNavLinks,
  siteConfig,
} from "@/lib/site-config";

export function LandingFooter() {
  const year = new Date().getFullYear();

  return (
    <footer className="border-t border-border bg-muted/40">
      <div className="mx-auto max-w-6xl px-4 py-12 sm:px-6">
        <p className="mb-10 flex items-start gap-2 text-xs text-muted-foreground sm:items-center">
          <span
            className="mt-1 h-2 w-2 shrink-0 rounded-full bg-amber-500 sm:mt-0"
            aria-hidden
          />
          <span>
            Governance control plane — not legal certification, not a notified
            body, not a final determination of EU AI Act obligations. See{" "}
            <Link
              href="/disclaimer"
              className="text-foreground underline-offset-2 hover:underline"
            >
              Disclaimer
            </Link>
            .
          </span>
        </p>

        <div className="mb-10 max-w-md">
          <p className="font-heading text-sm font-semibold">{siteConfig.name}</p>
          <p className="mt-2 text-sm text-muted-foreground">{siteConfig.description}</p>
        </div>

        <div className="grid grid-cols-2 gap-8 md:grid-cols-4">
          <nav aria-label="Product">
            <h2 className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
              Product
            </h2>
            <ul className="mt-3 space-y-2 text-sm">
              {landingNavLinks.map((link) => (
                <li key={link.href}>
                  <a
                    href={link.href.startsWith("#") ? `/${link.href}` : link.href}
                    className="text-muted-foreground hover:text-foreground"
                  >
                    {link.label}
                  </a>
                </li>
              ))}
              <li>
                <Link
                  href="/request-demo"
                  className="text-muted-foreground hover:text-foreground"
                >
                  Request demo
                </Link>
              </li>
              <li>
                <Link href="/login" className="text-muted-foreground hover:text-foreground">
                  Sign in
                </Link>
              </li>
            </ul>
          </nav>

          <nav aria-label="Resources">
            <h2 className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
              Resources
            </h2>
            <ul className="mt-3 space-y-2 text-sm">
              <li>
                <a href="/#faq" className="text-muted-foreground hover:text-foreground">
                  FAQ
                </a>
              </li>
              <li>
                <a
                  href={siteConfig.githubUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-muted-foreground hover:text-foreground"
                >
                  GitHub
                </a>
              </li>
              <li>
                <Link
                  href="/disclaimer"
                  className="text-muted-foreground hover:text-foreground"
                >
                  Product limitations
                </Link>
              </li>
            </ul>
          </nav>

          <nav aria-label="Legal">
            <h2 className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
              Legal
            </h2>
            <ul className="mt-3 space-y-2 text-sm">
              <li>
                <Link href="/privacy" className="text-muted-foreground hover:text-foreground">
                  Privacy
                </Link>
              </li>
              <li>
                <Link href="/terms" className="text-muted-foreground hover:text-foreground">
                  Terms
                </Link>
              </li>
              <li>
                <Link href="/refunds" className="text-muted-foreground hover:text-foreground">
                  Refunds
                </Link>
              </li>
              <li>
                <Link
                  href="/disclaimer"
                  className="text-muted-foreground hover:text-foreground"
                >
                  Disclaimer
                </Link>
              </li>
            </ul>
          </nav>

          <nav aria-label="Contact">
            <h2 className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
              Contact
            </h2>
            <ul className="mt-3 space-y-2 text-sm">
              <li>
                <a
                  href={`mailto:${siteConfig.supportEmail}`}
                  className="break-all text-muted-foreground hover:text-foreground"
                >
                  {siteConfig.supportEmail}
                </a>
              </li>
              <li>
                <Link
                  href="/request-demo"
                  className="text-muted-foreground hover:text-foreground"
                >
                  Request demo
                </Link>
              </li>
              <li>
                <a
                  href={siteConfig.portfolioUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-muted-foreground hover:text-foreground"
                >
                  Portfolio
                </a>
              </li>
            </ul>
          </nav>
        </div>
      </div>

      <div className="border-t border-border px-4 py-4 text-xs text-muted-foreground sm:px-6">
        <div className="mx-auto flex max-w-6xl flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
          <p>
            © {year} {siteConfig.name} · {siteConfig.ownerName}
          </p>
          {isAnalyticsConfigured() ? (
            <p>
              We use privacy-respecting analytics when configured.{" "}
              <Link href="/privacy" className="hover:text-foreground hover:underline">
                Privacy
              </Link>
            </p>
          ) : null}
        </div>
      </div>
    </footer>
  );
}
