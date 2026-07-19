import type { Metadata } from "next";

import {
  LegalPageShell,
  legalWebPageJsonLd,
} from "@/components/landing/legal-page-shell";
import { siteConfig } from "@/lib/site-config";

const title = "Privacy Policy";
const description =
  "How EU AI Assurance OS collects, uses, and protects personal data submitted via demo requests, sign-in, and optional analytics.";

export const metadata: Metadata = {
  title,
  description,
  alternates: { canonical: "/privacy" },
  openGraph: {
    title: `${title} — ${siteConfig.name}`,
    description,
    url: `${siteConfig.url}/privacy`,
    type: "website",
  },
};

export default function PrivacyPage() {
  const jsonLd = legalWebPageJsonLd({
    name: `${title} — ${siteConfig.name}`,
    description,
    path: "/privacy",
  });

  return (
    <>
      <script
        type="application/ld+json"
        // eslint-disable-next-line react/no-danger
        dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }}
      />
      <LegalPageShell title={title} description={description}>
        <section>
          <h2 className="font-heading text-lg font-semibold">Who we are</h2>
          <p className="mt-2">
            {siteConfig.name} operates {siteConfig.url.replace(/^https?:\/\//, "")}{" "}
            and provisioned governance workspaces for organisations evaluating or
            operating AI systems under EU AI Act–oriented controls. The product
            owner is {siteConfig.ownerName}.
          </p>
        </section>

        <section>
          <h2 className="font-heading text-lg font-semibold">Data we collect</h2>
          <ul className="mt-2 list-disc space-y-2 pl-5">
            <li>
              <strong>Demo requests:</strong> name, work email, job title, company
              details, AI programme context, and optional message when you submit{" "}
              <a href="/request-demo" className="text-primary hover:underline">
                /request-demo
              </a>
              .
            </li>
            <li>
              <strong>Account sign-in:</strong> credentials or OAuth identity
              attributes needed to provision and authenticate workspace access for
              your organisation.
            </li>
            <li>
              <strong>Product usage:</strong> tenant-scoped governance data you
              enter (systems, evidence metadata, evals, contracts, approvals, audit
              events) within provisioned tenants.
            </li>
            <li>
              <strong>Analytics (optional):</strong> aggregated page analytics via
              Google Analytics when configured in production, subject to your
              browser and any consent tooling.
            </li>
          </ul>
        </section>

        <section>
          <h2 className="font-heading text-lg font-semibold">How we use data</h2>
          <p className="mt-2">
            We use personal data to respond to demo and sales enquiries, operate
            authenticated workspaces, improve reliability and security, and meet
            legal obligations. We do not sell personal data.
          </p>
        </section>

        <section>
          <h2 className="font-heading text-lg font-semibold">Sharing</h2>
          <p className="mt-2">
            Demo notifications may be delivered through operational tooling (for
            example a private Discord webhook). Infrastructure providers (hosting,
            email, identity) process data under their agreements. Tenant data is
            isolated by design; we do not share one organisation&apos;s workspace
            content with another.
          </p>
        </section>

        <section>
          <h2 className="font-heading text-lg font-semibold">Retention</h2>
          <p className="mt-2">
            Demo enquiries are retained as needed for sales follow-up and abuse
            prevention. Workspace and audit data follow your organisation&apos;s
            agreement and product retention settings (audit events are designed for
            long-lived compliance records).
          </p>
        </section>

        <section>
          <h2 className="font-heading text-lg font-semibold">Your rights</h2>
          <p className="mt-2">
            Under applicable data-protection law (including UK GDPR / GDPR where it
            applies) you may request access, correction, or deletion of personal
            data we hold about you, subject to legal retention requirements.
            Contact{" "}
            <a
              href={`mailto:${siteConfig.supportEmail}`}
              className="text-primary hover:underline"
            >
              {siteConfig.supportEmail}
            </a>
            .
          </p>
        </section>
      </LegalPageShell>
    </>
  );
}
