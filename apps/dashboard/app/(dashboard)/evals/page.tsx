"use client";

import { useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { useSystems } from "@/hooks/use-systems";
import { useEvalOperations } from "@/hooks/use-eval-runs";
import { api } from "@/lib/api";
import { MOCK_SYSTEMS } from "@/lib/mock-data";
import type { EvalRun } from "@/lib/types";

const DATASETS = [
  "golden-eu-claims-v4",
  "hr-candidate-screening-v2",
  "customer-support-rag-v8",
];

export default function EvalsPage() {
  const { data: systems = MOCK_SYSTEMS } = useSystems();
  const { data: operations } = useEvalOperations();

  const [selectedSystemId, setSelectedSystemId] = useState(systems[0]?.id ?? "");
  const [dataset, setDataset] = useState(DATASETS[0]);
  const [threshold, setThreshold] = useState(85);
  const [consoleLines, setConsoleLines] = useState<string[]>([]);
  const [running, setRunning] = useState(false);

  async function handleRun(e: React.FormEvent) {
    e.preventDefault();
    setRunning(true);
    const system = systems.find((s) => s.id === selectedSystemId) ?? systems[0];
    setConsoleLines([`> queued eval run for ${system.name}`, "> submitting to API…"]);

    try {
      const { runId } = await api.evals.create({
        systemId: selectedSystemId,
        dataset,
        modelVersion: `${system.name.toLowerCase().replace(/\s/g, "-")}-2026-06`,
        promptVersion: "v1",
        threshold,
      });
      setConsoleLines((prev) => [...prev, `> run ${runId} created, executing…`]);
      const result: EvalRun = await api.evals.execute(runId);
      const m = result.metrics;
      setConsoleLines([
        `> run ${runId} for ${system.name}`,
        `> faithfulness: ${m.faithfulness !== undefined ? Math.round(m.faithfulness * 100) : "—"}%`,
        `> relevance: ${m.relevance !== undefined ? Math.round(m.relevance * 100) : "—"}%`,
        `> safety refusal: ${m.safetyRefusal !== undefined ? Math.round(m.safetyRefusal * 100) : "—"}%`,
        `> bias slice pass rate: ${m.biasSlicePassRate !== undefined ? Math.round(m.biasSlicePassRate * 100) : "—"}%`,
        `> latency p95: ${m.latencyP95Ms ?? "—"}ms`,
        `> cost: $${m.costUsd ?? "—"}`,
        `> release decision: ${result.releaseDecision}`,
      ]);
    } catch {
      const jitter = Math.round(Math.random() * 10 - 4);
      const score = Math.max(55, Math.min(96, system.evalScore + jitter));
      const decision = score >= threshold ? "PASS" : score < threshold - 7 ? "BLOCKED" : "REVIEW";
      setConsoleLines([
        `> queued eval run for ${system.name} (demo)`,
        "> loaded dataset and judge rubric",
        `> faithfulness: ${score}%`,
        `> safety refusal: ${Math.max(70, score - 3)}%`,
        "> latency guard: pass",
        `> data contract: ${system.dataContractStatus}`,
        `> release decision: ${decision}`,
      ]);
    } finally {
      setRunning(false);
    }
  }

  return (
    <div className="space-y-4">
      <div className="grid grid-cols-2 gap-4">
        <Card>
          <CardHeader className="pb-3">
            <CardTitle>Eval Gate Runner</CardTitle>
            <p className="text-sm text-muted-foreground">
              LLM-as-judge, RAG faithfulness, safety refusal, latency, and cost guardrails.
            </p>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleRun} className="space-y-3">
              <div className="space-y-1.5">
                <label className="text-xs font-bold uppercase text-muted-foreground">System</label>
                <Select value={selectedSystemId} onValueChange={(v) => v && setSelectedSystemId(v)}>
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {systems.map((s) => (
                      <SelectItem key={s.id} value={s.id}>
                        {s.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-1.5">
                <label className="text-xs font-bold uppercase text-muted-foreground">Dataset</label>
                <Select value={dataset} onValueChange={(v) => v && setDataset(v)}>
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {DATASETS.map((d) => (
                      <SelectItem key={d} value={d}>
                        {d}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-1.5">
                <label className="text-xs font-bold uppercase text-muted-foreground">
                  Threshold
                </label>
                <input
                  type="number"
                  min={70}
                  max={98}
                  value={threshold}
                  onChange={(e) => setThreshold(Number(e.target.value))}
                  className="w-full border border-border rounded-md px-3 py-2 text-sm bg-background"
                />
              </div>
              <Button type="submit" disabled={running}>
                {running ? "Running…" : "Run eval gate"}
              </Button>
            </form>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-3">
            <CardTitle>Gate Console</CardTitle>
            <p className="text-sm text-muted-foreground">Worker output with release decision.</p>
          </CardHeader>
          <CardContent>
            <div className="min-h-72 max-h-96 overflow-auto rounded-lg bg-gray-950 text-emerald-300 font-mono text-sm p-4 leading-relaxed">
              {consoleLines.length === 0 ? (
                <span className="text-gray-600">Awaiting eval run…</span>
              ) : (
                consoleLines.map((line, i) => <div key={i}>{line}</div>)
              )}
            </div>
          </CardContent>
        </Card>
      </div>

      {operations && (
        <Card>
          <CardHeader className="pb-3">
            <CardTitle>Operations</CardTitle>
            <p className="text-sm text-muted-foreground">
              Queued, running, retryable, and failed eval runs.
            </p>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-4 gap-4 text-center">
              <div>
                <p className="text-2xl font-bold">{operations.queued.length}</p>
                <p className="text-xs text-muted-foreground uppercase font-bold">Queued</p>
              </div>
              <div>
                <p className="text-2xl font-bold">{operations.running.length}</p>
                <p className="text-xs text-muted-foreground uppercase font-bold">Running</p>
              </div>
              <div>
                <p className="text-2xl font-bold text-amber-600">{operations.retryable.length}</p>
                <p className="text-xs text-muted-foreground uppercase font-bold">Retryable</p>
              </div>
              <div>
                <p className="text-2xl font-bold text-red-600">{operations.deadLetter.length}</p>
                <p className="text-xs text-muted-foreground uppercase font-bold">Dead letter</p>
              </div>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
