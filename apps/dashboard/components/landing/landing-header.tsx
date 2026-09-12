"use client";

import Link from "next/link";
import { useEffect, useId, useRef, useState } from "react";
import { Menu, ShieldCheck, X } from "lucide-react";

import { Button } from "@/components/ui/button";
import { landingNavLinks, siteConfig } from "@/lib/site-config";

export function LandingHeader() {
  const [open, setOpen] = useState(false);
  const menuId = useId();
  const menuRef = useRef<HTMLDivElement>(null);
  const toggleRef = useRef<HTMLButtonElement>(null);

  useEffect(() => {
    if (!open) return;

    const menu = menuRef.current;
    const previouslyFocused = document.activeElement as HTMLElement | null;
    const focusables = menu?.querySelectorAll<HTMLElement>(
      'a[href], button:not([disabled]), [tabindex]:not([tabindex="-1"])',
    );
    focusables?.[0]?.focus();

    function onKeyDown(event: KeyboardEvent) {
      if (event.key === "Escape") {
        setOpen(false);
        toggleRef.current?.focus();
        return;
      }
      if (event.key !== "Tab" || !focusables || focusables.length === 0) return;
      const first = focusables[0];
      const last = focusables[focusables.length - 1];
      if (event.shiftKey && document.activeElement === first) {
        event.preventDefault();
        last.focus();
      } else if (!event.shiftKey && document.activeElement === last) {
        event.preventDefault();
        first.focus();
      }
    }

    document.addEventListener("keydown", onKeyDown);
    return () => {
      document.removeEventListener("keydown", onKeyDown);
      previouslyFocused?.focus?.();
    };
  }, [open]);

  return (
    <header className="sticky top-0 z-40 border-b border-border bg-background/80 backdrop-blur-md">
      <div className="mx-auto flex h-14 max-w-6xl items-center justify-between gap-4 px-4 sm:px-6">
        <Link href="/" className="flex items-center gap-2 font-heading text-sm font-semibold">
          <ShieldCheck className="h-4 w-4 text-primary" aria-hidden="true" />
          {siteConfig.name}
        </Link>

        <nav
          aria-label="Main"
          className="hidden items-center gap-6 text-sm text-muted-foreground md:flex"
        >
          {landingNavLinks.map((link) => (
            <a key={link.href} href={link.href} className="hover:text-foreground">
              {link.label}
            </a>
          ))}
        </nav>

        <div className="hidden items-center gap-2 md:flex">
          <Button variant="outline" render={<Link href="/login" />}>
            Sign in
          </Button>
          <Button render={<Link href="/request-demo" />}>Request demo</Button>
        </div>

        <button
          ref={toggleRef}
          type="button"
          className="inline-flex h-8 w-8 items-center justify-center rounded-lg border border-border md:hidden"
          aria-expanded={open}
          aria-controls={menuId}
          aria-label={open ? "Close menu" : "Open menu"}
          onClick={() => setOpen((v) => !v)}
        >
          {open ? <X className="h-4 w-4" /> : <Menu className="h-4 w-4" />}
        </button>
      </div>

      {open ? (
        <div
          ref={menuRef}
          id={menuId}
          className="border-t border-border bg-background px-4 py-4 md:hidden"
        >
          <nav aria-label="Mobile" className="flex flex-col gap-3 text-sm">
            {landingNavLinks.map((link) => (
              <a
                key={link.href}
                href={link.href}
                className="text-muted-foreground hover:text-foreground"
                onClick={() => setOpen(false)}
              >
                {link.label}
              </a>
            ))}
            <Link
              href="/privacy"
              className="text-muted-foreground hover:text-foreground"
              onClick={() => setOpen(false)}
            >
              Privacy
            </Link>
            <Link
              href="/terms"
              className="text-muted-foreground hover:text-foreground"
              onClick={() => setOpen(false)}
            >
              Terms
            </Link>
            <Link
              href="/refunds"
              className="text-muted-foreground hover:text-foreground"
              onClick={() => setOpen(false)}
            >
              Refunds
            </Link>
            <div className="mt-2 flex flex-col gap-2">
              <Button variant="outline" render={<Link href="/login" />}>
                Sign in
              </Button>
              <Button render={<Link href="/request-demo" />}>Request demo</Button>
            </div>
          </nav>
        </div>
      ) : null}
    </header>
  );
}
