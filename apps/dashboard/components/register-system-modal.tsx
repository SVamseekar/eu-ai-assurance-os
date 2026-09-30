"use client";

import { useState } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { Modal } from "./ui/modal";
import { Button } from "./ui/button";
import { Textarea } from "./ui/textarea";
import { RiskBadge } from "./risk-badge";
import { SectorPackBadge } from "./sector-pack-badge";
import { CheckCircle2, AlertCircle, Sparkles } from "lucide-react";
import { api, ApiError } from "@/lib/api";
import { suggestRiskClass, toApiRiskClass } from "@/lib/risk-class";
import { SECTOR_PACK_OPTIONS, resolveSectorPackId } from "@/lib/sector-packs";

interface RegisterSystemModalProps {
  isOpen: boolean;
  onClose: () => void;
  onRegistered?: (id: string) => void;
}

const QUESTIONNAIRE = [
  {
    id: "q_biometrics",
    label: "Biometric & Critical Infrastructure",
    text: "Is the system used for real-time remote biometric identification, or for control/safety of critical physical infrastructure (e.g., power grid)?"
  },
  {
    id: "q_essential",
    label: "Essential Services & Access Control",
    text: "Does the system evaluate creditworthiness, compute insurance eligibility, prioritize claims routing, or routing access to essential welfare benefits?"
  },
  {
    id: "q_hr",
    label: "Employment & HR Pipelines",
    text: "Is the system used for recruitment, screening resumes, shortlisting job applicants, or evaluating worker performance/promotions?"
  },
  {
    id: "q_interaction",
    label: "Customer Interaction & Natural Persons",
    text: "Does the system directly interact with natural persons, or generate content that could be mistaken as human-written (e.g., chat copilots, generation tools)?"
  }
];

export function RegisterSystemModal({ isOpen, onClose, onRegistered }: RegisterSystemModalProps) {
  const qc = useQueryClient();
  const [step, setStep] = useState(1);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [name, setName] = useState("");
  const [owner, setOwner] = useState("");
  const [sector, setSector] = useState("insurance");
  const [purpose, setPurpose] = useState("");

  const [answers, setAnswers] = useState<Record<string, boolean>>({
    q_biometrics: false,
    q_essential: false,
    q_hr: false,
    q_interaction: false
  });

  function handleToggleAnswer(id: string, val: boolean) {
    setAnswers((p) => ({ ...p, [id]: val }));
  }

  function handleSectorChange(next: string) {
    setSector(next);
    // Align questionnaire defaults with sector pack hints (client-side only)
    if (next === "insurance" || next === "finance") {
      setAnswers((p) => ({ ...p, q_essential: true, q_hr: false }));
    } else if (next === "hr") {
      setAnswers((p) => ({ ...p, q_hr: true, q_essential: false }));
    }
  }

  const riskClass = suggestRiskClass({
    q_biometrics: answers.q_biometrics,
    q_essential: answers.q_essential,
    q_hr: answers.q_hr,
    q_interaction: answers.q_interaction,
  });
  let riskBasis = "No specific EU AI Act obligations suggested by these answers. Article 4 AI literacy and general law still apply.";

  if (riskClass === "high") {
    riskBasis = "Art. 6(2) Annex III — System falls under high-risk critical infrastructure, essential services, or HR hiring evaluation categories.";
  } else if (riskClass === "limited") {
    riskBasis = "Article 50 transparency duties may apply — the system interacts with natural persons or generates content.";
  }

  const packId = resolveSectorPackId(sector);
  const packObligations =
    packId === "insurance"
      ? [
          "Insurance pack: claims fairness testing (INS_CLAIMS_FAIRNESS)",
          "Insurance pack: human review of adverse claim decisions",
          "Insurance pack: claims model card documentation",
        ]
      : packId === "hr"
        ? [
            "HR pack: hiring/ranking transparency",
            "HR pack: employment human oversight + candidate notice",
          ]
        : packId === "finance"
          ? [
              "Finance pack: elevated KYC/fraud logging intensity",
              "Finance pack: human review of fraud/KYC flags",
            ]
          : [];

  const obligations = riskClass === "high"
    ? [
        "Index Technical Documentation (Art. 11) & Model Cards",
        "Establish Human Oversight SOP (Art. 14) with manual override route",
        "Keep automatic event logs (Art. 12)",
        "Run continuous evaluation runs (faithfulness, bias) and pass 85% threshold gate",
        "Monitor data-contract drift on schema inputs",
        ...packObligations,
      ]
    : riskClass === "limited"
    ? [
        "Disclose AI interaction and label AI-generated content (Article 50)",
        "Identify content generation origins explicitly",
        ...packObligations,
      ]
    : [
        "Optional compliance with voluntary industry code of conduct models",
        "Maintain baseline privacy data policies"
      ];

  async function handleSubmit() {
    setSaving(true);
    setError(null);
    try {
      const apiRisk = toApiRiskClass(riskClass);
      const created = await api.systems.create({
        name: name.trim(),
        owner: owner.trim(),
        purpose: purpose.trim(),
        riskClass: apiRisk,
        riskBasis,
        deploymentRegion: "EU",
        sector: sector || undefined,
      });
      await api.systems.classify(created.id, {
        riskClass: apiRisk,
        basis: riskBasis,
        humanOversightRequired: riskClass === "high",
        sector: sector || undefined,
      });
      await qc.invalidateQueries({ queryKey: ["systems"] });
      onRegistered?.(created.id);
      setStep(1);
      onClose();
    } catch (e) {
      setError(e instanceof ApiError ? e.message : "Could not register the system. Try again.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Register AI System"
      description={`Step ${step} of 3 — ${
        step === 1 ? "Identity & Purpose" : step === 2 ? "Risk Classification Questionnaire" : "Obligation Rollup"
      }`}
    >
      <div className="space-y-5">
        {step === 1 && (
          <div className="space-y-4">
            <div className="space-y-1.5">
              <label htmlFor="system-name" className="text-xs font-semibold text-muted-foreground uppercase tracking-wide">System Name</label>
              <input
                id="system-name"
                className="w-full border border-border rounded-lg px-3 py-2 text-sm bg-background focus:outline-none focus:ring-2 focus:ring-ring/50"
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="e.g. Underwriting Copilot"
              />
            </div>
            <div className="space-y-1.5">
              <label htmlFor="system-owner" className="text-xs font-semibold text-muted-foreground uppercase tracking-wide">Owner</label>
              <input
                id="system-owner"
                className="w-full border border-border rounded-lg px-3 py-2 text-sm bg-background focus:outline-none focus:ring-2 focus:ring-ring/50"
                value={owner}
                onChange={(e) => setOwner(e.target.value)}
                placeholder="e.g. Credit Risk Operations"
              />
            </div>
            <div className="space-y-1.5">
              <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wide">
                Sector / vertical pack
              </label>
              <select
                className="w-full border border-border rounded-lg px-3 py-2 text-sm bg-background focus:outline-none focus:ring-2 focus:ring-ring/50"
                value={sector}
                onChange={(e) => handleSectorChange(e.target.value)}
              >
                {SECTOR_PACK_OPTIONS.map((opt) => (
                  <option key={opt.value} value={opt.value}>
                    {opt.label}
                    {opt.packId ? " (pack enabled)" : ""}
                  </option>
                ))}
              </select>
              <p className="text-[10px] text-muted-foreground leading-normal">
                Enabled packs (insurance, HR, finance) attach extra controls via SPI.
                Not all industries are integrated; no live proprietary vendor connectors.
              </p>
              {sector && (
                <div className="pt-1">
                  <SectorPackBadge sector={sector} />
                </div>
              )}
            </div>
            <div className="space-y-1.5">
              <label htmlFor="system-purpose" className="text-xs font-semibold text-muted-foreground uppercase tracking-wide">Purpose</label>
              <Textarea
                id="system-purpose"
                rows={4}
                value={purpose}
                onChange={(e) => setPurpose(e.target.value)}
                placeholder="Describe what choices this AI model informs or automates, and who is affected..."
              />
            </div>
            <div className="flex justify-end pt-3">
              <Button disabled={!name || !owner} onClick={() => setStep(2)}>
                Next: Risk Analysis
              </Button>
            </div>
          </div>
        )}

        {step === 2 && (
          <div className="space-y-4">
            <p className="text-xs text-muted-foreground leading-normal">
              Answer these questions based on the EU AI Act classification standards (Articles 6 and 50).
            </p>
            <div className="space-y-3.5 divide-y divide-border/60">
              {QUESTIONNAIRE.map((q) => (
                <div key={q.id} className="pt-3.5 first:pt-0 flex items-start justify-between gap-6">
                  <div className="flex-1">
                    <p className="text-[11px] font-semibold text-foreground leading-normal">{q.label}</p>
                    <p className="text-[10px] text-muted-foreground leading-normal mt-0.5">{q.text}</p>
                  </div>
                  <div className="flex bg-muted/60 p-0.5 rounded-lg border border-border shrink-0 select-none">
                    <button
                      onClick={() => handleToggleAnswer(q.id, true)}
                      className={`text-[10px] font-semibold px-2.5 py-1 rounded-md transition-all cursor-pointer ${
                        answers[q.id] ? "bg-card text-foreground shadow-xs" : "text-muted-foreground hover:text-foreground"
                      }`}
                    >
                      Yes
                    </button>
                    <button
                      onClick={() => handleToggleAnswer(q.id, false)}
                      className={`text-[10px] font-semibold px-2.5 py-1 rounded-md transition-all cursor-pointer ${
                        !answers[q.id] ? "bg-card text-foreground shadow-xs" : "text-muted-foreground hover:text-foreground"
                      }`}
                    >
                      No
                    </button>
                  </div>
                </div>
              ))}
            </div>
            <div className="flex justify-between pt-4 border-t border-border">
              <Button variant="outline" onClick={() => setStep(1)}>
                Back
              </Button>
              <Button onClick={() => setStep(3)}>
                Next: Obligations Review
              </Button>
            </div>
          </div>
        )}

        {step === 3 && (
          <div className="space-y-4">
            {/* Risk class result banner */}
            <div className="bg-muted/40 border border-border p-4 rounded-xl flex items-center gap-3.5">
              <div className="w-10 h-10 rounded-lg bg-primary/10 grid place-items-center shrink-0">
                <Sparkles className="w-5 h-5 text-primary" />
              </div>
              <div>
                <p className="text-[10px] uppercase font-bold text-muted-foreground tracking-wider leading-none mb-1.5">Calculated Tier</p>
                <div className="flex items-center gap-2 flex-wrap">
                  <RiskBadge risk={riskClass} />
                  <SectorPackBadge sector={sector} />
                  <span className="text-xs text-muted-foreground">Release starts as: </span>
                  <span className={`text-xs font-semibold uppercase ${
                    riskClass === "high" ? "text-red-500" : riskClass === "limited" ? "text-amber-500" : "text-emerald-500"
                  }`}>
                    {riskClass === "high" ? "Blocked" : "Approved"}
                  </span>
                </div>
              </div>
            </div>

            {/* Regulatory obligation Checklist */}
            <div className="space-y-2">
              <h4 className="text-xs font-semibold text-foreground uppercase tracking-wider flex items-center gap-1.5">
                <CheckCircle2 className="w-3.5 h-3.5 text-emerald-500" />
                Required Governance Checklist
              </h4>
              <div className="space-y-1.5 border border-border bg-card rounded-xl p-3">
                {obligations.map((ob, i) => (
                  <div key={i} className="flex items-start gap-2 text-[11px] leading-relaxed">
                    <span className="w-1.5 h-1.5 rounded-full bg-primary shrink-0 mt-1.5" />
                    <span className="text-muted-foreground">{ob}</span>
                  </div>
                ))}
              </div>
              <p className="text-[10px] text-muted-foreground">Suggestion only, not legal advice. You confirm the class; the basis is recorded in the audit ledger.</p>
            </div>

            {/* Warning info */}
            {riskClass === "high" && (
              <div className="bg-red-50/50 dark:bg-red-950/20 border border-red-100 dark:border-red-950/50 p-3 rounded-lg flex items-start gap-2.5">
                <AlertCircle className="w-4 h-4 text-red-500 shrink-0 mt-0.5" />
                <p className="text-[10px] text-muted-foreground leading-normal">
                  High-risk systems are blocked by default. You must index compliance documents and pass judge evaluations before releasing.
                </p>
              </div>
            )}

            {error && <p role="alert" className="text-xs text-destructive">{error}</p>}
            <div className="flex justify-between pt-4 border-t border-border">
              <Button variant="outline" onClick={() => setStep(2)}>
                Back
              </Button>
              <Button
                onClick={handleSubmit}
                disabled={saving || !name.trim() || !owner.trim() || !purpose.trim()}
              >
                {saving ? "Registering…" : "Register system"}
              </Button>
            </div>
          </div>
        )}
      </div>
    </Modal>
  );
}
