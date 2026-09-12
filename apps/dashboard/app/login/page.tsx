import type { Metadata } from "next";

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

export default async function LoginPage({
  searchParams,
}: {
  searchParams: Promise<{ next?: string; auth_error?: string }>;
}) {
  const params = await searchParams;
  return <LoginScreen nextPath={params.next} authErrorCode={params.auth_error} />;
}
