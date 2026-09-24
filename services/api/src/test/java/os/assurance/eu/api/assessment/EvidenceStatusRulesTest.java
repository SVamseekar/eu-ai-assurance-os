package os.assurance.eu.api.assessment;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class EvidenceStatusRulesTest {
  private static final LocalDate TODAY = LocalDate.of(2026, 9, 24);

  @Test
  void acceptedInForceLinkInsideTheExistingWindowWithTheRequiredTestIsSatisfied() {
    assertThat(derive(facts("ACCEPTED", "supports", "IN_FORCE", "APPLICABLE", true, true, true)))
        .isEqualTo(EvidenceStatus.SATISFIED);
  }

  @Test
  void acceptedLinkOutsideTheWindowTheScopeOrTheTestIsInsufficient() {
    assertThat(derive(facts("ACCEPTED", "supports", "IN_FORCE", "APPLICABLE", false, true, true)))
        .isEqualTo(EvidenceStatus.INSUFFICIENT);
    assertThat(derive(facts("ACCEPTED", "supports", "IN_FORCE", "APPLICABLE", true, true, false)))
        .isEqualTo(EvidenceStatus.INSUFFICIENT);
    assertThat(derive(facts("ACCEPTED", "supports", "IN_FORCE", "APPLICABLE", true, false, true)))
        .isEqualTo(EvidenceStatus.INSUFFICIENT);
  }

  @Test
  void noAcceptedLinkAndNoExceptionIsMissing() {
    assertThat(derive(facts("PENDING", "supports", "IN_FORCE", "APPLICABLE", true, true, true)))
        .isEqualTo(EvidenceStatus.MISSING);
    assertThat(derive(facts("REJECTED", "supports", "IN_FORCE", "APPLICABLE", true, true, true)))
        .isEqualTo(EvidenceStatus.MISSING);
  }

  @Test
  void uncertainTensionOrAbstainNeedsHumanReview() {
    assertThat(derive(facts("PENDING", "supports", "IN_FORCE", "UNCERTAIN", true, true, true)))
        .isEqualTo(EvidenceStatus.NEEDS_HUMAN_REVIEW);
    assertThat(derive(facts("ACCEPTED", "tension_candidate", "IN_FORCE", "APPLICABLE", true, true, true)))
        .isEqualTo(EvidenceStatus.NEEDS_HUMAN_REVIEW);
    assertThat(derive(facts("PENDING", "abstain", "IN_FORCE", "APPLICABLE", true, true, true)))
        .isEqualTo(EvidenceStatus.NEEDS_HUMAN_REVIEW);
  }

  @Test
  void notApplicableWithANamedReviewerCannotBecomeSatisfied() {
    EvidenceFacts row = facts("ACCEPTED", "supports", "IN_FORCE", "NOT_APPLICABLE", true, true, true)
        .withReviewer("reviewer-1");
    assertThat(EvidenceStatusRules.derive(row, TODAY)).isEqualTo(EvidenceStatus.NOT_APPLICABLE);
    assertThat(EvidenceStatusRules.markSatisfied(row, TODAY)).isFalse();
  }

  @Test
  void acceptedExceptionIsMissingTheDayAfterExpiry() {
    EvidenceFacts open = facts("PENDING", "supports", "IN_FORCE", "APPLICABLE", false, false, false)
        .withException("rationale", TODAY);
    EvidenceFacts expired = facts("PENDING", "supports", "IN_FORCE", "APPLICABLE", false, false, false)
        .withException("rationale", TODAY.minusDays(1));
    assertThat(EvidenceStatusRules.derive(open, TODAY)).isEqualTo(EvidenceStatus.ACCEPTED_EXCEPTION);
    assertThat(EvidenceStatusRules.derive(expired, TODAY)).isEqualTo(EvidenceStatus.MISSING);
  }

  @Test
  void futureAcceptedLinkIsNeitherSatisfiedNorCurrentMissing() {
    EvidenceFacts future = facts("ACCEPTED", "supports", "FUTURE", "APPLICABLE", true, true, true);
    EvidenceStatus status = EvidenceStatusRules.derive(future, TODAY);
    assertThat(status).isNotEqualTo(EvidenceStatus.SATISFIED);
    assertThat(status).isNotEqualTo(EvidenceStatus.MISSING);
    assertThat(EvidenceStatusRules.countsAsCurrent(status)).isFalse();
  }

  private static EvidenceStatus derive(EvidenceFacts facts) {
    return EvidenceStatusRules.derive(facts, TODAY);
  }

  private static EvidenceFacts facts(
      String link,
      String relation,
      String force,
      String applicability,
      boolean evidenceInsideWindow,
      boolean requiredTestPresent,
      boolean scopeMatches) {
    return new EvidenceFacts(
        link, relation, force, applicability, null, null, null, evidenceInsideWindow, requiredTestPresent, scopeMatches);
  }
}
