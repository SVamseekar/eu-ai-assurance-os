package os.assurance.eu.api.readiness;

/**
 * Structured readiness gap. Never implies legal non-conformity certification status.
 */
public record ReadinessGap(
    String code,
    ReadinessGapSeverity severity,
    String message,
    String remediationHint,
    String dimension) {
}
