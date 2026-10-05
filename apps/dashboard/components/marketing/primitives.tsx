import Image from "next/image";
import Link from "next/link";
import type { ComponentProps, ReactNode } from "react";
import { ArrowRight, Check, CheckCircle2, ChevronRight, Home, OctagonX, ShieldAlert } from "lucide-react";

import type { MediaSlot } from "@/lib/marketing-media";
import { cn } from "@/lib/utils";

export type Tone = "dark" | "light";

export function Container({ className, children }: { className?: string; children: ReactNode }) {
  return <div className={cn("mx-auto w-full max-w-7xl px-4 sm:px-6 lg:px-8", className)}>{children}</div>;
}

/** Full-width band. Dark bands use the navy backdrop; light bands use white or mist. */
export function Band({
  tone,
  muted,
  grid,
  className,
  children,
  ...rest
}: {
  tone: Tone;
  muted?: boolean;
  grid?: boolean;
  className?: string;
  children: ReactNode;
} & Omit<ComponentProps<"section">, "className" | "children">) {
  return (
    <section
      {...rest}
      className={cn(
        "relative overflow-hidden",
        tone === "dark" ? "bg-navy-950 text-white" : muted ? "bg-mist text-ink" : "bg-white text-ink",
        className,
      )}
    >
      {grid && tone === "dark" ? (
        <div
          aria-hidden="true"
          className="mk-grid pointer-events-none absolute inset-0 [mask-image:radial-gradient(ellipse_at_top_right,black,transparent_70%)]"
        />
      ) : null}
      <div className="relative">{children}</div>
    </section>
  );
}

export function Eyebrow({ tone, children }: { tone: Tone; children: ReactNode }) {
  return (
    <p
      className={cn(
        "inline-block rounded-full px-3 py-1 text-[11px] leading-5 font-semibold uppercase tracking-[0.08em]",
        tone === "dark"
          ? "border border-white/10 bg-white/5 text-[#c9d1ff]"
          : "border border-brand/20 bg-brand-soft text-brand",
      )}
    >
      {children}
    </p>
  );
}

const buttonBase =
  "inline-flex h-11 items-center justify-center gap-2 rounded-lg px-5 text-sm font-semibold transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-60";

export const mkButton = {
  /** Electric blue on light surfaces. */
  primary: cn(buttonBase, "bg-brand text-white hover:bg-brand-hover focus-visible:ring-brand"),
  /** Periwinkle on dark surfaces (design board item 1). */
  primaryOnDark: cn(
    buttonBase,
    "bg-periwinkle text-navy-950 hover:bg-periwinkle-hover focus-visible:ring-periwinkle focus-visible:ring-offset-navy-950",
  ),
  /** Outlined on dark surfaces. */
  outlineOnDark: cn(
    buttonBase,
    "border border-white/20 bg-white/[0.03] text-white hover:bg-white/10 focus-visible:ring-white/60 focus-visible:ring-offset-navy-950",
  ),
  /** Solid navy, the board's "secondary CTA". */
  secondary: cn(buttonBase, "bg-navy-900 text-white hover:bg-navy-800 focus-visible:ring-navy-700"),
  /** Blue outline, the board's "tertiary CTA". */
  outline: cn(
    buttonBase,
    "border border-brand/40 bg-white text-brand hover:border-brand hover:bg-brand-soft focus-visible:ring-brand",
  ),
};

export function ButtonLink({
  href,
  variant,
  arrow,
  className,
  children,
  external,
}: {
  href: string;
  variant: keyof typeof mkButton;
  arrow?: boolean;
  className?: string;
  children: ReactNode;
  external?: boolean;
}) {
  const content = (
    <>
      {children}
      {arrow ? <ArrowRight className="h-4 w-4" aria-hidden="true" /> : null}
    </>
  );
  if (external) {
    return (
      <a href={href} target="_blank" rel="noopener noreferrer" className={cn(mkButton[variant], className)}>
        {content}
      </a>
    );
  }
  return (
    <Link href={href} className={cn(mkButton[variant], className)}>
      {content}
    </Link>
  );
}

export function TextLink({ href, tone, children }: { href: string; tone: Tone; children: ReactNode }) {
  return (
    <Link
      href={href}
      className={cn(
        "inline-flex items-center gap-1.5 text-sm font-semibold underline-offset-4 hover:underline",
        tone === "dark" ? "text-periwinkle" : "text-brand",
      )}
    >
      {children}
      <ArrowRight className="h-4 w-4" aria-hidden="true" />
    </Link>
  );
}

export type Crumb = { href: string; label: string };

/** Design board item 9. */
export function Breadcrumbs({ crumbs, tone }: { crumbs: Crumb[]; tone: Tone }) {
  return (
    <nav aria-label="Breadcrumb">
      <ol
        className={cn(
          "inline-flex flex-wrap items-center gap-2 rounded-lg px-3 py-2 text-sm",
          tone === "dark" ? "bg-white/5 text-on-dark-muted" : "bg-brand-soft/60 text-ink-muted",
        )}
      >
        {crumbs.map((crumb, index) => {
          const last = index === crumbs.length - 1;
          return (
            <li key={crumb.href} className="flex items-center gap-2">
              {index > 0 ? <ChevronRight className="h-3.5 w-3.5 opacity-60" aria-hidden="true" /> : null}
              {last ? (
                <span aria-current="page" className={tone === "dark" ? "text-white" : "text-ink"}>
                  {crumb.label}
                </span>
              ) : (
                <Link
                  href={crumb.href}
                  className={cn("hover:underline", tone === "dark" ? "text-periwinkle" : "text-brand")}
                >
                  {index === 0 && crumb.href === "/" ? (
                    <>
                      <Home className="h-4 w-4" aria-hidden="true" />
                      <span className="sr-only">{crumb.label}</span>
                    </>
                  ) : (
                    crumb.label
                  )}
                </Link>
              )}
            </li>
          );
        })}
      </ol>
    </nav>
  );
}

/** Design board item 15: eyebrow pill, bold headline, supporting copy. */
export function PageHeader({
  tone,
  eyebrow,
  title,
  description,
  crumbs,
  children,
  className,
}: {
  tone: Tone;
  eyebrow?: string;
  title: ReactNode;
  description?: ReactNode;
  crumbs?: Crumb[];
  children?: ReactNode;
  className?: string;
}) {
  return (
    <div className={cn("max-w-3xl", className)}>
      {crumbs?.length ? (
        <div className="mb-6">
          <Breadcrumbs crumbs={crumbs} tone={tone} />
        </div>
      ) : null}
      {eyebrow ? <Eyebrow tone={tone}>{eyebrow}</Eyebrow> : null}
      <h1
        className={cn(
          "mt-4 text-4xl font-bold tracking-tight text-balance sm:text-5xl",
          tone === "dark" ? "text-white" : "text-ink",
        )}
      >
        {title}
      </h1>
      {description ? (
        <p className={cn("mt-4 text-lg text-pretty", tone === "dark" ? "text-on-dark-muted" : "text-ink-muted")}>
          {description}
        </p>
      ) : null}
      {children}
    </div>
  );
}

export function SectionHeading({
  tone,
  eyebrow,
  title,
  description,
  align = "left",
}: {
  tone: Tone;
  eyebrow?: string;
  title: ReactNode;
  description?: ReactNode;
  align?: "left" | "center";
}) {
  return (
    <div className={cn("max-w-2xl", align === "center" && "mx-auto text-center")}>
      {eyebrow ? <Eyebrow tone={tone}>{eyebrow}</Eyebrow> : null}
      <h2
        className={cn(
          "mt-4 text-3xl font-bold tracking-tight text-balance sm:text-4xl",
          tone === "dark" ? "text-white" : "text-ink",
        )}
      >
        {title}
      </h2>
      {description ? (
        <p className={cn("mt-3 text-base text-pretty", tone === "dark" ? "text-on-dark-muted" : "text-ink-muted")}>
          {description}
        </p>
      ) : null}
    </div>
  );
}

export type Decision = "PASS" | "REVIEW" | "BLOCKED";

const decisionStyle: Record<Decision, { tile: string; text: string; icon: ReactNode }> = {
  PASS: {
    tile: "border-pass/25 bg-pass-soft",
    text: "text-pass",
    icon: <CheckCircle2 className="h-8 w-8 shrink-0 text-pass" aria-hidden="true" />,
  },
  REVIEW: {
    tile: "border-review/25 bg-review-soft",
    text: "text-review",
    icon: <ShieldAlert className="h-8 w-8 shrink-0 text-review" aria-hidden="true" />,
  },
  BLOCKED: {
    tile: "border-blocked/25 bg-blocked-soft",
    text: "text-blocked",
    icon: <OctagonX className="h-8 w-8 shrink-0 text-blocked" aria-hidden="true" />,
  },
};

/** Design board item 10. */
export function DecisionTile({
  decision,
  detail,
  meta,
  className,
}: {
  decision: Decision;
  detail: ReactNode;
  meta?: ReactNode;
  className?: string;
}) {
  const style = decisionStyle[decision];
  return (
    <div className={cn("flex items-start gap-3 rounded-xl border p-4", style.tile, className)}>
      {style.icon}
      <div>
        <p className={cn("text-lg font-bold tracking-tight", style.text)}>{decision}</p>
        {meta ? <p className="text-xs font-medium text-ink-muted">{meta}</p> : null}
        <p className="mt-1 text-sm text-ink-muted">{detail}</p>
      </div>
    </div>
  );
}

export function IconTile({
  icon: Icon,
  className,
}: {
  icon: React.ComponentType<{ className?: string }>;
  className?: string;
}) {
  return (
    <span className={cn("inline-flex h-11 w-11 shrink-0 items-center justify-center rounded-xl", className)}>
      <Icon className="h-5 w-5" aria-hidden="true" />
    </span>
  );
}

/** A photo the owner supplies later. Until then, built-in artwork fills the same box. */
export function PhotoSlot({
  slot,
  fallback,
  className,
  sizes = "(min-width: 1024px) 40vw, 100vw",
  priority,
}: {
  slot: MediaSlot;
  fallback: ReactNode;
  className?: string;
  sizes?: string;
  priority?: boolean;
}) {
  return (
    <div className={cn("relative overflow-hidden", className)}>
      {slot.src ? (
        <Image
          src={slot.src}
          alt={slot.alt}
          fill
          sizes={sizes}
          priority={priority}
          className="object-cover"
          style={slot.position ? { objectPosition: slot.position } : undefined}
        />
      ) : (
        fallback
      )}
    </div>
  );
}

/** "Illustrative example" caption under product mock-ups, so sample data never reads as a customer record. */
export function IllustrativeNote({ tone, children }: { tone: Tone; children?: ReactNode }) {
  return (
    <p className={cn("mt-3 text-xs", tone === "dark" ? "text-on-dark-muted/80" : "text-ink-muted")}>
      {children ?? "Illustrative example with sample data."}
    </p>
  );
}

/** Filled green check used in lists and mock-ups (storyboard style). */
export function CheckDot({ className }: { className?: string }) {
  return (
    <span
      aria-hidden="true"
      className={cn("inline-flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-pass text-white", className)}
    >
      <Check className="h-3 w-3" strokeWidth={3} />
    </span>
  );
}
