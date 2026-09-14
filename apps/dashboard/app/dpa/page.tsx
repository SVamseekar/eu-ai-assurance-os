import type { Metadata } from "next";

import {
  LegalPageShell,
  legalWebPageJsonLd,
} from "@/components/landing/legal-page-shell";
import { siteConfig } from "@/lib/site-config";

const title = "Data Processing Addendum (template)";
const description =
  "Template DPA for EU AI Assurance OS workspaces. Have qualified counsel review before signature. Not legal advice.";

export const metadata: Metadata = {
  title,
  description,
  alternates: { canonical: "/dpa" },
  openGraph: {
    title: `${title} — ${siteConfig.name}`,
    description,
    url: `${siteConfig.url}/dpa`,
    type: "website",
  },
};

export default function DpaPage() {
  const jsonLd = legalWebPageJsonLd({
    name: `${title} — ${siteConfig.name}`,
    description,
    path: "/dpa",
  });

  return (
    <>
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }}
      />
      <LegalPageShell title={title} description={description}>
        <section>
          <h2 className="font-heading text-lg font-semibold">Status</h2>
          <p className="mt-2">
            This page is a <strong>starting template</strong> for a processor DPA
            between {siteConfig.ownerName} ({siteConfig.name}) and a customer
            organisation. It is <strong>not</strong> a signed contract and{" "}
            <strong>not legal advice</strong>. Execute a counsel-reviewed DPA
            before processing customer personal data in production.
          </p>
        </section>
        <section>
          <h2 className="font-heading text-lg font-semibold">Roles</h2>
          <p className="mt-2">
            Customer is the controller of account, invite, and AI-system
            metadata they submit. {siteConfig.name} is a processor for that
            workspace data. Evidence content uploaded by the customer remains
            the customer&apos;s responsibility.
          </p>
        </section>
        <section>
          <h2 className="font-heading text-lg font-semibold">Processing</h2>
          <p className="mt-2">
            Purpose: host the governance control plane (registry, evidence,
            evals, audit, release gates). Duration: the subscription or sprint
            term plus backup retention. Location: EU region as specified on the
            order form. Sub-processors: hosting, email, and error monitoring as
            listed to the customer in writing.
          </p>
        </section>
        <section>
          <h2 className="font-heading text-lg font-semibold">Security</h2>
          <p className="mt-2">
            Tenant isolation by verified credentials, hashed API keys, JWT
            sessions, hash-chained audit, and optional object storage. This is
            not a SOC 2 report.
          </p>
        </section>
        <section>
          <h2 className="font-heading text-lg font-semibold">Contact</h2>
          <p className="mt-2">
            <a href={`mailto:${siteConfig.supportEmail}`}>{siteConfig.supportEmail}</a>
          </p>
        </section>
      </LegalPageShell>
    </>
  );
}
