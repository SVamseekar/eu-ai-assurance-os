import type { NextConfig } from "next";
import { securityHeaders } from "./lib/security-headers";

const nextConfig: NextConfig = {
  reactCompiler: true,
  // Self-host Docker image only (infra/Dockerfile.dashboard sets DOCKER_BUILD=1).
  // Vercel production builds leave this unset and use the platform bundler.
  ...(process.env.DOCKER_BUILD === "1" ? { output: "standalone" as const } : {}),
  async headers() {
    return [{ source: "/:path*", headers: securityHeaders() }];
  },
};

export default nextConfig;
