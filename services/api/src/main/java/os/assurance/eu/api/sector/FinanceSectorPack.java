package os.assurance.eu.api.sector;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Financial services / KYC vertical — fraud assistants, KYC copilots, credit-adjacent scoring.
 */
@Component
public class FinanceSectorPack implements SectorPack {
  @Override
  public String id() {
    return "finance";
  }

  @Override
  public String displayName() {
    return "Financial services / KYC";
  }

  @Override
  public String summary() {
    return "KYC/fraud assistant controls with elevated logging intensity";
  }

  @Override
  public Set<String> sectorKeys() {
    return Set.of("finance", "financial", "financial_services", "kyc", "banking", "fintech");
  }

  @Override
  public List<SectorControlDef> extraControls() {
    return List.of(
        new SectorControlDef(
            "FIN_KYC_LOGGING",
            "KYC / fraud assistant logging intensity",
            "Retain detailed operational logs for KYC and fraud-assistant outputs, overrides, "
                + "and model version used, suitable for post-market monitoring and complaints.",
            "HIGH",
            "OPS"),
        new SectorControlDef(
            "FIN_FRAUD_FALSE_POSITIVE_REVIEW",
            "Human review of fraud / KYC flags",
            "Route high-impact fraud or KYC adverse flags to trained reviewers before account "
                + "restriction or service denial; track false-positive rates.",
            "HIGH",
            "OVERSIGHT"),
        new SectorControlDef(
            "FIN_CREDIT_EXPLAINABILITY",
            "Credit / KYC decision explainability",
            "Provide operator-facing explanations for automated creditworthiness, KYC risk, "
                + "or fraud scores that affect access to essential financial services.",
            "HIGH",
            "TRANSPARENCY"));
  }

  @Override
  public Map<String, Object> questionnaireDefaults() {
    return Map.of(
        "sector", "finance",
        "essential_private_service", true,
        "decision_impact", "access_to_service",
        "profiling", true,
        "interacts_with_natural_persons", true);
  }

  @Override
  public List<SampleEvidenceTemplate> sampleEvidenceTemplates() {
    return List.of(
        new SampleEvidenceTemplate(
            "finance-kyc-logging",
            "KYC assistant logging policy",
            "LOGGING_POLICY",
            "sector/finance/kyc-logging-policy-template.md",
            "Sample logging intensity policy for KYC / fraud assistants"),
        new SampleEvidenceTemplate(
            "finance-fraud-review",
            "Fraud flag human review SOP",
            "SOP",
            "sector/finance/fraud-review-sop-template.md",
            "Sample SOP for human review of automated fraud flags"));
  }
}
