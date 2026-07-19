package os.assurance.eu.api.determination;

import java.util.List;
import java.util.Map;

public record QuestionnaireDefinition(
    String rulesetVersion,
    String disclaimer,
    String productLabel,
    List<Question> questions) {

  public record Question(
      String id,
      String label,
      String help,
      String type,
      boolean required,
      List<Map<String, String>> options) {
  }

  public static QuestionnaireDefinition v1() {
    return new QuestionnaireDefinition(
        DeterminationDisclaimers.RULESET_VERSION,
        DeterminationDisclaimers.FULL,
        DeterminationDisclaimers.METRICS_LABEL,
        List.of(
            q("sector", "Sector / domain",
                "Primary business domain of the AI system.",
                "select", true,
                opts("insurance", "hr", "finance", "healthcare", "public_sector", "other")),
            q("users_affected", "Users affected",
                "Scale of natural persons potentially affected.",
                "select", true,
                opts("few", "many", "vulnerable")),
            q("decision_impact", "Decision impact",
                "How system outputs affect people or service access.",
                "select", true,
                opts("none", "informational", "eligibility", "access_to_service", "employment", "unknown")),
            q("biometric", "Biometric identification",
                "Used for biometric identification of natural persons?",
                "boolean_unknown", true,
                booleanUnknownOpts()),
            q("employment", "Employment / HR use",
                "Used for recruitment, screening, performance, or worker management?",
                "boolean_unknown", true,
                booleanUnknownOpts()),
            q("essential_private_service", "Essential private service",
                "Affects access to essential private services (e.g. insurance, credit)?",
                "boolean_unknown", true,
                booleanUnknownOpts()),
            q("human_in_loop", "Human in the loop",
                "Meaningful human review before material outcomes?",
                "boolean_unknown", true,
                booleanUnknownOpts()),
            q("interacts_with_natural_persons", "Interacts with natural persons",
                "Direct interaction with people or content that may be mistaken as human?",
                "boolean", true,
                booleanOpts()),
            q("profiling", "Profiling / automated scoring",
                "Profiles or scores natural persons automatically?",
                "boolean_unknown", true,
                booleanUnknownOpts()),
            q("high_risk_self_assessment", "Operator high-risk self-assessment",
                "Does the operator currently treat this system as high-risk?",
                "boolean_unknown", false,
                booleanUnknownOpts())
        ));
  }

  private static Question q(
      String id,
      String label,
      String help,
      String type,
      boolean required,
      List<Map<String, String>> options) {
    return new Question(id, label, help, type, required, options);
  }

  private static List<Map<String, String>> opts(String... values) {
    return java.util.Arrays.stream(values)
        .map(v -> Map.of("value", v, "label", humanize(v)))
        .toList();
  }

  private static List<Map<String, String>> booleanOpts() {
    return List.of(
        Map.of("value", "true", "label", "Yes"),
        Map.of("value", "false", "label", "No"));
  }

  private static List<Map<String, String>> booleanUnknownOpts() {
    return List.of(
        Map.of("value", "true", "label", "Yes"),
        Map.of("value", "false", "label", "No"),
        Map.of("value", "unknown", "label", "Unknown / need legal input"));
  }

  private static String humanize(String value) {
    return value.replace('_', ' ');
  }
}
