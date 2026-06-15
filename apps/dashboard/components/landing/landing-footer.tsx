import Link from "next/link";

import { appRoutes, landingNavLinks, siteConfig } from "@/lib/site-config";

export function LandingFooter() {
  return (
    <footer className="border-t border-border bg-muted/40">
      <div className="mx-auto grid max-w-6xl gap-8 px-4 py-12 sm:px-6 sm:grid-cols-3">
        <div>
          <p className="font-heading text-sm font-semibold">{siteConfig.name}</p>
          <p className="mt-2 max-w-xs text-sm text-muted-foreground">{siteConfig.description}</p>
        </div>
        <nav aria-label="Product">
          <h2 className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
            Product
          </h2>
          <ul className="mt-3 space-y-2 text-sm">
            {landingNavLinks.map((link) => (
              <li key={link.href}>
                <a href={link.href} className="text-muted-foreground hover:text-foreground">
                  {link.label}
                </a>
              </li>
            ))}
          </ul>
        </nav>
        <nav aria-label="App">
          <h2 className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
            App
          </h2>
          <ul className="mt-3 space-y-2 text-sm">
            {appRoutes.map((route) => (
              <li key={route.href}>
                <Link href={route.href} className="text-muted-foreground hover:text-foreground">
                  {route.label}
                </Link>
              </li>
            ))}
          </ul>
        </nav>
      </div>
      <div className="border-t border-border px-4 py-4 text-xs text-muted-foreground sm:px-6">
        © {new Date().getFullYear()} {siteConfig.name}. All rights reserved.
      </div>
    </footer>
  );
}
