import Link from "next/link";
import { ShieldCheck } from "lucide-react";

import { Button } from "@/components/ui/button";
import { landingNavLinks, siteConfig } from "@/lib/site-config";

export function LandingHeader() {
  return (
    <header className="sticky top-0 z-40 border-b border-border bg-background/80 backdrop-blur-md">
      <div className="mx-auto flex h-14 max-w-6xl items-center justify-between px-4 sm:px-6">
        <Link href="/" className="flex items-center gap-2 font-heading text-sm font-semibold">
          <ShieldCheck className="h-4 w-4 text-primary" aria-hidden="true" />
          {siteConfig.name}
        </Link>
        <nav aria-label="Main" className="hidden items-center gap-6 text-sm text-muted-foreground md:flex">
          {landingNavLinks.map((link) => (
            <a key={link.href} href={link.href} className="hover:text-foreground">
              {link.label}
            </a>
          ))}
        </nav>
        <Button render={<Link href="/command" />}>Open Dashboard</Button>
      </div>
    </header>
  );
}
