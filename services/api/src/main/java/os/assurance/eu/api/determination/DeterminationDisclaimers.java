package os.assurance.eu.api.determination;

/**
 * Product-safe legal framing for assisted obligation determination.
 * Never claim compliance, certification, or official conformity assessment.
 */
public final class DeterminationDisclaimers {
  public static final String RULESET_VERSION = "v1";

  public static final String SHORT =
      "Assisted determination (not legal advice). Suggested applicability / obligation map only. "
          + "Requires human legal review.";

  public static final String FULL =
      "Assisted obligation determination (ruleset "
          + RULESET_VERSION
          + "). This is a suggested applicability / obligation map based on questionnaire answers "
          + "and the control catalog. It is not legal advice, not a legal determination, not "
          + "certification, and not an official conformity assessment under the EU AI Act. "
          + "A qualified human legal reviewer must confirm obligations before release decisions.";

  public static final String METRICS_LABEL =
      "Assisted obligation determination (ruleset " + RULESET_VERSION + ")";

  private DeterminationDisclaimers() {
  }
}
