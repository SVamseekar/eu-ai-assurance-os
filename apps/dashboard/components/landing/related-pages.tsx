import Link from "next/link";
import { ArrowRight } from "lucide-react";

import { relatedByPath } from "@/lib/landing-content";

export function RelatedPages({ path }: { path: string }) {
  const items = relatedByPath[path];
  if (!items?.length) return null;

  return (
    <section aria-labelledby="keep-reading-heading" className="border-t border-line bg-white">
      <div className="mx-auto max-w-7xl px-4 py-14 sm:px-6 lg:px-8">
        <h2
          id="keep-reading-heading"
          className="text-2xl font-bold tracking-tight text-ink"
        >
          Keep reading
        </h2>
        <ul className="mt-6 grid gap-4 sm:grid-cols-3">
          {items.map((item) => (
            <li key={item.href}>
              <Link
                href={item.href}
                className="group flex h-full flex-col rounded-2xl border border-line bg-white p-5 transition-shadow hover:border-brand/40 hover:shadow-lg hover:shadow-navy-950/5"
              >
                <span className="font-semibold text-ink">{item.title}</span>
                <span className="mt-1 flex-1 text-sm text-muted-foreground">
                  {item.description}
                </span>
                <span className="mt-3 inline-flex items-center gap-1 text-sm font-semibold text-brand">
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
