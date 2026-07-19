package os.assurance.eu.api.sector;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Insurance / claims vertical — primary PRD use case (Claims Triage AI).
 * Depth: claims automation fairness, adverse decision human review, model cards.
 */
@Component
public class InsuranceSectorPack implements SectorPack {
  @Override
  public String id() {
    return "insurance";
  }

  @Override
  public String displayName() {
    return "Insurance / Claims";
  }

  @Override
  public String summary() {
    return "Claims triage depth: fairness, adverse-decision review, claims model cards";
  }

  @Override
  public Set<String> sectorKeys() {
    return Set.of("insurance", "claims", "insurer", "underwriting");
  }

  @Override
  public List<SectorControlDef> extraControls() {
    return List.of(
        new SectorControlDef(
            "INS_CLAIMS_FAIRNESS",
            "Claims automation fairness",
            "Document bias testing and fairness monitoring for automated claims triage, "
                + "routing, and eligibility scoring across protected attributes and claim segments.",
            "HIGH",
            "FAIRNESS"),
        new SectorControlDef(
            "INS_ADVERSE_DECISION_REVIEW",
            "Human review of adverse claim decisions",
            "Require meaningful human review before adverse automated outcomes "
                + "(denial, down-tiering, material delay) with an override path and audit trail.",
            "HIGH",
            "OVERSIGHT"),
        new SectorControlDef(
            "INS_CLAIMS_EXPLAINABILITY",
            "Claims decision explainability",
            "Provide claim-handler-facing rationales for model-driven triage scores and "
                + "material routing decisions suitable for complaint handling.",
            "HIGH",
            "TRANSPARENCY"),
        new SectorControlDef(
            "INS_MODEL_CARD_CLAIMS",
            "Claims model card documentation",
            "Maintain a claims-specific model card covering training data sources, known "
                + "limitations, intended use (triage vs final decision), and human oversight points.",
            "HIGH,LIMITED",
            "DOCUMENTATION"));
  }

  @Override
  public Map<String, Object> questionnaireDefaults() {
    return Map.of(
        "sector", "insurance",
        "essential_private_service", true,
        "decision_impact", "eligibility",
        "profiling", true,
        "interacts_with_natural_persons", true);
  }

  @Override
  public List<SampleEvidenceTemplate> sampleEvidenceTemplates() {
    return List.of(
        new SampleEvidenceTemplate(
            "insurance-dpia",
            "Claims AI DPIA starter",
            "DPIA",
            "sector/insurance/dpia-template.md",
            "Sample DPIA outline for claims triage / eligibility AI"),
        new SampleEvidenceTemplate(
            "insurance-model-card",
            "Claims model card starter",
            "MODEL_CARD",
            "sector/insurance/model-card-template.md",
            "Sample model card for claims automation systems"));
  }
}
