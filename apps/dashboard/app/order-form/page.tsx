import type { Metadata } from "next";

import {
  LegalPageShell,
  legalWebPageJsonLd,
} from "@/components/landing/legal-page-shell";
import { siteConfig } from "@/lib/site-config";

const title = "Order form — release readiness sprint";
const description =
  "Commercial offer for a two-week EU AI Act release-readiness sprint. Not a certificate.";

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
          <h2 className="font-heading text-lg font-semibold">SKU</h2>
          <p className="mt-2">
            <strong>EU AI Act release readiness sprint</strong> — one named AI
            system, ten working days. Price: <strong>€4,900</strong> net. Extra
            system in the same sprint: +€1,500. 50% to start, 50% on delivery.
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
            entity. We send an invoice and a workspace invite. Execute the{" "}
            <a href="/msa">MSA</a> and <a href="/dpa">DPA</a> templates after
            counsel review.
          </p>
        </section>
      </LegalPageShell>
    </>
  );
}
