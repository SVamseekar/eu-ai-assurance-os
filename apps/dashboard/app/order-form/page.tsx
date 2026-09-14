import type { Metadata } from "next";

import {
  LegalPageShell,
  legalWebPageJsonLd,
} from "@/components/landing/legal-page-shell";
import { siteConfig } from "@/lib/site-config";

const title = "Order form — release readiness sprint";
const description =
  "Template order form for a scoped EU AI Act release-readiness engagement. Fees are those in the written quote. Not a certificate.";

export const metadata: Metadata = {
  title,
  description,
  alternates: { canonical: "/order-form" },
};

export default function OrderFormPage() {
  const jsonLd = legalWebPageJsonLd({
    name: `${title} — ${siteConfig.name}`,
    description,
    path: "/order-form",
  });

  return (
    <>
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }}
      />
      <LegalPageShell title={title} description={description}>
        <section>
          <h2 className="font-heading text-lg font-semibold">Engagement</h2>
          <p className="mt-2">
            <strong>EU AI Act release readiness sprint</strong> — one named AI
            system. Fees, extra systems, term, and payment schedule are those
            set out in the written quote. This page is a template, not an offer
            of a list price.
          </p>
        </section>
        <section>
          <h2 className="font-heading text-lg font-semibold">Deliverables</h2>
          <ul className="mt-2 list-disc space-y-1 pl-5">
            <li>Evgraph library scan (approval-before-deploy, dataset license) — you keep the CLI</li>
            <li>System registered in {siteConfig.name}: risk class, obligation map (assisted), PASS / REVIEW / BLOCKED</li>
            <li>Sealed evidence pack (JSON + PDF hash) including Annex IV-shaped checklist</li>
            <li>Optional CI snippets for both gates</li>
            <li>60-minute readout</li>
          </ul>
        </section>
        <section>
          <h2 className="font-heading text-lg font-semibold">Not included</h2>
          <p className="mt-2">
            Legal certification, notified-body assessment, Annex IV as a legal
            instrument, FRIA as legal advice, EU database registration, Stripe
            self-serve subscription.
          </p>
        </section>
        <section>
          <h2 className="font-heading text-lg font-semibold">How to order</h2>
          <p className="mt-2">
            Email {siteConfig.supportEmail} with the system name and billing
            entity. After a demo we send a written quote, then an invoice and a
            workspace invite. Execute the{" "}
            <a href="/msa">MSA</a> and <a href="/dpa">DPA</a> templates after
            counsel review.
          </p>
        </section>
      </LegalPageShell>
    </>
  );
}
