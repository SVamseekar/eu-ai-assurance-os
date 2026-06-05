"use client";

import { useState } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { ApiStatusPill } from "@/components/api-status-pill";
import { useSystems } from "@/hooks/use-systems";
import { useEvidenceDocuments } from "@/hooks/use-evidence-documents";
import { api } from "@/lib/api";
import { MOCK_SYSTEMS } from "@/lib/mock-data";
import type { EvidenceQueryResponse, EvidenceDocument } from "@/lib/types";
import { formatDate } from "@/lib/utils";

const EVIDENCE_TYPES = ["DPIA", "POLICY", "MODEL_CARD", "VENDOR_DOC", "CONTROL_MAP"];

const LABEL = "text-xs font-medium text-muted-foreground mb-1.5 block";
const INPUT = "w-full border border-border rounded-lg px-3 py-2 text-sm bg-background focus:outline-none focus:ring-2 focus:ring-ring transition-shadow";

function SectionBox({ title, subtitle, children, right }: {
  title: string; subtitle?: string; children: React.ReactNode; right?: React.ReactNode;
}) {
  return (
    <div className="bg-card rounded-2xl border border-border flex flex-col">
      <div className="flex items-start justify-between px-5 pt-5 pb-4 border-b border-border gap-3">
        <div>
          <p className="text-sm font-semibold">{title}</p>
          {subtitle && <p className="text-xs text-muted-foreground mt-0.5">{subtitle}</p>}
        </div>
        {right}
      </div>
      <div className="p-5 flex-1">{children}</div>
    </div>
  );
}

export default function EvidencePage() {
  const { data: systems = MOCK_SYSTEMS, isError: systemsError } = useSystems();
  const qc = useQueryClient();
  const apiOnline = !systemsError && systems !== MOCK_SYSTEMS;

  const [selectedSystemId, setSelectedSystemId] = useState<string>(systems[0]?.id ?? "");
  const [question, setQuestion] = useState(
    "Which controls block the Claims Triage AI release, and what evidence is missing?"
  );
  const [ragResponse, setRagResponse] = useState<EvidenceQueryResponse | null>(null);
  const [demoAnswer, setDemoAnswer] = useState<string | null>(null);
  const [querying, setQuerying] = useState(false);

  const [docType, setDocType] = useState("DPIA");
  const [docTitle, setDocTitle] = useState("Claims Triage Oversight SOP");
  const [docSource, setDocSource] = useState("memory://claims-oversight-sop");
  const [docContent, setDocContent] = useState(
    "Human oversight SOP requires reviewer override, claimant appeal route, owner sign-off, and monthly bias monitoring evidence before release."
  );
  const [indexing, setIndexing] = useState(false);

  const { data: documents = [] } = useEvidenceDocuments(apiOnline ? selectedSystemId : undefined);

  async function handleQuery(e: React.SyntheticEvent) {
    e.preventDefault();
    setQuerying(true);
    try {
      const res = await api.evidence.query(selectedSystemId, question);
      setRagResponse(res);
      setDemoAnswer(null);
    } catch {
      setRagResponse(null);
      setDemoAnswer(
        "API unavailable. The system is classified as high-risk. The release gate depends on human oversight evidence, eval threshold performance, and data-contract status."
      );
    } finally {
      setQuerying(false);
    }
  }

  async function handleIndex(e: React.SyntheticEvent) {
    e.preventDefault();
    setIndexing(true);
    try {
      await api.evidence.index({
        systemId: selectedSystemId,
        type: docType,
        title: docTitle,
        sourceUri: docSource,
        content: docContent,
        metadata: { source: "web-dashboard" },
      });
      qc.invalidateQueries({ queryKey: ["evidence-documents", selectedSystemId] });
    } catch (err) {
      console.error("Index failed", err);
    } finally {
      setIndexing(false);
    }
  }

  const selectedSystem = systems.find((s) => s.id === selectedSystemId) ?? systems[0];

  return (
    <div className="space-y-4">
      <div className="grid grid-cols-2 gap-4">
        <SectionBox
          title="Compliance Evidence RAG"
          subtitle="Ask against policies, DPIAs, model cards, vendor docs, and EU control mappings."
        >
          <form onSubmit={handleQuery} className="space-y-4">
            <div>
              <label className={LABEL}>System</label>
              <Select value={selectedSystemId} onValueChange={(v) => v && setSelectedSystemId(v)}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {systems.map((s) => (
                    <SelectItem key={s.id} value={s.id}>{s.name}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div>
              <label className={LABEL}>Question</label>
              <Textarea rows={5} value={question} onChange={(e) => setQuestion(e.target.value)} />
            </div>
            <Button type="submit" disabled={querying} size="sm">
              {querying ? "Querying…" : "Ask with citations"}
            </Button>
          </form>
        </SectionBox>

        <SectionBox
          title="Grounded Answer"
          subtitle="Every answer includes confidence, source clauses, and reviewer action."
        >
          {ragResponse ? (
            <div className="space-y-3">
              <p className="text-xs text-muted-foreground">{question}</p>
              <p className="text-sm font-medium">{ragResponse.answer}</p>
              {ragResponse.citations.map((c, i) => (
                <div key={i} className="border-l-2 border-primary/40 pl-3 text-xs text-muted-foreground">
                  <span className="font-medium text-foreground">{c.title} · {c.section}</span>
                  <p className="mt-0.5">{c.snippet}</p>
                </div>
              ))}
              <div className="border-l-2 border-primary/40 pl-3 text-xs text-muted-foreground">
                <span className="font-medium">Confidence: {Math.round(ragResponse.confidence * 100)}%</span>
                <p className="mt-0.5">Reviewer should inspect cited source material before release approval.</p>
              </div>
            </div>
          ) : demoAnswer ? (
            <div className="space-y-3">
              <p className="text-xs text-muted-foreground uppercase font-medium tracking-wide">Demo mode</p>
              <p className="text-sm">{demoAnswer}</p>
              <div className="border-l-2 border-primary/40 pl-3 text-xs text-muted-foreground">
                <span className="font-medium">Source: DPIA-CLM-014</span>
                <p className="mt-0.5">Reviewer override must include purpose, affected cohort, appeal route, and owner sign-off.</p>
              </div>
            </div>
          ) : (
            <p className="text-sm text-muted-foreground">Run a cited compliance query to see results here.</p>
          )}
        </SectionBox>
      </div>

      <div className="grid grid-cols-2 gap-4">
        <SectionBox
          title="Index Evidence"
          subtitle="Upload extracted text from a policy, DPIA, model card, or vendor document."
        >
          <form onSubmit={handleIndex} className="space-y-4">
            <div>
              <label className={LABEL}>Type</label>
              <Select value={docType} onValueChange={(v) => v && setDocType(v)}>
                <SelectTrigger><SelectValue /></SelectTrigger>
                <SelectContent>
                  {EVIDENCE_TYPES.map((t) => (
                    <SelectItem key={t} value={t}>{t}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div>
              <label className={LABEL}>Title</label>
              <input className={INPUT} value={docTitle} onChange={(e) => setDocTitle(e.target.value)} />
            </div>
            <div>
              <label className={LABEL}>Source URI</label>
              <input className={INPUT} value={docSource} onChange={(e) => setDocSource(e.target.value)} />
            </div>
            <div>
              <label className={LABEL}>Extracted text</label>
              <Textarea rows={4} value={docContent} onChange={(e) => setDocContent(e.target.value)} />
            </div>
            <div className="flex items-center gap-3">
              <Button type="submit" disabled={indexing || !apiOnline} size="sm">
                {indexing ? "Indexing…" : "Index document"}
              </Button>
              {!apiOnline && <p className="text-xs text-muted-foreground">Start the API to index.</p>}
            </div>
          </form>
        </SectionBox>

        <SectionBox
          title="Indexed Documents"
          subtitle={`Documents on ${selectedSystem?.name ?? "selected system"}`}
          right={<ApiStatusPill online={apiOnline} />}
        >
          {documents.length === 0 ? (
            <div className="border border-dashed border-border rounded-xl min-h-32 flex items-center justify-center text-sm text-muted-foreground">
              No indexed documents.
            </div>
          ) : (
            <div className="space-y-2">
              {(documents as EvidenceDocument[]).map((doc) => (
                <div key={doc.id} className="flex items-center justify-between rounded-lg bg-muted/30 px-3 py-2.5">
                  <div>
                    <p className="text-sm font-medium">{doc.title}</p>
                    <p className="text-xs text-muted-foreground mt-0.5">
                      {doc.type} · {doc.chunkCount} chunk{doc.chunkCount !== 1 ? "s" : ""} · {formatDate(doc.createdAt)}
                    </p>
                  </div>
                  <span className="text-xs text-muted-foreground">{doc.ingestionStatus}</span>
                </div>
              ))}
            </div>
          )}
        </SectionBox>
      </div>
    </div>
  );
}
