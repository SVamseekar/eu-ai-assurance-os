"use client";

import Link from "next/link";
import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { api } from "@/lib/api";
import { DPA_VERSION } from "@/lib/legal";

/** Settings → Legal: an admin accepts the published DPA for the organisation; everyone sees the status. */
export function LegalCard() {
  const queryClient = useQueryClient();
  const me = useQuery({ queryKey: ["me"], queryFn: api.me, retry: false, staleTime: 5 * 60_000 });
  const dpa = useQuery({ queryKey: ["dpa-acceptance"], queryFn: api.account.dpaAcceptance, retry: false });
  const [agreed, setAgreed] = useState(false);
  const accept = useMutation({
    mutationFn: () => api.account.acceptDpa(DPA_VERSION),
    onSuccess: (data) => queryClient.setQueryData(["dpa-acceptance"], data),
  });

  if (me.data?.demo) return null;
  const accepted = dpa.data?.acceptedAt ? dpa.data : null;
  const current = accepted?.version === DPA_VERSION;
  const isAdmin = me.data?.role === "ADMIN";

  return (
    <Card id="legal">
      <CardHeader>
        <CardTitle>Legal</CardTitle>
        <CardDescription>
          The <Link href="/dpa" className="underline-offset-4 hover:underline">Data Processing Agreement</Link> covers
          the personal data in this workspace.
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-3 text-sm">
        {accepted ? (
          <p>
            DPA version <code>{accepted.version}</code> accepted on{" "}
            {new Date(accepted.acceptedAt as string).toLocaleDateString("en-GB", { day: "numeric", month: "long", year: "numeric" })}.
          </p>
        ) : (
          <p className="text-muted-foreground">The DPA has not been accepted for this workspace yet.</p>
        )}
        {!current && isAdmin ? (
          <div className="space-y-3 rounded-lg border border-border p-3">
            <label className="flex items-start gap-2">
              <input type="checkbox" className="mt-0.5" checked={agreed} onChange={(e) => setAgreed(e.target.checked)} />
              <span>
                I accept the DPA (version <code>{DPA_VERSION}</code>) on behalf of my organisation.
              </span>
            </label>
            <Button size="sm" disabled={!agreed || accept.isPending} onClick={() => accept.mutate()}>
              {accept.isPending ? "Saving…" : "Accept DPA"}
            </Button>
            {accept.isError && <p className="text-xs text-destructive">Could not record the acceptance. Try again.</p>}
          </div>
        ) : null}
        {!current && !isAdmin ? (
          <p className="text-xs text-muted-foreground">An admin of this workspace can accept the DPA.</p>
        ) : null}
      </CardContent>
    </Card>
  );
}
