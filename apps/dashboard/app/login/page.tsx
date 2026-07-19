"use client";

import { Suspense, useMemo, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";

const AUTH_ERROR_MESSAGES: Record<string, string> = {
  not_provisioned:
    "No account is provisioned for this identity. Contact your administrator.",
  denied: "Sign-in was cancelled or denied by the identity provider.",
  state: "Sign-in session expired or was invalid. Please try again.",
  unsupported_provider: "That identity provider is not supported.",
  sign_in_failed: "Sign-in failed. Please try again or use email and password.",
  sign_in_unavailable:
    "Social sign-in is temporarily unavailable. Use email and password, or try again later.",
  not_configured:
    "Social sign-in is not configured in this environment. Use email and password.",
};

function LoginForm() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const oauthError = useMemo(() => {
    const code = searchParams.get("auth_error");
    if (!code) return null;
    return AUTH_ERROR_MESSAGES[code] ?? "Sign-in failed. Please try again.";
  }, [searchParams]);

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
    const next = searchParams.get("next") || "/";
    router.push(next.startsWith("/") ? next : "/");
  }

  const displayError = error ?? oauthError;

  return (
    <div className="flex min-h-screen items-center justify-center px-4">
      <div className="w-full max-w-sm space-y-4 rounded-lg border p-6">
        <h1 className="text-lg font-semibold">Sign in</h1>
        {displayError && <p className="text-sm text-red-600">{displayError}</p>}

        <div className="space-y-2">
          <a
            href="/api/auth/oauth/google/start"
            className="flex w-full items-center justify-center rounded border px-3 py-2 text-sm font-medium hover:bg-muted"
          >
            Continue with Google
          </a>
          <a
            href="/api/auth/oauth/microsoft/start"
            className="flex w-full items-center justify-center rounded border px-3 py-2 text-sm font-medium hover:bg-muted"
          >
            Continue with Microsoft
          </a>
        </div>

        <div className="relative py-1">
          <div className="absolute inset-0 flex items-center">
            <span className="w-full border-t" />
          </div>
          <div className="relative flex justify-center text-xs uppercase">
            <span className="bg-background px-2 text-muted-foreground">or</span>
          </div>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-1">
            <label htmlFor="email" className="text-sm font-medium">
              Email
            </label>
            <input
              id="email"
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full rounded border px-3 py-2 text-sm"
            />
          </div>
          <div className="space-y-1">
            <label htmlFor="password" className="text-sm font-medium">
              Password
            </label>
            <input
              id="password"
              type="password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full rounded border px-3 py-2 text-sm"
            />
          </div>
          <button
            type="submit"
            disabled={submitting}
            className="w-full rounded bg-primary px-3 py-2 text-sm font-medium text-primary-foreground disabled:opacity-50"
          >
            {submitting ? "Signing in..." : "Sign in"}
          </button>
        </form>
      </div>
    </div>
  );
}

export default function LoginPage() {
  return (
    <Suspense
      fallback={
        <div className="flex min-h-screen items-center justify-center text-sm text-muted-foreground">
          Loading…
        </div>
      }
    >
      <LoginForm />
    </Suspense>
  );
}
