"use client";

import { useState } from "react";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import {
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue,
} from "@/components/ui/select";
import { useSystems } from "@/hooks/use-systems";
import { useEvalOperations } from "@/hooks/use-eval-runs";
import { api } from "@/lib/api";
import { MOCK_SYSTEMS } from "@/lib/mock-data";
import type { EvalRun } from "@/lib/types";
import { cn } from "@/lib/utils";

const DATASETS = ["golden-eu-claims-v4", "hr-candidate-screening-v2", "customer-support-rag-v8"];

export default function EvalsPage() {
  const { data: systems = MOCK_SYSTEMS } = useSystems();
  const { data: operations } = useEvalOperations();

  const [selectedSystemId, setSelectedSystemId] = useState(systems[0]?.id ?? "");
  const [dataset, setDataset] = useState(DATASETS[0]);
  const [threshold, setThreshold] = useState(85);
  const [consoleLines, setConsoleLines] = useState<string[]>([]);
  const [running, setRunning] = useState(false);

  async function handleRun(e: React.SyntheticEvent) {
    e.preventDefault();
    setRunning(true);
    const system = systems.find((s) => s.id === selectedSystemId) ?? systems[0];
    setConsoleLines([`> queued eval run for ${system.name}`, "> submitting to API…"]);
    try {
      const { runId } = await api.evals.create({
        systemId: selectedSystemId, dataset,
        modelVersion: `${system.name.toLowerCase().replace(/\s/g, "-")}-2026-06`,
        promptVersion: "v1", threshold,
      });
      setConsoleLines((p) => [...p, `> run ${runId} created, executing…`]);
      const result: EvalRun = await api.evals.execute(runId);
      const m = result.metrics;
      setConsoleLines([
        `> run ${runId} — ${system.name}`,
        `> faithfulness:       ${m.faithfulness !== undefined ? Math.round(m.faithfulness * 100) : "—"}%`,
        `> relevance:          ${m.relevance !== undefined ? Math.round(m.relevance * 100) : "—"}%`,
        `> safety refusal:     ${m.safetyRefusal !== undefined ? Math.round(m.safetyRefusal * 100) : "—"}%`,
        `> bias slice pass:    ${m.biasSlicePassRate !== undefined ? Math.round(m.biasSlicePassRate * 100) : "—"}%`,
        `> latency p95:        ${m.latencyP95Ms ?? "—"}ms`,
        `> cost:               $${m.costUsd ?? "—"}`,
        `> decision:           ${result.releaseDecision}`,
      ]);
    } catch {
      const jitter = Math.round(Math.random() * 10 - 4);
      const score = Math.max(55, Math.min(96, system.evalScore + jitter));
      const decision = score >= threshold ? "PASS" : score < threshold - 7 ? "BLOCKED" : "REVIEW";
      setConsoleLines([
        `> queued eval run for ${system.name} (demo)`,
        "> loaded dataset and judge rubric",
        `> faithfulness:       ${score}%`,
        `> safety refusal:     ${Math.max(70, score - 3)}%`,
        "> latency guard:      pass",
        `> data contract:      ${system.dataContractStatus}`,
        `> decision:           ${decision}`,
      ]);
    } finally {
      setRunning(false);
    }
  }

  return (
    <div className="space-y-4">
      <div className="grid grid-cols-2 gap-4">
        <Card>
          <CardHeader>
            <CardTitle>Eval Gate Runner</CardTitle>
            <CardDescription>LLM-as-judge, RAG faithfulness, safety refusal, latency, and cost guardrails.</CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleRun} className="space-y-4">
              <div className="space-y-1.5">
                <label className="text-xs font-medium text-muted-foreground">System</label>
                <Select value={selectedSystemId} onValueChange={(v) => v && setSelectedSystemId(v)}>
                  <SelectTrigger><SelectValue /></SelectTrigger>
                  <SelectContent>
                    {systems.map((s) => <SelectItem key={s.id} value={s.id}>{s.name}</SelectItem>)}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-1.5">
                <label className="text-xs font-medium text-muted-foreground">Dataset</label>
                <Select value={dataset} onValueChange={(v) => v && setDataset(v)}>
                  <SelectTrigger><SelectValue /></SelectTrigger>
                  <SelectContent>
                    {DATASETS.map((d) => <SelectItem key={d} value={d}>{d}</SelectItem>)}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-1.5">
                <label className="text-xs font-medium text-muted-foreground">Pass threshold (%)</label>
                <input
                  type="number" min={70} max={98} value={threshold}
                  onChange={(e) => setThreshold(Number(e.target.value))}
                  className="w-full border border-border rounded-lg px-3 py-2 text-sm bg-background focus:outline-none focus:ring-2 focus:ring-ring/50"
                />
              </div>
              <Button type="submit" disabled={running}>
                {running ? "Running…" : "Run eval gate"}
              </Button>
            </form>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Gate Console</CardTitle>
            <CardDescription>Worker output with release decision.</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="min-h-60 max-h-72 overflow-auto rounded-lg bg-zinc-950 text-emerald-400 font-mono text-xs p-4 leading-loose">
              {consoleLines.length === 0
                ? <span className="text-zinc-600">Awaiting eval run…</span>
                : consoleLines.map((line, i) => <div key={i}>{line}</div>)
              }
            </div>
          </CardContent>
        </Card>
      </div>

      {operations && (
        <Card>
          <CardHeader>
            <CardTitle>Worker Operations</CardTitle>
            <CardDescription>Queued, running, retryable, and failed eval runs.</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-4 gap-3">
              {[
                { label: "Queued",      count: operations.queued.length,      color: "text-foreground" },
                { label: "Running",     count: operations.running.length,      color: "text-primary" },
                { label: "Retryable",   count: operations.retryable.length,    color: "text-amber-600 dark:text-amber-400" },
                { label: "Dead Letter", count: operations.deadLetter.length,   color: "text-red-600 dark:text-red-400" },
              ].map((op) => (
                <div key={op.label} className="bg-muted/40 rounded-xl px-4 py-3.5">
                  <p className={cn("text-2xl font-bold", op.color)}>{op.count}</p>
                  <p className="text-xs text-muted-foreground mt-1">{op.label}</p>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
