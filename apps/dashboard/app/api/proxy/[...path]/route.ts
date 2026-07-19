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
  const body = ["GET", "HEAD"].includes(request.method) ? undefined : await request.arrayBuffer();
  const headers: Record<string, string> = {
    Authorization: `Bearer ${accessToken}`,
  };
  const contentType = request.headers.get("Content-Type");
  if (contentType) {
    headers["Content-Type"] = contentType;
  } else if (body && body.byteLength > 0) {
    headers["Content-Type"] = "application/json";
  }
  return fetch(targetUrl, {
    method: request.method,
    headers,
    body: body && body.byteLength > 0 ? body : undefined,
  });
}

function passthroughHeaders(upstream: Response): Headers {
  const headers = new Headers();
  const contentType = upstream.headers.get("Content-Type");
  if (contentType) headers.set("Content-Type", contentType);
  const disposition = upstream.headers.get("Content-Disposition");
  if (disposition) headers.set("Content-Disposition", disposition);
  const contentSha = upstream.headers.get("X-Content-Sha256");
  if (contentSha) headers.set("X-Content-Sha256", contentSha);
  // Expose custom header to browser JS for PDF export seal display
  headers.set("Access-Control-Expose-Headers", "X-Content-Sha256, Content-Disposition");
  return headers;
}

async function handle(request: NextRequest, path: string[]): Promise<NextResponse> {
  const accessToken = readAccessToken(request);
  if (!accessToken) {
    return NextResponse.json({ error: "Not authenticated" }, { status: 401 });
  }

  let upstream = await forward(request, path, accessToken);
  let sessionCookies: { accessToken: string; refreshToken: string } | null = null;

  if (upstream.status === 401) {
    const refreshToken = readRefreshToken(request);
    const rotated = refreshToken ? await refreshAccessToken(refreshToken) : null;
    if (!rotated) {
      const failed = NextResponse.json({ error: "Session expired" }, { status: 401 });
      clearSessionCookies(failed);
      return failed;
    }
    upstream = await forward(request, path, rotated.accessToken);
    sessionCookies = rotated;
  }

  const body = await upstream.arrayBuffer();
  const response = new NextResponse(body, {
    status: upstream.status,
    headers: passthroughHeaders(upstream),
  });
  if (sessionCookies) {
    setSessionCookies(response, sessionCookies.accessToken, sessionCookies.refreshToken);
  }
  return response;
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
export async function PUT(request: NextRequest, { params }: { params: Promise<{ path: string[] }> }) {
  return handle(request, (await params).path);
}
export async function DELETE(request: NextRequest, { params }: { params: Promise<{ path: string[] }> }) {
  return handle(request, (await params).path);
}
