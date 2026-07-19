package os.assurance.eu.api.determination;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ObligationRuleEngineTest {
  private ObligationRuleEngine engine;

  @BeforeEach
  void setUp() {
    engine = new ObligationRuleEngine();
  }

  @Test
  void insuranceHighPathMarksEssentialServiceAndOversightApplicable() {
    List<ObligationRule> rules = sampleRules();
    Map<String, Object> answers = Map.of(
        "sector", "insurance",
        "users_affected", "many",
        "decision_impact", "eligibility",
        "biometric", false,
        "employment", false,
        "essential_private_service", true,
        "human_in_loop", true,
        "interacts_with_natural_persons", true,
        "profiling", true,
        "high_risk_self_assessment", true);

    List<DeterminationObligation> result = engine.evaluate(rules, answers);

    assertThat(result).isNotEmpty();
    assertThat(find(result, "ESSENTIAL_SERVICE_ACCESS").applicability())
        .isEqualTo(Applicability.APPLICABLE);
    assertThat(find(result, "BIOMETRIC_IDENTIFICATION").applicability())
        .isEqualTo(Applicability.NOT_APPLICABLE);
    assertThat(find(result, "TRANSPARENCY_NATURAL_PERSONS").applicability())
        .isEqualTo(Applicability.APPLICABLE);
    assertThat(find(result, "BASELINE_GOVERNANCE").applicability())
        .isEqualTo(Applicability.APPLICABLE);
    assertThat(find(result, "HIGH_RISK_BUNDLE_SELF_ASSESSED").applicability())
        .isEqualTo(Applicability.APPLICABLE);
    assertThat(find(result, "ESSENTIAL_SERVICE_ACCESS").controlCodes())
        .contains("HUMAN_OVERSIGHT", "RISK_MANAGEMENT");
  }

  @Test
  void minimalChatbotPathIsReducedSet() {
    List<ObligationRule> rules = sampleRules();
    Map<String, Object> answers = Map.of(
        "sector", "other",
        "users_affected", "few",
        "decision_impact", "informational",
        "biometric", false,
        "employment", false,
        "essential_private_service", false,
        "human_in_loop", false,
        "interacts_with_natural_persons", true,
        "profiling", false,
        "high_risk_self_assessment", false);

    List<DeterminationObligation> result = engine.evaluate(rules, answers);

    assertThat(find(result, "ESSENTIAL_SERVICE_ACCESS").applicability())
        .isEqualTo(Applicability.NOT_APPLICABLE);
    assertThat(find(result, "EMPLOYMENT_HR").applicability())
        .isEqualTo(Applicability.NOT_APPLICABLE);
    assertThat(find(result, "BIOMETRIC_IDENTIFICATION").applicability())
        .isEqualTo(Applicability.NOT_APPLICABLE);
    assertThat(find(result, "HIGH_RISK_BUNDLE_SELF_ASSESSED").applicability())
        .isEqualTo(Applicability.NOT_APPLICABLE);
    assertThat(find(result, "TRANSPARENCY_NATURAL_PERSONS").applicability())
        .isEqualTo(Applicability.APPLICABLE);
    assertThat(find(result, "BASELINE_GOVERNANCE").applicability())
        .isEqualTo(Applicability.APPLICABLE);

    long highApplicable = result.stream()
        .filter(o -> o.applicability() == Applicability.APPLICABLE)
        .filter(o -> "HIGH".equals(o.severity()))
        .count();
    assertThat(highApplicable).isZero();
  }

  @Test
  void unknownEssentialServiceYieldsUncertain() {
    List<ObligationRule> rules = sampleRules();
    Map<String, Object> answers = Map.of(
        "sector", "insurance",
        "decision_impact", "unknown",
        "essential_private_service", true,
        "biometric", false,
        "employment", false,
        "interacts_with_natural_persons", false,
        "profiling", false,
        "human_in_loop", true,
        "high_risk_self_assessment", false);

    List<DeterminationObligation> result = engine.evaluate(rules, answers);
    assertThat(find(result, "ESSENTIAL_SERVICE_ACCESS").applicability())
        .isEqualTo(Applicability.UNCERTAIN);
  }

  private DeterminationObligation find(List<DeterminationObligation> items, String code) {
    return items.stream()
        .filter(o -> code.equals(o.ruleCode()))
        .findFirst()
        .orElseThrow();
  }

  private List<ObligationRule> sampleRules() {
    return List.of(
        rule("ESSENTIAL_SERVICE_ACCESS", "HIGH",
            Map.of(
                "applicableIf", Map.of("all", List.of(
                    Map.of("field", "essential_private_service", "op", "eq", "value", true),
                    Map.of("field", "decision_impact", "op", "in", "value",
                        List.of("eligibility", "access_to_service")))),
                "uncertainIf", Map.of("all", List.of(
                    Map.of("field", "essential_private_service", "op", "eq", "value", true),
                    Map.of("field", "decision_impact", "op", "eq", "value", "unknown")))),
            List.of("RISK_MANAGEMENT", "HUMAN_OVERSIGHT")),
        rule("BIOMETRIC_IDENTIFICATION", "HIGH",
            Map.of("applicableIf", Map.of("all", List.of(
                Map.of("field", "biometric", "op", "eq", "value", true)))),
            List.of("HUMAN_OVERSIGHT")),
        rule("EMPLOYMENT_HR", "HIGH",
            Map.of("applicableIf", Map.of("all", List.of(
                Map.of("field", "employment", "op", "eq", "value", true)))),
            List.of("HUMAN_OVERSIGHT")),
        rule("TRANSPARENCY_NATURAL_PERSONS", "MEDIUM",
            Map.of("applicableIf", Map.of("all", List.of(
                Map.of("field", "interacts_with_natural_persons", "op", "eq", "value", true)))),
            List.of("TRANSPARENCY")),
        rule("BASELINE_GOVERNANCE", "LOW",
            Map.of("applicableIf", Map.of("all", List.of())),
            List.of("RISK_MANAGEMENT", "DATA_GOVERNANCE")),
        rule("HIGH_RISK_BUNDLE_SELF_ASSESSED", "HIGH",
            Map.of("applicableIf", Map.of("all", List.of(
                Map.of("field", "high_risk_self_assessment", "op", "eq", "value", true)))),
            List.of("HUMAN_OVERSIGHT", "TECHNICAL_DOCUMENTATION"))
    );
  }

  private ObligationRule rule(
      String code, String severity, Map<String, Object> when, List<String> controls) {
    return new ObligationRule(
        UUID.randomUUID(),
        code,
        code,
        "test",
        "indicative",
        when,
        severity,
        controls,
        DeterminationDisclaimers.RULESET_VERSION,
        true);
  }
}
