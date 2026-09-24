"use client";

import { useState } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { useDashboard } from "@/context/dashboard-context";
import { useProposalActions, useProposals } from "@/hooks/use-proposals";
import type { MappingProposal } from "@/lib/types";

const SITTING_DOCUMENTS = [
  {
    title: "Model card",
    text: "Model card for a credit-scoring assistant. The system is a high-risk AI system listed in Annex III.",
  },
  {
    title: "Approval note",
    text: "Approval note: natural persons are informed that they interact with an AI system before the release.",
  },
  {
    title: "Dataset note",
    text: "Dataset note. Training rows are personal data and shall be processed lawfully, fairly and in a transparent manner.",
  },
  {
    title: "Eval summary",
    text: "Eval summary for security of network and information systems supporting the business processes of financial entities. Score 91.",
  },
  {
    title: "Retention policy",
    text: "Retention policy. Personal data shall be processed lawfully and kept only for the stated retention window.",
  },
];

export default function ProposalsPage() {
  const { allSystems } = useDashboard();
  const [picked, setPicked] = useState("");
  const systemId = picked || allSystems[0]?.id || "";
  const query = useProposals(systemId || null);
  const actions = useProposalActions(systemId);
  const [relation, setRelation] = useState("supports");
  const [provisionKey, setProvisionKey] = useState("02016R0679-20160504#5:1:");
  const [excerpt, setExcerpt] = useState("");
  const [corpusVersion, setCorpusVersion] = useState("");
  const [actionError, setActionError] = useState("");

  const items = query.data?.items ?? [];
  const current = query.data?.currentCorpusVersion ?? "";

  async function onCreate(event: React.FormEvent) {
    event.preventDefault();
    setActionError("");
    try {
      await actions.create.mutateAsync({
        relation,
        provisionKey: relation === "abstain" ? undefined : provisionKey,
        excerpt,
        corpusVersion: corpusVersion.trim() || undefined,
      });
      setExcerpt("");
      setCorpusVersion("");
    } catch (error) {
      setActionError(error instanceof Error ? error.message : "Could not file the proposal");
    }
  }

  async function onDecide(proposal: MappingProposal, decision: "accept" | "reject") {
    setActionError("");
    try {
      if (decision === "accept") {
        await actions.accept.mutateAsync(proposal.id);
      } else {
        await actions.reject.mutateAsync(proposal.id);
      }
    } catch (error) {
      setActionError(error instanceof Error ? error.message : "Decision failed");
    }
  }

  return (
    <div className="space-y-4">
      <Card>
        <CardHeader>
          <CardTitle>Queue</CardTitle>
          <CardDescription>
            Accept and reject are limited to a compliance officer or an admin. An engineering lead receives 403.
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-3 text-sm">
          <label className="block text-xs text-muted-foreground">
            System
            <select
              className="mt-1 w-full rounded-lg border border-border bg-background px-2 py-1.5 text-sm"
              value={systemId}
              onChange={(event) => setPicked(event.target.value)}
            >
              {allSystems.map((system) => (
                <option key={system.id} value={system.id}>
                  {system.name}
                </option>
              ))}
            </select>
          </label>
          <Button
            size="sm"
            variant="outline"
            disabled={!systemId || actions.mapDocuments.isPending}
            onClick={() => {
              setActionError("");
              actions.mapDocuments.mutate(SITTING_DOCUMENTS, {
                onError: (error) =>
                  setActionError(error instanceof Error ? error.message : "Could not map the documents"),
              });
            }}
          >
            Map the five documents
          </Button>
          {current && (
            <p className="text-xs text-muted-foreground">
              Current corpus_version <span className="font-mono break-all">{current}</span>
            </p>
          )}
          {query.isLoading && <p className="text-muted-foreground">Loading the queue.</p>}
          {query.isError && (
            <p className="text-destructive">Queue could not be loaded. {String(query.error)}</p>
          )}
          {query.isSuccess && items.length === 0 && (
            <div className="rounded-lg border border-dashed border-border px-3 py-6 text-center">
              <p className="font-medium">Queue is empty</p>
              <p className="mt-1 text-xs text-muted-foreground">No proposed link is waiting on this system.</p>
            </div>
          )}
          {items.map((proposal) => (
            <ProposalRow
              key={proposal.id}
              proposal={proposal}
              busy={actions.accept.isPending || actions.reject.isPending}
              onDecide={onDecide}
            />
          ))}
          {actionError && <p className="text-destructive text-xs">{actionError}</p>}
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>File a proposal</CardTitle>
          <CardDescription>
            Use abstain when no link is proposed. Paste a different corpus hash to show a mismatch.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form className="space-y-3 text-sm" onSubmit={onCreate}>
            <label className="block text-xs text-muted-foreground">
              Relation
              <select
                className="mt-1 w-full rounded-lg border border-border bg-background px-2 py-1.5 text-sm"
                value={relation}
                onChange={(event) => setRelation(event.target.value)}
              >
                <option value="supports">supports</option>
                <option value="overlaps">overlaps</option>
                <option value="tension_candidate">tension_candidate</option>
                <option value="abstain">abstain</option>
              </select>
            </label>
            {relation !== "abstain" && (
              <label className="block text-xs text-muted-foreground">
                Provision key
                <input
                  className="mt-1 w-full rounded-lg border border-border bg-background px-2 py-1.5 font-mono text-xs"
                  value={provisionKey}
                  onChange={(event) => setProvisionKey(event.target.value)}
                />
              </label>
            )}
            <label className="block text-xs text-muted-foreground">
              Excerpt
              <textarea
                className="mt-1 w-full rounded-lg border border-border bg-background px-2 py-1.5 text-sm"
                rows={3}
                value={excerpt}
                onChange={(event) => setExcerpt(event.target.value)}
              />
            </label>
            <label className="block text-xs text-muted-foreground">
              Corpus version override
              <input
                className="mt-1 w-full rounded-lg border border-border bg-background px-2 py-1.5 font-mono text-xs"
                value={corpusVersion}
                placeholder="Leave empty to pin the current corpus"
                onChange={(event) => setCorpusVersion(event.target.value)}
              />
            </label>
            <Button type="submit" size="sm" disabled={!systemId || actions.create.isPending}>
              File proposal
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}

function ProposalRow({
  proposal,
  busy,
  onDecide,
}: {
  proposal: MappingProposal;
  busy: boolean;
  onDecide: (proposal: MappingProposal, decision: "accept" | "reject") => void;
}) {
  const pending = proposal.status === "PENDING";
  const abstain = proposal.displayState === "abstain";
  const mismatch = proposal.displayState === "corpus_mismatch";
  return (
    <div className="rounded-lg border border-border p-3 space-y-2">
      <div className="flex flex-wrap items-center gap-2 text-xs">
        <span className="font-medium">{proposal.relation}</span>
        <span className="rounded-full border border-border px-2 py-0.5">{proposal.displayState}</span>
        <span className="text-muted-foreground">{proposal.status}</span>
      </div>
      {abstain && (
        <p>No link proposed. Attach one by hand or dismiss.</p>
      )}
      {mismatch && (
        <div className="space-y-1 text-xs">
          <p>Corpus versions differ. Re-retrieve before accept.</p>
          <p className="font-mono break-all">Pinned {proposal.pinnedCorpusVersion}</p>
          <p className="font-mono break-all">Current {proposal.currentCorpusVersion}</p>
        </div>
      )}
      {proposal.provisionKey && (
        <p className="font-mono text-[11px]">
          {proposal.provisionKey}
          {proposal.forceStatus ? ` · ${proposal.forceStatus}` : ""}
          {proposal.forceFrom ? ` · ${proposal.forceFrom}` : ""}
        </p>
      )}
      {proposal.excerpt && <p className="text-xs text-muted-foreground">{proposal.excerpt}</p>}
      {pending && (
        <div className="flex gap-2">
          <Button
            size="sm"
            disabled={busy || abstain || mismatch}
            onClick={() => onDecide(proposal, "accept")}
          >
            Accept
          </Button>
          <Button size="sm" variant="outline" disabled={busy} onClick={() => onDecide(proposal, "reject")}>
            {abstain ? "Dismiss" : "Reject"}
          </Button>
        </div>
      )}
    </div>
  );
}
