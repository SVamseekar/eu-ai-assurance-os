package os.assurance.eu.api.assessment;

import java.time.LocalDate;

/**
 * Derives one of the six evidence statuses. A future duty is omitted from the
 * current tally. Not-applicable cannot be rewritten into satisfied.
 */
public final class EvidenceStatusRules {
  private EvidenceStatusRules() {
  }

  public static EvidenceStatus derive(EvidenceFacts facts, LocalDate today) {
    if (facts == null) {
      return EvidenceStatus.MISSING;
    }
    if ("NOT_APPLICABLE".equals(facts.applicability()) && named(facts.reviewerId())) {
      return EvidenceStatus.NOT_APPLICABLE;
    }
    if ("UNCERTAIN".equals(facts.applicability())
        || "tension_candidate".equals(facts.relation())
        || "abstain".equals(facts.relation())) {
      return EvidenceStatus.NEEDS_HUMAN_REVIEW;
    }
    if (named(facts.exceptionRationale()) && facts.exceptionExpiresOn() != null) {
      if (today.isAfter(facts.exceptionExpiresOn())) {
        return EvidenceStatus.MISSING;
      }
      return EvidenceStatus.ACCEPTED_EXCEPTION;
    }
    if ("FUTURE".equals(facts.forceStatus())) {
      return null;
    }
    if (!"ACCEPTED".equals(facts.linkStatus())) {
      return EvidenceStatus.MISSING;
    }
    if (!"IN_FORCE".equals(facts.forceStatus())
        || !facts.evidenceInsideReviewWindow()
        || !facts.requiredTestPresent()
        || !facts.scopeMatches()) {
      return EvidenceStatus.INSUFFICIENT;
    }
    return EvidenceStatus.SATISFIED;
  }

  public static boolean markSatisfied(EvidenceFacts facts, LocalDate today) {
    if (facts != null && "NOT_APPLICABLE".equals(facts.applicability())) {
      return false;
    }
    return derive(facts, today) == EvidenceStatus.SATISFIED;
  }

  public static boolean countsAsCurrent(EvidenceStatus status) {
    return status != null;
  }

  private static boolean named(String value) {
    return value != null && !value.isBlank();
  }
}
