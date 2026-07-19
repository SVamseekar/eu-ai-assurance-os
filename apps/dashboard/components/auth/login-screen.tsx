"use client";

import Link from "next/link";
import { useMemo, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import {
  AlertCircle,
  ArrowRight,
  CheckCircle2,
  Loader2,
  Lock,
  ShieldCheck,
} from "lucide-react";

import { Button } from "@/components/ui/button";
import { siteConfig } from "@/lib/site-config";
import { cn } from "@/lib/utils";

const AUTH_ERROR_MESSAGES: Record<string, string> = {
  not_provisioned:
    "No account is provisioned for this identity. Request a demo or contact your administrator.",
  denied: "Sign-in was cancelled or denied by the identity provider.",
  state: "Sign-in session expired or was invalid. Please try again.",
  unsupported_provider: "That identity provider is not supported.",
  sign_in_failed: "Sign-in failed. Please try again or use email and password.",
  sign_in_unavailable:
    "Social sign-in is temporarily unavailable. Use email and password, or try again later.",
  not_configured:
    "Social sign-in is not configured in this environment. Use email and password, or request a demo.",
};

const TRUST_POINTS = [
  "PASS / REVIEW / BLOCKED release decisions",
  "Cited evidence RAG and eval gates",
  "Hash-chained audit ledger",
  "Google & Microsoft SSO when provisioned",
] as const;

function GoogleIcon({ className }: { className?: string }) {
  return (
    <svg className={className} viewBox="0 0 24 24" aria-hidden="true">
      <path
        fill="#4285F4"
        d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
      />
      <path
        fill="#34A853"
        d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
      />
      <path
        fill="#FBBC05"
        d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"
      />
      <path
        fill="#EA4335"
        d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"
      />
    </svg>
  );
}

function MicrosoftIcon({ className }: { className?: string }) {
  return (
    <svg className={className} viewBox="0 0 23 23" aria-hidden="true">
      <path fill="#f25022" d="M1 1h10v10H1z" />
      <path fill="#00a4ef" d="M12 1h10v10H12z" />
      <path fill="#7fba00" d="M1 12h10v10H1z" />
      <path fill="#ffb900" d="M12 12h10v10H12z" />
    </svg>
  );
}

const inputClassName = cn(
  "w-full rounded-lg border border-border bg-background px-3 py-2.5 text-sm",
  "placeholder:text-muted-foreground",
  "outline-none transition-colors",
  "focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50",
  "disabled:cursor-not-allowed disabled:opacity-50",
);

export function LoginScreen() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [oauthRedirecting, setOauthRedirecting] = useState<
    "google" | "microsoft" | null
  >(null);

  const oauthError = useMemo(() => {
    const code = searchParams.get("auth_error");
    if (!code) return null;
    return AUTH_ERROR_MESSAGES[code] ?? "Sign-in failed. Please try again.";
  }, [searchParams]);

  const displayError = error ?? oauthError;
  const busy = submitting || oauthRedirecting !== null;

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault();
    setSubmitting(true);
    setError(null);

    const response = await fetch("/api/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password }),
    });

    setSubmitting(false);
    if (!response.ok) {
      setError("Invalid email or password");
      return;
    }
    const next = searchParams.get("next") || "/command";
    router.push(next.startsWith("/") && !next.startsWith("//") ? next : "/command");
  }

  function startOAuth(provider: "google" | "microsoft") {
    setError(null);
    setOauthRedirecting(provider);
    window.location.assign(`/api/auth/oauth/${provider}/start`);
  }

  return (
    <div className="flex min-h-screen flex-col bg-background lg:flex-row">
      {/* Brand panel */}
      <aside className="relative hidden overflow-hidden border-r border-border bg-muted/40 lg:flex lg:w-[46%] lg:flex-col lg:justify-between lg:px-12 lg:py-12 xl:px-16">
        <div
          className="pointer-events-none absolute inset-0 opacity-40"
          aria-hidden="true"
          style={{
            background:
              "radial-gradient(ellipse 80% 60% at 20% 20%, color-mix(in oklch, var(--primary) 22%, transparent), transparent), radial-gradient(ellipse 60% 50% at 80% 80%, color-mix(in oklch, var(--primary) 12%, transparent), transparent)",
          }}
        />
        <div className="relative z-10">
          <Link
            href="/"
            className="inline-flex items-center gap-2 font-heading text-sm font-semibold tracking-tight"
          >
            <span className="flex size-8 items-center justify-center rounded-lg bg-primary text-primary-foreground">
              <ShieldCheck className="size-4" aria-hidden="true" />
            </span>
            {siteConfig.name}
          </Link>
          <h1 className="mt-12 max-w-md font-heading text-3xl font-semibold tracking-tight xl:text-4xl">
            Ship AI systems in the EU with evidence, not guesswork.
          </h1>
          <p className="mt-4 max-w-md text-sm leading-relaxed text-muted-foreground">
            Sign in to your tenant workspace — risk classification, cited
            evidence, eval gates, contracts, and release decisions in one
            control plane.
          </p>
          <ul className="mt-10 space-y-3">
            {TRUST_POINTS.map((point) => (
              <li key={point} className="flex items-start gap-2.5 text-sm">
                <CheckCircle2
                  className="mt-0.5 size-4 shrink-0 text-primary"
                  aria-hidden="true"
                />
                <span>{point}</span>
              </li>
            ))}
          </ul>
        </div>
        <p className="relative z-10 text-xs text-muted-foreground">
          Access is provisioned per organisation.{" "}
          <Link href="/request-demo" className="text-foreground underline-offset-4 hover:underline">
            Request a demo
          </Link>{" "}
          to get set up.
        </p>
      </aside>

      {/* Form panel */}
      <div className="flex flex-1 flex-col">
        <header className="flex h-14 items-center justify-between border-b border-border px-4 sm:px-6 lg:border-0 lg:px-10 lg:pt-6">
          <Link
            href="/"
            className="inline-flex items-center gap-2 font-heading text-sm font-semibold lg:hidden"
          >
            <ShieldCheck className="size-4 text-primary" aria-hidden="true" />
            {siteConfig.shortName}
          </Link>
          <Link
            href="/request-demo"
            className="ml-auto text-sm text-muted-foreground hover:text-foreground"
          >
            Request a demo
          </Link>
        </header>

        <main className="flex flex-1 items-center justify-center px-4 py-10 sm:px-6 lg:px-10">
          <div className="w-full max-w-[400px]">
            <div className="mb-8">
              <div className="mb-4 flex size-10 items-center justify-center rounded-xl border border-border bg-card shadow-sm lg:hidden">
                <Lock className="size-4 text-primary" aria-hidden="true" />
              </div>
              <h2 className="font-heading text-2xl font-semibold tracking-tight">
                Sign in
              </h2>
              <p className="mt-2 text-sm text-muted-foreground">
                Use your organisation account. SSO requires a provisioned user.
              </p>
            </div>

            {displayError ? (
              <div
                role="alert"
                className="mb-6 flex gap-3 rounded-lg border border-destructive/30 bg-destructive/10 px-3 py-3 text-sm text-destructive"
              >
                <AlertCircle className="mt-0.5 size-4 shrink-0" aria-hidden="true" />
                <p>{displayError}</p>
              </div>
            ) : null}

            <div className="space-y-3">
              <Button
                type="button"
                variant="outline"
                size="lg"
                disabled={busy}
                className="h-11 w-full justify-center gap-3 border-border bg-card text-foreground shadow-sm hover:bg-muted"
                onClick={() => startOAuth("google")}
              >
                {oauthRedirecting === "google" ? (
                  <Loader2 className="size-4 animate-spin" aria-hidden="true" />
                ) : (
                  <GoogleIcon className="size-4" />
                )}
                {oauthRedirecting === "google"
                  ? "Connecting to Google…"
                  : "Continue with Google"}
              </Button>
              <Button
                type="button"
                variant="outline"
                size="lg"
                disabled={busy}
                className="h-11 w-full justify-center gap-3 border-border bg-card text-foreground shadow-sm hover:bg-muted"
                onClick={() => startOAuth("microsoft")}
              >
                {oauthRedirecting === "microsoft" ? (
                  <Loader2 className="size-4 animate-spin" aria-hidden="true" />
                ) : (
                  <MicrosoftIcon className="size-4" />
                )}
                {oauthRedirecting === "microsoft"
                  ? "Connecting to Microsoft…"
                  : "Continue with Microsoft"}
              </Button>
            </div>

            <div className="relative my-8">
              <div className="absolute inset-0 flex items-center" aria-hidden="true">
                <span className="w-full border-t border-border" />
              </div>
              <div className="relative flex justify-center text-xs">
                <span className="bg-background px-3 font-medium uppercase tracking-wider text-muted-foreground">
                  or email
                </span>
              </div>
            </div>

            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="space-y-1.5">
                <label htmlFor="email" className="text-sm font-medium">
                  Work email
                </label>
                <input
                  id="email"
                  type="email"
                  autoComplete="email"
                  required
                  disabled={busy}
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="you@company.com"
                  className={inputClassName}
                />
              </div>
              <div className="space-y-1.5">
                <label htmlFor="password" className="text-sm font-medium">
                  Password
                </label>
                <input
                  id="password"
                  type="password"
                  autoComplete="current-password"
                  required
                  disabled={busy}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  className={inputClassName}
                />
              </div>
              <Button
                type="submit"
                size="lg"
                disabled={busy}
                className="h-11 w-full gap-2"
              >
                {submitting ? (
                  <>
                    <Loader2 className="size-4 animate-spin" aria-hidden="true" />
                    Signing in…
                  </>
                ) : (
                  <>
                    Sign in
                    <ArrowRight className="size-4" aria-hidden="true" />
                  </>
                )}
              </Button>
            </form>

            <p className="mt-8 text-center text-sm text-muted-foreground">
              Need access for your team?{" "}
              <Link
                href="/request-demo"
                className="font-medium text-foreground underline-offset-4 hover:underline"
              >
                Request a demo
              </Link>
            </p>

            <p className="mt-6 text-center text-xs text-muted-foreground">
              By signing in you agree to our{" "}
              <Link href="/terms" className="underline-offset-4 hover:underline">
                Terms
              </Link>{" "}
              and{" "}
              <Link href="/privacy" className="underline-offset-4 hover:underline">
                Privacy Policy
              </Link>
              .
            </p>
          </div>
        </main>

        <footer className="border-t border-border px-4 py-4 text-center text-xs text-muted-foreground sm:px-6">
          <Link href="/" className="hover:text-foreground">
            ← Back to {siteConfig.name}
          </Link>
          <span className="mx-2 text-border">·</span>
          <a
            href={`mailto:${siteConfig.supportEmail}`}
            className="hover:text-foreground"
          >
            Support
          </a>
        </footer>
      </div>
    </div>
  );
}
