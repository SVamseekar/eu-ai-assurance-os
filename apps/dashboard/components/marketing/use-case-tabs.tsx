"use client";

import { useEffect, useId, useState } from "react";
import { Workflow } from "lucide-react";

import { ButtonLink, CheckDot, PhotoSlot } from "@/components/marketing/primitives";
import { marketingMedia } from "@/lib/marketing-media";
import type { UseCase } from "@/lib/use-cases";
import { cn } from "@/lib/utils";

/** Storyboard frame 05. The URL hash (#engineering, #legal, …) selects a tab so menu links land on it. */
export function UseCaseTabs({ cases }: { cases: UseCase[] }) {
  const baseId = useId();
  const [active, setActive] = useState(0);

  useEffect(() => {
    function fromHash() {
      const index = cases.findIndex((c) => `#${c.id}` === window.location.hash);
      if (index >= 0) setActive(index);
    }
    // Next.js updates the URL with pushState for same-page hash links, which fires no hashchange,
    // so links to /who-its-for#<id> clicked on this page (menus, footer) are caught here too.
    function fromClick(event: MouseEvent) {
      const anchor = (event.target as Element | null)?.closest?.("a[href*='#']");
      if (!(anchor instanceof HTMLAnchorElement)) return;
      const url = new URL(anchor.href);
      if (url.pathname !== window.location.pathname) return;
      const index = cases.findIndex((c) => `#${c.id}` === url.hash);
      if (index >= 0) setActive(index);
    }
    fromHash();
    window.addEventListener("hashchange", fromHash);
    window.addEventListener("popstate", fromHash);
    document.addEventListener("click", fromClick, true);
    return () => {
      window.removeEventListener("hashchange", fromHash);
      window.removeEventListener("popstate", fromHash);
      document.removeEventListener("click", fromClick, true);
    };
  }, [cases]);

  function select(index: number) {
    setActive(index);
    window.history.replaceState(null, "", `#${cases[index].id}`);
  }

  function onKeyDown(event: React.KeyboardEvent<HTMLButtonElement>) {
    if (event.key !== "ArrowRight" && event.key !== "ArrowLeft") return;
    event.preventDefault();
    const next = (active + (event.key === "ArrowRight" ? 1 : cases.length - 1)) % cases.length;
    select(next);
    document.getElementById(cases[next].id)?.focus();
  }

  const current = cases[active];

  return (
    <div>
      <div
        role="tablist"
        aria-label="Teams"
        className="grid grid-cols-2 gap-1 rounded-xl bg-mist p-1 sm:grid-cols-3 lg:grid-cols-6"
      >
        {cases.map((c, i) => (
          <button
            key={c.id}
            id={c.id}
            role="tab"
            type="button"
            aria-selected={i === active}
            aria-controls={`${baseId}-panel`}
            tabIndex={i === active ? 0 : -1}
            onClick={() => select(i)}
            onKeyDown={onKeyDown}
            className={cn(
              "scroll-mt-32 rounded-lg border-b-2 px-3 py-3 text-center text-sm font-semibold transition-colors",
              i === active
                ? "border-brand bg-white text-brand shadow-sm"
                : "border-transparent text-ink hover:bg-white/60",
            )}
          >
            {c.tab}
          </button>
        ))}
      </div>

      <div
        id={`${baseId}-panel`}
        role="tabpanel"
        aria-labelledby={cases[active].id}
        className="mt-8 grid items-center gap-10 lg:grid-cols-2"
      >
        <div className="relative">
          <PhotoSlot
            slot={marketingMedia.engineerAtDesk}
            className="aspect-[3/2] rounded-2xl bg-navy-950"
            fallback={<div className="mk-night absolute inset-0" aria-hidden="true" />}
          />
          <UseCaseOverlay title={current.panelTitle} items={current.panelItems} />
        </div>
        <div className="order-first lg:order-none">
          <h3 className="text-2xl font-bold tracking-tight text-ink">{current.heading}</h3>
          <ul className="mt-5 space-y-3">
            {current.points.map((point) => (
              <li key={point} className="flex items-start gap-3 text-ink/85">
                <CheckDot className="mt-0.5" />
                {point}
              </li>
            ))}
          </ul>
          <div className="mt-8 flex flex-wrap gap-3">
            <ButtonLink href={current.primary.href} variant="primary" arrow>
              {current.primary.label}
            </ButtonLink>
            <ButtonLink href={current.secondary.href} variant="outline">
              {current.secondary.label}
            </ButtonLink>
          </div>
        </div>
      </div>
    </div>
  );
}

/** Checklist card that sits over the photo (or the night backdrop until a photo is supplied). */
function UseCaseOverlay({ title, items }: { title: string; items: string[] }) {
  return (
    <div className="absolute inset-0 flex items-center justify-end p-4 sm:p-6" aria-hidden="true">
      <div className="w-60 rounded-xl border border-white/10 bg-navy-900/90 p-4 text-white shadow-xl backdrop-blur">
        <p className="flex items-center gap-2 text-sm font-semibold">
          <Workflow className="h-4 w-4 text-periwinkle" />
          {title}
        </p>
        <ul className="mt-3 space-y-2">
          {items.map((item) => (
            <li key={item} className="flex items-center gap-2 text-[13px] text-white/85">
              <CheckDot className="h-4 w-4 [&>svg]:h-2.5 [&>svg]:w-2.5" />
              {item}
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}
