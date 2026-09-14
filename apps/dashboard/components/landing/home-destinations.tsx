import Link from "next/link";
import { ArrowRight } from "lucide-react";

import { homeDestinations } from "@/lib/landing-content";

export function HomeDestinations() {
  return (
    <section aria-label="Explore the product" className="mx-auto max-w-6xl px-4 py-16 sm:px-6">
      <h2 className="font-heading text-2xl font-semibold tracking-tight sm:text-3xl">
        Explore the product
      </h2>
      <p className="mt-3 max-w-2xl text-muted-foreground">
        See how a release decision is made, how we read public AI claims, and
        how to start a conversation about your systems.
      </p>
      <ul className="mt-8 grid gap-4 sm:grid-cols-2">
        {homeDestinations.map((item) => (
          <li key={item.href}>
            <Link
              href={item.href}
              className="group flex h-full flex-col rounded-xl border border-border bg-card p-5 shadow-sm transition-colors hover:border-primary/40 hover:bg-muted/30"
            >
              <span className="font-heading text-base font-semibold">{item.title}</span>
              <span className="mt-2 flex-1 text-sm text-muted-foreground">
                {item.description}
              </span>
              <span className="mt-4 inline-flex items-center gap-1 text-sm font-medium text-primary">
                Open {item.title}
                <ArrowRight
                  className="h-4 w-4 transition-transform group-hover:translate-x-0.5"
                  aria-hidden
                />
              </span>
            </Link>
          </li>
        ))}
      </ul>
    </section>
  );
}
