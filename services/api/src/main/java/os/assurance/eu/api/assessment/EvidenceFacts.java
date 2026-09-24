package os.assurance.eu.api.assessment;

import java.time.LocalDate;

/**
 * Inputs that stay separate from the derived status: applicability, force date,
 * and the human link.
 */
public record EvidenceFacts(
    String linkStatus,
    String relation,
    String forceStatus,
    String applicability,
    String reviewerId,
    String exceptionRationale,
    LocalDate exceptionExpiresOn,
    boolean evidenceInsideReviewWindow,
    boolean requiredTestPresent,
    boolean scopeMatches) {

  public EvidenceFacts withReviewer(String reviewerId) {
    return new EvidenceFacts(
        linkStatus,
        relation,
        forceStatus,
        applicability,
        reviewerId,
        exceptionRationale,
        exceptionExpiresOn,
        evidenceInsideReviewWindow,
        requiredTestPresent,
        scopeMatches);
  }

  public EvidenceFacts withException(String rationale, LocalDate expiresOn) {
    return new EvidenceFacts(
        linkStatus,
        relation,
        forceStatus,
        applicability,
        reviewerId,
        rationale,
        expiresOn,
        evidenceInsideReviewWindow,
        requiredTestPresent,
        scopeMatches);
  }
}
