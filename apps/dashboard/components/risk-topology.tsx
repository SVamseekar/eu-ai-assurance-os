"use client";

import { useEffect, useRef } from "react";
import { useTheme } from "next-themes";
import type { AiSystem } from "@/lib/types";
import { normaliseDecision } from "@/lib/utils";

const RISK_COLORS: Record<string, string> = {
  high: "#b42318",
  limited: "#b54708",
  minimal: "#057a55",
  prohibited: "#7f1d1d",
};

interface RiskTopologyProps {
  systems: AiSystem[];
  filter: "all" | "high" | "limited" | "minimal";
}

export function RiskTopology({ systems, filter }: RiskTopologyProps) {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const { resolvedTheme } = useTheme();

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext("2d");
    if (!ctx) return;

    const filtered = systems.filter((s) => filter === "all" || s.riskClass === filter);
    const dark = resolvedTheme === "dark";

    ctx.clearRect(0, 0, canvas.width, canvas.height);
    ctx.fillStyle = dark ? "#111318" : "#f9fafb";
    ctx.fillRect(0, 0, canvas.width, canvas.height);

    ctx.strokeStyle = dark ? "#313846" : "#d7dce3";
    ctx.lineWidth = 1;
    for (let x = 100; x < canvas.width; x += 150) {
      ctx.beginPath();
      ctx.moveTo(x, 30);
      ctx.lineTo(x, canvas.height - 40);
      ctx.stroke();
    }
    for (let y = 75; y < canvas.height; y += 75) {
      ctx.beginPath();
      ctx.moveTo(40, y);
      ctx.lineTo(canvas.width - 30, y);
      ctx.stroke();
    }

    ctx.fillStyle = dark ? "#a7b0bf" : "#667085";
    ctx.font = "13px system-ui";
    ctx.fillText("Lower release readiness", 44, 24);
    ctx.fillText("Higher release readiness", canvas.width - 190, 24);
    ctx.save();
    ctx.translate(18, canvas.height - 70);
    ctx.rotate(-Math.PI / 2);
    ctx.fillText("Risk and data criticality", 0, 0);
    ctx.restore();

    filtered.forEach((system, index) => {
      const x = 130 + (system.evidenceCoverage / 100) * 650 + (index % 2) * 18;
      const y =
        295 -
        (system.evalScore / 100) * 230 +
        (system.riskClass === "high" ? -18 : system.riskClass === "limited" ? 10 : 24);
      const decision = normaliseDecision(system.releaseDecision);
      const radius = decision === "Blocked" ? 20 : 15;

      ctx.beginPath();
      ctx.arc(x, y, radius, 0, Math.PI * 2);
      ctx.fillStyle = RISK_COLORS[system.riskClass] ?? "#667085";
      ctx.globalAlpha = 0.9;
      ctx.fill();
      ctx.globalAlpha = 1;
      ctx.strokeStyle = dark ? "#f2f4f7" : "#ffffff";
      ctx.lineWidth = 3;
      ctx.stroke();

      ctx.fillStyle = dark ? "#f2f4f7" : "#111827";
      ctx.font = "12px system-ui";
      const label = system.name.length > 22 ? `${system.name.slice(0, 21)}…` : system.name;
      ctx.fillText(label, x + radius + 7, y + 4);
    });
  }, [systems, filter, resolvedTheme]);

  return (
    <canvas
      ref={canvasRef}
      width={900}
      height={360}
      className="block w-full h-auto rounded-lg border border-border bg-muted/20"
      aria-label="AI system risk topology"
    />
  );
}
