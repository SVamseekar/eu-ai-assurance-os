import type { Metadata } from "next";

import { SignupForm } from "@/components/auth/signup-form";
import { siteConfig } from "@/lib/site-config";

export const metadata: Metadata = {
  title: "Create your workspace",
  description: `Create a ${siteConfig.name} workspace and start a free trial.`,
  robots: { index: false, follow: false },
  alternates: { canonical: "/signup" },
};

export default function SignupPage() {
  return <SignupForm />;
}
