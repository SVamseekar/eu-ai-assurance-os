import { EvalRunMock } from "@/components/marketing/mockups";
import { ProductDetailPage } from "@/components/marketing/product-detail";
import { marketingMetadata } from "@/lib/seo";

const path = "/product/evaluations";
const description =
  "Use your own eval harnesses. Send signed results, compare them against fixed thresholds, and include them in the release decision.";

export const metadata = marketingMetadata({
  title: "Evaluations",
  description,
  path,
  keywords: ["AI evaluation thresholds", "LLM eval gate", "AI accuracy robustness evidence"],
});

export default function EvaluationsPage() {
  return (
    <ProductDetailPage
      tone="light"
      path={path}
      name="Evaluations"
      eyebrow="Evaluations"
      title="Connect your evaluation results."
      description={description}
      visual={<EvalRunMock />}
      primary={{ href: "/product/release-gate", label: "See the release gate" }}
      pointsTitle="Scores that decide, not decorate."
      points={[
        { title: "Bring your own harness", body: "Results arrive from your eval pipeline through an HMAC-signed callback, so scores cannot be forged in transit." },
        { title: "Fixed thresholds", body: "Each system has thresholds per metric. The latest completed run must meet them or the release does not pass." },
        { title: "Part of the decision", body: "Eval results feed the same PASS, REVIEW or BLOCKED decision as evidence, contracts and approvals." },
        { title: "History per release", body: "Every run is kept with its dataset, model and prompt versions, so you can show what was tested." },
        { title: "In the evidence pack", body: "The signed pack carries the eval run behind the release decision." },
        { title: "Accuracy and robustness", body: "Records that support the EU AI Act's accuracy and robustness expectations for high-risk systems." },
      ]}
    />
  );
}
