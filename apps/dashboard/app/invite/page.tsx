"use client";

import { useEffect, useState, Suspense } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import { siteConfig } from "@/lib/site-config";

type Preview = {
  email: string;
  role: string;
  tenantName: string;
  expiresAt: string;
  expired: boolean;
  accepted: boolean;
};

function InviteForm() {
  const params = useSearchParams();
  const token = params.get("token") ?? "";
  const router = useRouter();
  const [preview, setPreview] = useState<Preview | null>(null);
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    if (!token) return;
    fetch(`/api/auth/invites/${encodeURIComponent(token)}`)
      .then(async (res) => {
        if (!res.ok) throw new Error("Invite not found");
        return res.json();
      })
      .then(setPreview)
      .catch(() => setError("This invite is invalid or has expired."));
  }, [token]);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    const res = await fetch("/api/auth/accept-invite", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ token, password }),
    });
    setBusy(false);
    if (!res.ok) {
      setError("Could not accept invite. Use a password of at least 12 characters.");
      return;
    }
    router.replace("/command");
  }

  return (
    <main className="mx-auto max-w-md px-4 py-16">
      <p className="text-sm text-muted-foreground">
        <Link href="/login">← Sign in</Link>
      </p>
      <h1 className="font-heading mt-4 text-2xl font-semibold">Accept workspace invite</h1>
      <p className="mt-2 text-sm text-muted-foreground">{siteConfig.name}</p>
      {error && <p className="mt-4 text-sm text-destructive">{error}</p>}
      {preview && !preview.expired && !preview.accepted && (
        <form onSubmit={onSubmit} className="mt-6 space-y-4">
          <p className="text-sm">
            {preview.email} · {preview.role} · {preview.tenantName}
          </p>
          <label className="block text-sm">
            Password (12+ characters)
            <input
              type="password"
              minLength={12}
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="mt-1 w-full rounded-lg border border-border bg-background px-3 py-2 text-sm"
            />
          </label>
          <Button type="submit" disabled={busy || password.length < 12}>
            {busy ? "Saving…" : "Join workspace"}
          </Button>
        </form>
      )}
      {!token && <p className="mt-4 text-sm">Missing invite token.</p>}
    </main>
  );
}

export default function InvitePage() {
  return (
    <Suspense fallback={<p className="p-8 text-sm">Loading invite…</p>}>
      <InviteForm />
    </Suspense>
  );
}
