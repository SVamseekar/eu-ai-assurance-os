import { NextRequest, NextResponse } from "next/server";
import {
  clearSessionCookies,
  readAccessToken,
  readRefreshToken,
  refreshAccessToken,
  setSessionCookies,
} from "@/lib/session";

const API_BASE = process.env.ASSURANCE_API_BASE_URL ?? "http://localhost:8080";

async function forward(
  request: NextRequest,
  path: string[],
  accessToken: string,
): Promise<Response> {
  const targetUrl = `${API_BASE}/api/v1/${path.join("/")}${request.nextUrl.search}`;
  const body = ["GET", "HEAD"].includes(request.method) ? undefined : await request.text();
  return fetch(targetUrl, {
    method: request.method,
    headers: {
      "Content-Type": request.headers.get("Content-Type") ?? "application/json",
      Authorization: `Bearer ${accessToken}`,
    },
    body,
  });
}

async function handle(request: NextRequest, path: string[]): Promise<NextResponse> {
  const accessToken = readAccessToken(request);
  if (!accessToken) {
    return NextResponse.json({ error: "Not authenticated" }, { status: 401 });
  }

  let upstream = await forward(request, path, accessToken);

  if (upstream.status === 401) {
    const refreshToken = readRefreshToken(request);
    const rotated = refreshToken ? await refreshAccessToken(refreshToken) : null;
    if (!rotated) {
      const failed = NextResponse.json({ error: "Session expired" }, { status: 401 });
      clearSessionCookies(failed);
      return failed;
    }
    upstream = await forward(request, path, rotated.accessToken);
    const body = await upstream.text();
    const response = new NextResponse(body, {
      status: upstream.status,
      headers: { "Content-Type": upstream.headers.get("Content-Type") ?? "application/json" },
    });
    setSessionCookies(response, rotated.accessToken, rotated.refreshToken);
    return response;
  }

  const body = await upstream.text();
  return new NextResponse(body, {
    status: upstream.status,
    headers: { "Content-Type": upstream.headers.get("Content-Type") ?? "application/json" },
  });
}

export async function GET(request: NextRequest, { params }: { params: Promise<{ path: string[] }> }) {
  return handle(request, (await params).path);
}
export async function POST(request: NextRequest, { params }: { params: Promise<{ path: string[] }> }) {
  return handle(request, (await params).path);
}
export async function PATCH(request: NextRequest, { params }: { params: Promise<{ path: string[] }> }) {
  return handle(request, (await params).path);
}
export async function DELETE(request: NextRequest, { params }: { params: Promise<{ path: string[] }> }) {
  return handle(request, (await params).path);
}