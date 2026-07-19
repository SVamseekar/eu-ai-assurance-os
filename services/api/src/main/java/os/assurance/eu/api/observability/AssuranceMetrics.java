package os.assurance.eu.api.observability;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;
import os.assurance.eu.api.system.ReleaseDecision;

/**
 * Product metrics for Part 8 observability (release gate, audit, auth).
 *
 * <p>Latency timers for registry/evidence remain on {@link NfrMetrics}. Counters here are
 * operational signals only — they do not certify SLO compliance (see {@code docs/NFR.md}).
 */
@Component
public class AssuranceMetrics {
  public static final String RELEASE_GATE_DECISION = "assurance.release_gate.decision";
  public static final String AUDIT_APPEND = "assurance.audit.append";
  public static final String AUTH_LOGIN_FAILURES = "assurance.auth.login.failures";

  private final MeterRegistry meterRegistry;

  public AssuranceMetrics(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
  }

  public void releaseGateDecision(ReleaseDecision decision) {
    String value = decision == null ? "UNKNOWN" : decision.name();
    meterRegistry.counter(RELEASE_GATE_DECISION, "decision", value).increment();
  }

  public void auditAppend() {
    meterRegistry.counter(AUDIT_APPEND).increment();
  }

  public void authLoginFailure(String reason) {
    String tag = reason == null || reason.isBlank() ? "unknown" : reason;
    meterRegistry.counter(AUTH_LOGIN_FAILURES, "reason", tag).increment();
  }
}
