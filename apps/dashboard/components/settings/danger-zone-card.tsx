"use client";

import { useState } from "react";
import { useMutation, useQuery } from "@tanstack/react-query";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { api } from "@/lib/api";

/** Workspace data export and scheduled deletion. Admins only. */
export function DangerZoneCard() {
  const me = useQuery({ queryKey: ["me"], queryFn: api.me, retry: false, staleTime: 5 * 60_000 });
  const [confirmName, setConfirmName] = useState("");
  const [open, setOpen] = useState(false);

  const exportAll = useMutation({ mutationFn: api.account.exportAll });
  const remove = useMutation({
    mutationFn: () => api.account.deleteWorkspace(confirmName),
    onSuccess: async () => {
      await fetch("/api/auth/logout", { method: "POST" }).catch(() => undefined);
      window.location.assign("/?deleted=1");
    },
  });

  if (me.data?.role !== "ADMIN" || me.data.demo) return null;

  return (
    <Card>
      <CardHeader>
        <CardTitle>Export and delete</CardTitle>
        <CardDescription>
          Admins only. Download everything in this workspace, or schedule it for deletion.
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="flex flex-wrap items-center justify-between gap-2">
          <p className="text-sm">
            Export all workspace data
            <span className="block text-xs text-muted-foreground">
              A zip with one JSON file per table, including extracted evidence text and the audit ledger.
              Passwords and key hashes are left out.
            </span>
          </p>
          <Button type="button" size="sm" variant="outline" disabled={exportAll.isPending} onClick={() => exportAll.mutate()}>
            {exportAll.isPending ? "Preparing…" : "Export"}
          </Button>
        </div>
        {exportAll.isError && <p className="text-xs text-destructive">Export failed. Try again.</p>}

        <div className="space-y-2 border-t border-border pt-4">
          <p className="text-sm">
            Delete workspace
            <span className="block text-xs text-muted-foreground">
              Access ends immediately and every API key stops working. All data is erased permanently after
              30 days. Export first if you need a copy.
            </span>
          </p>
          {!open ? (
            <Button type="button" size="sm" variant="outline" onClick={() => setOpen(true)}>
              Delete workspace…
            </Button>
          ) : (
            <form
              className="space-y-2"
              onSubmit={(e) => {
                e.preventDefault();
                remove.mutate();
              }}
            >
              <label className="block text-xs">
                Type the organisation name to confirm
                <input
                  value={confirmName}
                  onChange={(e) => setConfirmName(e.target.value)}
                  autoComplete="off"
                  className="mt-1 block w-full max-w-sm rounded-lg border border-border bg-background px-3 py-2 text-sm"
                />
              </label>
              <div className="flex gap-2">
                <Button type="submit" size="sm" disabled={remove.isPending || confirmName.trim() === ""}>
                  {remove.isPending ? "Deleting…" : "Delete workspace"}
                </Button>
                <Button type="button" size="sm" variant="ghost" onClick={() => { setOpen(false); setConfirmName(""); }}>
                  Cancel
                </Button>
              </div>
              {remove.isError && (
                <p className="text-xs text-destructive">
                  That name doesn&apos;t match, or you don&apos;t have permission.
                </p>
              )}
            </form>
          )}
        </div>
      </CardContent>
    </Card>
  );
}
