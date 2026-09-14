import type { Metadata } from "next";

import {
  LegalPageShell,
  legalWebPageJsonLd,
} from "@/components/landing/legal-page-shell";
import { siteConfig } from "@/lib/site-config";

const title = "Master Services Agreement (template)";
const description =
  "Template MSA for hosted EU AI Assurance OS and readiness sprints. Counsel must review. Not a certificate of EU AI Act conformity.";

export const metadata: Metadata = {
  title,
  description,
  alternates: { canonical: "/msa" },
};

export default function MsaPage() {
  const jsonLd = legalWebPageJsonLd({
    name: `${title} — ${siteConfig.name}`,
    description,
    path: "/msa",
  });

  return (
    <>
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }}
      />
      <LegalPageShell title={title} description={description}>
        <section>
          <h2 className="font-heading text-lg font-semibold">What we supply</h2>
          <p className="mt-2">
            A hosted governance control plane and, if ordered, a time-boxed
            readiness sprint. Outputs are evidence packs, obligation maps, and
            release-gate decisions. They are <strong>assisted readiness</strong>,
            not notified-body work, not legal advice, and not a Declaration of
            Conformity.
          </p>
        </section>
        <section>
          <h2 className="font-heading text-lg font-semibold">Customer duties</h2>
          <p className="mt-2">
            Customer provides accurate system metadata, evidence, and a human
            legal reviewer. Customer decides whether a model ships.
          </p>
        </section>
        <section>
          <h2 className="font-heading text-lg font-semibold">Liability</h2>
          <p className="mt-2">
            Cap and exclusions to be set in the signed MSA. Do not treat this
            webpage as the executed agreement.
          </p>
        </section>
        <section>
          <h2 className="font-heading text-lg font-semibold">Order of documents</h2>
          <p className="mt-2">
            Signed order form + this MSA + DPA. Website Terms cover the public
            site only.
          </p>
        </section>
      </LegalPageShell>
    </>
  );
}
