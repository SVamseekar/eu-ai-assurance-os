import Link from "next/link";
import type { ReactNode } from "react";
import { ShieldCheck } from "lucide-react";

import { siteConfig } from "@/lib/site-config";
import { cn } from "@/lib/utils";

export const authInputClassName = cn(
  "w-full rounded-lg border border-border bg-background px-3 py-2.5 text-sm",
  "placeholder:text-muted-foreground",
  "outline-none transition-colors",
  "focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50",
  "disabled:cursor-not-allowed disabled:opacity-50",
);

/** Centered card used by the signup, verification and password pages. */
export function AuthCard({
  title,
  description,
  children,
  footer,
}: {
  title: string;
  description?: string;
  children: ReactNode;
  footer?: ReactNode;
}) {
  return (
    <div className="flex min-h-screen flex-col bg-background">
      <header className="flex h-14 items-center justify-between border-b border-border px-4 sm:px-6">
        <Link href="/" className="inline-flex items-center gap-2 font-heading text-sm font-semibold">
          <ShieldCheck className="size-4 text-primary" aria-hidden="true" />
          {siteConfig.name}
        </Link>
        <Link href="/login" className="text-sm text-muted-foreground hover:text-foreground">
          Sign in
        </Link>
      </header>
      <main className="flex flex-1 items-center justify-center px-4 py-10 sm:px-6">
        <div className="w-full max-w-[400px]">
          <h1 className="font-heading text-2xl font-semibold tracking-tight">{title}</h1>
          {description ? <p className="mt-2 text-sm text-muted-foreground">{description}</p> : null}
          <div className="mt-6">{children}</div>
          {footer ? <div className="mt-8 text-center text-sm text-muted-foreground">{footer}</div> : null}
        </div>
      </main>
    </div>
  );
}

export function FormError({ message }: { message: string | null }) {
  if (!message) return null;
  return (
    <p role="alert" className="rounded-lg border border-destructive/30 bg-destructive/10 px-3 py-2.5 text-sm text-destructive">
      {message}
    </p>
  );
}
