package os.assurance.eu.api.assessment;

/**
 * Derived evidence tally. Not stored as a legal result.
 */
public enum EvidenceStatus {
  SATISFIED,
  INSUFFICIENT,
  MISSING,
  NEEDS_HUMAN_REVIEW,
  NOT_APPLICABLE,
  ACCEPTED_EXCEPTION
}
