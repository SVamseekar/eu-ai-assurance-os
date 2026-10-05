import { EvidenceTableMock } from "@/components/marketing/mockups";
import { ProductDetailPage } from "@/components/marketing/product-detail";
import { marketingMetadata } from "@/lib/seo";

const path = "/product/evidence";
const description =
  "Upload, index and search your own documents. Get cited answers, see what is missing, and map evidence to EU AI Act controls.";

export const metadata = marketingMetadata({
  title: "Evidence management",
  description,
  path,
  keywords: ["AI evidence management", "model card", "DPIA", "EU AI Act technical documentation"],
});

export default function EvidencePage() {
  return (
    <ProductDetailPage
      tone="dark"
      path={path}
      name="Evidence"
      eyebrow="Evidence"
      title="Answers grounded in real sources."
      description={description}
      visual={<EvidenceTableMock />}
      primary={{ href: "/how-it-works", label: "See how it works" }}
      pointsTitle="Evidence that stays attached to the system."
      points={[
        { title: "Upload what you already have", body: "Model cards, DPIAs, oversight procedures, control maps and vendor documents. Text is extracted on upload and indexed per system." },
        { title: "Cited answers", body: "Ask a question and get an answer that cites the documents behind it." },
        { title: "Coverage by risk class", body: "Each risk class needs certain document types. Coverage below the bar keeps the release gate from passing." },
        { title: "Mapped to controls", body: "Link evidence to the EU AI Act controls it supports, so an auditor can follow the trail." },
        { title: "No LLM by default", body: "Your content is not sent to a language model unless you turn that on." },
        { title: "Tenant isolation", body: "Every query is scoped to your workspace, and tests cover it." },
      ]}
    />
  );
}
