package os.assurance.eu.api.observability;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import os.assurance.eu.api.system.ReleaseDecision;

class AssuranceMetricsTest {

  @Test
  void recordsReleaseGateDecisionAuditAndLoginFailures() {
    SimpleMeterRegistry registry = new SimpleMeterRegistry();
    AssuranceMetrics metrics = new AssuranceMetrics(registry);

    metrics.releaseGateDecision(ReleaseDecision.BLOCKED);
    metrics.releaseGateDecision(ReleaseDecision.PASS);
    metrics.auditAppend();
    metrics.authLoginFailure("invalid_credentials");

    assertThat(registry.find(AssuranceMetrics.RELEASE_GATE_DECISION)
            .tag("decision", "BLOCKED")
            .counter()
            .count())
        .isEqualTo(1.0);
    assertThat(registry.find(AssuranceMetrics.RELEASE_GATE_DECISION)
            .tag("decision", "PASS")
            .counter()
            .count())
        .isEqualTo(1.0);
    assertThat(registry.find(AssuranceMetrics.AUDIT_APPEND).counter().count()).isEqualTo(1.0);
    assertThat(registry.find(AssuranceMetrics.AUTH_LOGIN_FAILURES)
            .tag("reason", "invalid_credentials")
            .counter()
            .count())
        .isEqualTo(1.0);
  }

  @Test
  void nfrTimersRegisterWithoutAssertingSlo() {
    SimpleMeterRegistry registry = new SimpleMeterRegistry();
    NfrMetrics nfr = new NfrMetrics(registry);

    String result = nfr.recordRegistryRead("list", () -> "ok");
    assertThat(result).isEqualTo("ok");
    nfr.recordEvidenceQuery(() -> "answer");

    assertThat(registry.find(NfrMetrics.REGISTRY_READ_TIMER)
            .tag("operation", "list")
            .timer())
        .isNotNull();
    assertThat(registry.find(NfrMetrics.EVIDENCE_QUERY_TIMER)
            .tag("operation", "answer")
            .timer())
        .isNotNull();
    // Deliberately no p95 threshold assertion — see docs/NFR.md
  }
}
