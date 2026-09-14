import Link from "next/link";
import { ArrowRight } from "lucide-react";

import { relatedByPath } from "@/lib/landing-content";

export function RelatedPages({ path }: { path: string }) {
  const items = relatedByPath[path];
  if (!items?.length) return null;

  return (
    <section aria-labelledby="keep-reading-heading" className="border-t border-border">
      <div className="mx-auto max-w-6xl px-4 py-12 sm:px-6">
        <h2
          id="keep-reading-heading"
          className="font-heading text-xl font-semibold tracking-tight"
        >
          Keep reading
        </h2>
        <ul className="mt-6 grid gap-4 sm:grid-cols-3">
          {items.map((item) => (
            <li key={item.href}>
              <Link
                href={item.href}
                className="group flex h-full flex-col rounded-xl border border-border bg-card p-4 shadow-sm transition-colors hover:border-primary/40 hover:bg-muted/30"
              >
                <span className="font-heading text-sm font-semibold">{item.title}</span>
                <span className="mt-1 flex-1 text-sm text-muted-foreground">
                  {item.description}
                </span>
                <span className="mt-3 inline-flex items-center gap-1 text-sm font-medium text-primary">
                  Open {item.title}
                  <ArrowRight
                    className="h-3.5 w-3.5 transition-transform group-hover:translate-x-0.5"
                    aria-hidden
                  />
                </span>
              </Link>
            </li>
          ))}
        </ul>
      </div>
    </section>
  );
}
