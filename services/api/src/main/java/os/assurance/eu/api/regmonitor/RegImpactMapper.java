package os.assurance.eu.api.regmonitor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Conservative keyword → control/obligation code mapper.
 *
 * <p>v1 always emits {@link ImpactLevel#UNCERTAIN}. Never mutates risk class or control status.
 */
@Component
public class RegImpactMapper {

  public List<RegImpactHint> mapHints(UUID regItemId, String title, String summary, Instant now) {
    String text = ((title == null ? "" : title) + " " + (summary == null ? "" : summary))
        .toLowerCase(Locale.ROOT);

    Set<HintKey> keys = new LinkedHashSet<>();

    if (containsAny(text, "human oversight", "art. 14", "article 14", "human-in-the-loop", "stop button")) {
      keys.add(new HintKey("HUMAN_OVERSIGHT", "HUMAN_OVERSIGHT_HIGH_IMPACT",
          "Keyword match suggests human oversight themes; impact UNCERTAIN pending review."));
    }
    if (containsAny(text, "transparency", "art. 50", "article 50", "natural persons", "deepfake", "synthetic")) {
      keys.add(new HintKey("TRANSPARENCY", "TRANSPARENCY_NATURAL_PERSONS",
          "Keyword match suggests transparency-style duties; impact UNCERTAIN pending review."));
    }
    if (containsAny(text, "data governance", "training data", "art. 10", "article 10", "bias", "profiling")) {
      keys.add(new HintKey("DATA_GOVERNANCE", "DATA_GOVERNANCE_PROFILING",
          "Keyword match suggests data governance themes; impact UNCERTAIN pending review."));
    }
    if (containsAny(text, "record-keeping", "record keeping", "logging", "logs of system")) {
      keys.add(new HintKey("RECORD_KEEPING", null,
          "Keyword match suggests logging / record-keeping themes; impact UNCERTAIN pending review."));
    }
    if (containsAny(text, "biometric", "remote biometric", "facial recognition")) {
      keys.add(new HintKey("HUMAN_OVERSIGHT", "BIOMETRIC_IDENTIFICATION",
          "Keyword match suggests biometric identification themes; impact UNCERTAIN pending review."));
    }
    if (containsAny(text, "employment", "worker management", "recruitment", "hr ", " screening", "candidate")) {
      keys.add(new HintKey("HUMAN_OVERSIGHT", "EMPLOYMENT_HR",
          "Keyword match suggests employment/HR themes; impact UNCERTAIN pending review."));
    }
    if (containsAny(text, "essential private", "insurance", "credit scoring", "eligibility", "claims")) {
      keys.add(new HintKey("RISK_MANAGEMENT", "ESSENTIAL_SERVICE_ACCESS",
          "Keyword match suggests essential private service access themes; impact UNCERTAIN pending review."));
    }
    if (containsAny(text, "cybersecurity", "cyber security", "robustness", "accuracy", "poisoning")) {
      keys.add(new HintKey("CYBERSECURITY", null,
          "Keyword match suggests cybersecurity/robustness themes; impact UNCERTAIN pending review."));
      keys.add(new HintKey("ACCURACY_ROBUSTNESS", null,
          "Keyword match suggests accuracy/robustness themes; impact UNCERTAIN pending review."));
    }
    if (containsAny(text, "high-risk", "high risk", "annex iii", "conformity assessment", "technical documentation")) {
      keys.add(new HintKey("RISK_MANAGEMENT", "HIGH_RISK_BUNDLE_SELF_ASSESSED",
          "Keyword match suggests high-risk / Annex III themes; impact UNCERTAIN — never auto-reclassifies."));
      keys.add(new HintKey("TECHNICAL_DOCUMENTATION", null,
          "Keyword match suggests technical documentation themes; impact UNCERTAIN pending review."));
    }
    if (containsAny(text, "risk management", "art. 9", "article 9")) {
      keys.add(new HintKey("RISK_MANAGEMENT", "BASELINE_GOVERNANCE",
          "Keyword match suggests risk management themes; impact UNCERTAIN pending review."));
    }

    // Always at least one conservative baseline hint so humans have a review anchor.
    if (keys.isEmpty()) {
      keys.add(new HintKey("RISK_MANAGEMENT", "BASELINE_GOVERNANCE",
          "No strong keyword match; baseline UNCERTAIN hint for human triage only."));
    }

    List<RegImpactHint> hints = new ArrayList<>();
    for (HintKey key : keys) {
      hints.add(new RegImpactHint(
          UUID.randomUUID(),
          regItemId,
          key.controlCode(),
          key.obligationCode(),
          ImpactLevel.UNCERTAIN,
          key.note(),
          now));
    }
    return hints;
  }

  private static boolean containsAny(String text, String... needles) {
    for (String needle : needles) {
      if (text.contains(needle.toLowerCase(Locale.ROOT))) {
        return true;
      }
    }
    return false;
  }

  private record HintKey(String controlCode, String obligationCode, String note) {
  }
}
