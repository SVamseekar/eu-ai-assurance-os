"use client";

import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { useState } from "react";
import { Loader2 } from "lucide-react";

import { AuthCard, FormError, authInputClassName } from "@/components/auth/auth-card";
import { Button } from "@/components/ui/button";
import { passwordProblem } from "@/lib/auth-forms";

export function ResetPasswordForm() {
  const token = useSearchParams().get("token") ?? "";
  const router = useRouter();
  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [expired, setExpired] = useState(false);
  const [busy, setBusy] = useState(false);

  async function onSubmit(event: React.FormEvent) {
    event.preventDefault();
    const problem = passwordProblem(password, confirm);
    if (problem) {
      setError(problem);
      return;
    }
    setBusy(true);
    setError(null);
    const res = await fetch("/api/auth/password/reset", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ token, newPassword: password }),
    });
    setBusy(false);
    if (res.status === 204) {
      router.replace("/login?reset=1");
      return;
    }
    if (res.status === 410) {
      setExpired(true);
      return;
    }
    const body = await res.json().catch(() => null);
    setError(typeof body?.error === "string" ? body.error : "Could not update the password. Please try again.");
  }

  if (!token || expired) {
    return (
      <AuthCard
        title="This link can't be used"
        description="It is missing, invalid, already used, or expired."
        footer={
          <Link href="/forgot-password" className="font-medium text-foreground underline-offset-4 hover:underline">
            Request a new reset link
          </Link>
        }
      >
        {null}
      </AuthCard>
    );
  }

  return (
    <AuthCard title="Choose a new password" description="All signed-in sessions will be ended.">
      <form onSubmit={onSubmit} className="space-y-4">
        <FormError message={error} />
        <div className="space-y-1.5">
          <label htmlFor="password" className="text-sm font-medium">
            New password (12+ characters)
          </label>
          <input
            id="password"
            type="password"
            autoComplete="new-password"
            required
            minLength={12}
            maxLength={128}
            disabled={busy}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className={authInputClassName}
          />
        </div>
        <div className="space-y-1.5">
          <label htmlFor="confirm" className="text-sm font-medium">
            Confirm new password
          </label>
          <input
            id="confirm"
            type="password"
            autoComplete="new-password"
            required
            disabled={busy}
            value={confirm}
            onChange={(e) => setConfirm(e.target.value)}
            className={authInputClassName}
          />
        </div>
        <Button type="submit" size="lg" disabled={busy} className="h-11 w-full gap-2">
          {busy ? (
            <>
              <Loader2 className="size-4 animate-spin" aria-hidden="true" />
              Saving…
            </>
          ) : (
            "Update password"
          )}
        </Button>
      </form>
    </AuthCard>
  );
}
