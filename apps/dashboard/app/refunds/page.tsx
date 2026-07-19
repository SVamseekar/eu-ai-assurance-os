import type { Metadata } from "next";

import {
  LegalPageShell,
  legalWebPageJsonLd,
} from "@/components/landing/legal-page-shell";
import { siteConfig } from "@/lib/site-config";

const title = "Refunds Policy";
const description =
  "Enterprise cancellation and refund terms for EU AI Assurance OS — order form and statement of work led.";

export const metadata: Metadata = {
  title,
  description,
  alternates: { canonical: "/refunds" },
  openGraph: {
    title: `${title} — ${siteConfig.name}`,
    description,
    url: `${siteConfig.url}/refunds`,
    type: "website",
  },
};

export default function RefundsPage() {
  const jsonLd = legalWebPageJsonLd({
    name: `${title} — ${siteConfig.name}`,
    description,
    path: "/refunds",
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
          <p>
            {siteConfig.name} is sold to organisations on an enterprise basis
            following a tailored demo and scoping conversation.
          </p>
        </section>

        <section>
          <h2 className="font-heading text-lg font-semibold">
            Cancellations and refunds
          </h2>
          <p className="mt-2">
            Cancellation and refund terms are set out in your organisation&apos;s
            order form or statement of work. Contact{" "}
            <a
              href={`mailto:${siteConfig.supportEmail}`}
              className="text-primary hover:underline"
            >
              {siteConfig.supportEmail}
            </a>{" "}
            for billing questions.
          </p>
        </section>

        <section>
          <h2 className="font-heading text-lg font-semibold">Product scope</h2>
          <p className="mt-2">
            This product is a governance control plane for evidence, evals, and
            approvals. It does not provide legal certification or a final
            determination of EU AI Act obligations.
          </p>
        </section>

        <section>
          <h2 className="font-heading text-lg font-semibold">Consumer sales</h2>
          <p className="mt-2">
            {siteConfig.name} is not offered as a consumer self-serve
            subscription with an automatic money-back window. Where mandatory
            consumer or local law requires a different remedy, that law prevails
            over this page.
          </p>
        </section>
      </LegalPageShell>
    </>
  );
}
