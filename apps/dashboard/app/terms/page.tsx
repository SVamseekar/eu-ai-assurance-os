import type { Metadata } from "next";

import {
  LegalPageShell,
  legalWebPageJsonLd,
} from "@/components/landing/legal-page-shell";
import { siteConfig } from "@/lib/site-config";

const title = "Terms of Service";
const description =
  "Terms governing access to the EU AI Assurance OS website, demo requests, and provisioned AI governance workspaces.";

export const metadata: Metadata = {
  title,
  description,
  alternates: { canonical: "/terms" },
  openGraph: {
    title: `${title} — ${siteConfig.name}`,
    description,
    url: `${siteConfig.url}/terms`,
    type: "website",
  },
};

export default function TermsPage() {
  const jsonLd = legalWebPageJsonLd({
    name: `${title} — ${siteConfig.name}`,
    description,
    path: "/terms",
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
          <h2 className="font-heading text-lg font-semibold">Agreement</h2>
          <p className="mt-2">
            These Terms govern your use of the {siteConfig.name} marketing website
            and any provisioned governance workspace made available to your
            organisation. By using the site or submitting a demo request, you
            agree to these Terms.
          </p>
        </section>

        <section>
          <h2 className="font-heading text-lg font-semibold">Service scope</h2>
          <p className="mt-2">
            {siteConfig.name} is a governance control plane for evidence, eval
            gates, data contracts, approvals, and audit-ready release decisions. It
            helps organisations organise controls and readiness artefacts. It is{" "}
            <strong>not</strong> legal advice, <strong>not</strong> a notified-body
            service, and <strong>does not</strong> issue legal certifications or
            final determinations of EU AI Act obligations. See also our{" "}
            <a href="/disclaimer" className="text-primary hover:underline">
              Disclaimer
            </a>
            .
          </p>
        </section>

        <section>
          <h2 className="font-heading text-lg font-semibold">Accounts &amp; access</h2>
          <p className="mt-2">
            Workspace access is provided to organisations under a commercial
            arrangement (order form, statement of work, or equivalent). You must
            keep credentials confidential and ensure only authorised personnel use
            your tenant. You are responsible for content uploaded into your
            workspace.
          </p>
        </section>

        <section>
          <h2 className="font-heading text-lg font-semibold">Acceptable use</h2>
          <p className="mt-2">
            You may not misuse the service (including probing for vulnerabilities
            without authorisation, reverse engineering beyond applicable law,
            attempting to bypass tenant isolation, or submitting unlawful content).
            Demo forms must not be used for spam or automated abuse.
          </p>
        </section>

        <section>
          <h2 className="font-heading text-lg font-semibold">Intellectual property</h2>
          <p className="mt-2">
            The product, branding, and site content remain the property of{" "}
            {siteConfig.ownerName} and licensors. Your organisation retains rights
            to data you submit, subject to the licence needed for us to host and
            process it to provide the service.
          </p>
        </section>

        <section>
          <h2 className="font-heading text-lg font-semibold">
            Liability &amp; warranties
          </h2>
          <p className="mt-2">
            The website and product are provided on an &quot;as available&quot;
            basis. To the fullest extent permitted by law, we disclaim warranties
            that the product ensures regulatory conformity. Commercial liability
            caps and indemnities, if any, are set out in your organisation&apos;s
            order form or statement of work.
          </p>
        </section>

        <section>
          <h2 className="font-heading text-lg font-semibold">Contact</h2>
          <p className="mt-2">
            Questions:{" "}
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
