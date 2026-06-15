import { ImageResponse } from "next/og";

import { siteConfig } from "@/lib/site-config";

export const size = { width: 1200, height: 630 };
export const contentType = "image/png";

export default function OpengraphImage() {
  return new ImageResponse(
    (
      <div
        style={{
          width: "100%",
          height: "100%",
          display: "flex",
          flexDirection: "column",
          justifyContent: "center",
          padding: "80px",
          backgroundColor: "#f5f5f7",
          color: "#1a1a2e",
          fontSize: 64,
          fontWeight: 600,
        }}
      >
        <div style={{ fontSize: 28, color: "#4338ca", fontWeight: 600, marginBottom: 24 }}>
          EU AI Act Release Governance
        </div>
        <div>{siteConfig.name}</div>
        <div style={{ fontSize: 28, color: "#6b7280", fontWeight: 400, marginTop: 24 }}>
          {siteConfig.description}
        </div>
      </div>
    ),
    { ...size }
  );
}
