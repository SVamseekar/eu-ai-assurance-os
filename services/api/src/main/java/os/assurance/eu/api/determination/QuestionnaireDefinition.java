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

  public static QuestionnaireDefinition current() {
    return v2();
  }

  public static QuestionnaireDefinition v1() {
    return v2();
  }

  public static QuestionnaireDefinition v2() {
    return new QuestionnaireDefinition(
        DeterminationDisclaimers.RULESET_VERSION,
        DeterminationDisclaimers.FULL,
        DeterminationDisclaimers.METRICS_LABEL,
        List.of(
            q("operator_role", "Operator role",
                "Are you acting as provider, deployer, importer, or distributor of this system?",
                "select", true,
                opts("provider", "deployer", "importer", "distributor", "unknown")),
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
                booleanUnknownOpts()),
            q("art50_chatbot", "Chatbot / conversational AI",
                "Does a natural person interact with the system in a way that might be mistaken for a human?",
                "boolean_unknown", true,
                booleanUnknownOpts()),
            q("art50_synthetic", "Synthetic content generation",
                "Does the system generate synthetic audio, image, video, or text that should be marked as AI-generated?",
                "boolean_unknown", true,
                booleanUnknownOpts()),
            q("gpai_model", "General-purpose AI model",
                "Is this a GPAI model or a system that embeds one?",
                "boolean_unknown", false,
                booleanUnknownOpts()),
            q("prohibited_social_scoring", "Social scoring of persons",
                "Does the system score natural persons based on social behaviour or personal characteristics for detrimental treatment? (Art. 5 screen — not a legal finding.)",
                "boolean_unknown", true,
                booleanUnknownOpts()),
            q("prohibited_emotion_workplace", "Workplace / education emotion recognition",
                "Does the system infer emotions in the workplace or education? (Art. 5 screen.)",
                "boolean_unknown", true,
                booleanUnknownOpts()),
            q("prohibited_subliminal", "Subliminal or manipulative techniques",
                "Does the system use subliminal or purposefully manipulative techniques to distort behaviour? (Art. 5 screen.)",
                "boolean_unknown", true,
                booleanUnknownOpts()),
            q("prohibited_biometric_realtime", "Real-time remote biometric ID in public",
                "Is real-time remote biometric identification used in publicly accessible spaces? (Art. 5 screen.)",
                "boolean_unknown", true,
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
