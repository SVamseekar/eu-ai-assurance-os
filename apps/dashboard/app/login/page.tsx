import type { Metadata } from "next";
import { Suspense } from "react";
import { ShieldCheck } from "lucide-react";

import { LoginScreen } from "@/components/auth/login-screen";
import { siteConfig } from "@/lib/site-config";

export const metadata: Metadata = {
  title: "Sign in",
  description: `Sign in to ${siteConfig.name} — EU AI Act release governance for your organisation.`,
  robots: {
    index: false,
    follow: false,
  },
  alternates: { canonical: "/login" },
  openGraph: {
    title: `Sign in — ${siteConfig.name}`,
    description: `Sign in to ${siteConfig.name}.`,
    url: `${siteConfig.url}/login`,
    type: "website",
  },
};

function LoginFallback() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center gap-3 bg-background">
      <ShieldCheck className="size-6 animate-pulse text-primary" aria-hidden="true" />
      <p className="text-sm text-muted-foreground">Loading sign-in…</p>
    </div>
  );
}

export default function LoginPage() {
  return (
    <Suspense fallback={<LoginFallback />}>
      <LoginScreen />
    </Suspense>
  );
}
