package os.assurance.eu.api.readiness;

/**
 * Product readiness status for conformity documentation — never legal certification.
 */
public enum CertificationReadinessStatus {
  /** Critical gaps remain; release gate blocked or score below floor. */
  NOT_READY,
  /** Score meets threshold and no critical/high gaps — ready for human review only. */
  READY_FOR_REVIEW,
  /** Partial progress; structured gaps remain. */
  GAPS
}
