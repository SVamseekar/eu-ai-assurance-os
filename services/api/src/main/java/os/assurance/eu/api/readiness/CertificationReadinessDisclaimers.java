package os.assurance.eu.api.readiness;

/**
 * Product-safe framing for certification readiness automation.
 * Never claim notified-body status, official conformity, or legal certification.
 */
public final class CertificationReadinessDisclaimers {
  public static final String PRODUCT_LABEL = "Certification readiness automation";

  public static final String SHORT =
      "Certification readiness score and gap report only. Not legal certification "
          + "and not an official conformity assessment under the EU AI Act.";

  public static final String FULL =
      "Certification readiness automation produces a weighted readiness score (0–100) "
          + "and a structured gap list toward conformity documentation themes. "
          + "It does not issue certificates, does not declare Regulation conformity, "
          + "is not notified-body attestation, and is not legal advice. "
          + "A qualified human reviewer must confirm documentation completeness before "
          + "any external conformity process.";

  private CertificationReadinessDisclaimers() {
  }
}
