import { NextRequest, NextResponse } from "next/server";
import { clientIpHeaders } from "@/lib/client-ip";
import { fetchUpstream, serviceUnavailable } from "@/lib/upstream";

const API_BASE = process.env.ASSURANCE_API_BASE_URL ?? "http://localhost:8080";

/**
 * Public entry point for CI bots: forwards only the API key and query string, with no session
 * cookie involved, and returns the release-gate decision unchanged.
 */
export async function GET(request: NextRequest) {
  const apiKey = request.headers.get("X-Api-Key");
  if (!apiKey) {
    return NextResponse.json({ error: "Missing X-Api-Key" }, { status: 401 });
  }
  const upstream = await fetchUpstream(`${API_BASE}/api/v1/ci/release-gate${request.nextUrl.search}`, {
    method: "GET",
    headers: { "X-Api-Key": apiKey, ...clientIpHeaders(request) },
  });
  if (!upstream) return serviceUnavailable();
  return new NextResponse(await upstream.text(), {
    status: upstream.status,
    headers: { "Content-Type": upstream.headers.get("Content-Type") ?? "application/json" },
  });
}
