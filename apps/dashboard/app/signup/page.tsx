import type { Metadata } from "next";
import { cookies } from "next/headers";
import { redirect } from "next/navigation";

import { SignupForm } from "@/components/auth/signup-form";
import { parsePlanIntent } from "@/lib/plan-intent";
import { siteConfig } from "@/lib/site-config";

export const metadata: Metadata = {
  title: "Create your workspace",
  description: `Create a ${siteConfig.name} workspace and start a free trial.`,
  robots: { index: false, follow: false },
  alternates: { canonical: "/signup" },
};

export default async function SignupPage({ searchParams }: { searchParams: Promise<Record<string, string | string[]>> }) {
  const raw = await searchParams;
  const intent = parsePlanIntent({ get: (k) => (typeof raw[k] === "string" ? (raw[k] as string) : null) });
  // Signed-in users who pick a paid plan on /pricing go straight to billing.
  if (intent && (await cookies()).has("session_refresh")) redirect("/settings#billing");
  return <SignupForm planIntent={intent} />;
}
