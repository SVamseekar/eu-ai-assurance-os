"use client";

import Link from "next/link";
import { useState } from "react";
import { Loader2 } from "lucide-react";

import { AuthCard, FormError, authInputClassName } from "@/components/auth/auth-card";
import { TurnstileWidget, turnstileEnabled } from "@/components/auth/turnstile-widget";
import { Button } from "@/components/ui/button";

export function ForgotPasswordForm() {
  const [email, setEmail] = useState("");
  const [turnstileToken, setTurnstileToken] = useState("");
  const [challengeKey, setChallengeKey] = useState(0);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const [done, setDone] = useState(false);

  async function onSubmit(event: React.FormEvent) {
    event.preventDefault();
    setBusy(true);
    setError(null);
    const res = await fetch("/api/auth/password/forgot", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, turnstileToken }),
    });
    setBusy(false);
    setTurnstileToken("");
    setChallengeKey((k) => k + 1);
    if (res.status === 202) {
      setDone(true);
      return;
    }
    const body = await res.json().catch(() => null);
    setError(typeof body?.error === "string" ? body.error : "Could not send the email. Please try again.");
  }

  const footer = (
    <Link href="/login" className="font-medium text-foreground underline-offset-4 hover:underline">
      Back to sign in
    </Link>
  );

  if (done) {
    return (
      <AuthCard title="Check your inbox" footer={footer}>
        <p className="text-sm">
          If an account exists for that email, we sent a reset link. It works once and expires in 1 hour.
        </p>
      </AuthCard>
    );
  }

  return (
    <AuthCard title="Reset your password" description="Enter your email and we will send you a reset link." footer={footer}>
      <form onSubmit={onSubmit} className="space-y-4">
        <FormError message={error} />
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
            className={authInputClassName}
          />
        </div>
        <TurnstileWidget key={challengeKey} onToken={setTurnstileToken} />
        <Button
          type="submit"
          size="lg"
          disabled={busy || (turnstileEnabled && turnstileToken === "")}
          className="h-11 w-full gap-2"
        >
          {busy ? (
            <>
              <Loader2 className="size-4 animate-spin" aria-hidden="true" />
              Sending…
            </>
          ) : (
            "Send reset link"
          )}
        </Button>
      </form>
    </AuthCard>
  );
}
