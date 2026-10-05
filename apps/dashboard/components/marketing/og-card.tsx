import { ImageResponse } from "next/og";

import { siteConfig } from "@/lib/site-config";

export const OG_SIZE = { width: 1200, height: 630 };

function Pill({ label, color, background }: { label: string; color: string; background: string }) {
  return (
    <div
      style={{
        display: "flex",
        alignItems: "center",
        gap: 12,
        padding: "14px 26px",
        borderRadius: 999,
        background,
        color,
        fontSize: 30,
        fontWeight: 700,
        letterSpacing: 2,
      }}
    >
      <div style={{ width: 16, height: 16, borderRadius: 999, background: color }} />
      {label}
    </div>
  );
}

/** Social card: product name, tagline, and a gate turning BLOCKED into PASS. */
export function renderOgCard() {
  return new ImageResponse(
    (
      <div
        style={{
          width: "100%",
          height: "100%",
          display: "flex",
          flexDirection: "column",
          justifyContent: "space-between",
          padding: "72px 80px",
          background: "radial-gradient(circle at 80% 0%, #24379a 0%, #0b1440 40%, #050a1c 75%)",
          color: "#ffffff",
        }}
      >
        <div style={{ display: "flex", alignItems: "center", gap: 18, fontSize: 34, fontWeight: 700 }}>
          <div
            style={{
              width: 48,
              height: 48,
              borderRadius: 12,
              background: "#3b5bff",
              display: "flex",
            }}
          />
          {siteConfig.shortName}
        </div>
        <div style={{ display: "flex", flexDirection: "column", gap: 20 }}>
          <div style={{ fontSize: 66, fontWeight: 800, lineHeight: 1.1, maxWidth: 960 }}>{siteConfig.tagline}</div>
          <div style={{ fontSize: 28, color: "#b6c2ff" }}>Mapped to the EU AI Act. Evidence and readiness, not legal advice.</div>
        </div>
        <div style={{ display: "flex", alignItems: "center", gap: 24 }}>
          <Pill label="BLOCKED" color="#ff6b6b" background="rgba(255,107,107,0.14)" />
          <div style={{ fontSize: 40, color: "#7f93ff" }}>→</div>
          <Pill label="PASS" color="#3ddc97" background="rgba(61,220,151,0.14)" />
          <div style={{ marginLeft: 24, fontSize: 24, color: "#8f9bd6" }}>Release gate in CI · signed evidence pack</div>
        </div>
      </div>
    ),
    { ...OG_SIZE },
  );
}
