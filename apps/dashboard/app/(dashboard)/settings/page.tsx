"use client";

import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { api } from "@/lib/api";

const ROLES = [
  "ADMIN",
  "AI_ENGINEERING_LEAD",
  "COMPLIANCE_OFFICER",
  "LEGAL_COUNSEL",
  "AUDITOR",
] as const;

export default function SettingsPage() {
  const qc = useQueryClient();
  const users = useQuery({ queryKey: ["admin", "users"], queryFn: api.admin.users });
  const invites = useQuery({ queryKey: ["admin", "invites"], queryFn: api.admin.invites });
  const ops = useQuery({ queryKey: ["ops", "readiness"], queryFn: api.ops.readiness });
  const [email, setEmail] = useState("");
  const [role, setRole] = useState<(typeof ROLES)[number]>("COMPLIANCE_OFFICER");
  const [lastToken, setLastToken] = useState<string | null>(null);

  const invite = useMutation({
    mutationFn: () => api.admin.inviteUser({ email, role }),
    onSuccess: (created) => {
      setLastToken(created.inviteToken ?? null);
      setEmail("");
      void qc.invalidateQueries({ queryKey: ["admin"] });
    },
  });

  return (
    <div className="space-y-4">
      <Card>
        <CardHeader>
          <CardTitle>Workspace users</CardTitle>
          <CardDescription>
            Invite colleagues. The raw invite token is shown once — send it out of band.
            Only workspace admins can invite.
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <form
            className="flex flex-wrap items-end gap-2"
            onSubmit={(e) => {
              e.preventDefault();
              invite.mutate();
            }}
          >
            <label className="text-xs">
              Email
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="mt-1 block w-56 rounded-lg border border-border bg-background px-3 py-2 text-sm"
              />
            </label>
            <label className="text-xs">
              Role
              <select
                value={role}
                onChange={(e) => setRole(e.target.value as (typeof ROLES)[number])}
                className="mt-1 block rounded-lg border border-border bg-background px-3 py-2 text-sm"
              >
                {ROLES.map((r) => (
                  <option key={r} value={r}>
                    {r}
                  </option>
                ))}
              </select>
            </label>
            <Button type="submit" size="sm" disabled={invite.isPending || !email}>
              Invite
            </Button>
          </form>
          {invite.isError && (
            <p className="text-xs text-destructive">
              Invite failed. You need the ADMIN role, and the email must be unused.
            </p>
          )}
          {lastToken && (
            <p className="break-all rounded-lg bg-muted px-3 py-2 text-xs">
              Send this link once:{" "}
              <span className="font-mono">/invite?token={lastToken}</span>
            </p>
          )}
          <ul className="text-sm">
            {(users.data ?? []).map((user) => (
              <li key={user.id} className="border-b border-border py-2">
                {user.email} · {user.role}
              </li>
            ))}
          </ul>
          {(invites.data ?? []).length > 0 && (
            <p className="text-xs text-muted-foreground">
              Open invites: {invites.data?.map((i) => i.email).join(", ")}
            </p>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Operator checklist</CardTitle>
          <CardDescription>
            Not an SLA or SOC 2. ADMIN only.
          </CardDescription>
        </CardHeader>
        <CardContent>
          {ops.isError && (
            <p className="text-xs text-muted-foreground">Ops checklist requires ADMIN.</p>
          )}
          {ops.data && (
            <div className="space-y-2 text-sm">
              <p>
                Production-ready (this checklist):{" "}
                <strong>{ops.data.productionReady ? "yes" : "not yet"}</strong>
              </p>
              <ul className="list-disc pl-5 text-xs text-muted-foreground">
                {Object.entries(ops.data.checks).map(([k, v]) => (
                  <li key={k}>
                    {k}: {v ? "ok" : "missing"}
                  </li>
                ))}
              </ul>
              {ops.data.blockers.length > 0 && (
                <ul className="list-disc pl-5 text-xs text-destructive">
                  {ops.data.blockers.map((b) => (
                    <li key={b}>{b}</li>
                  ))}
                </ul>
              )}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
