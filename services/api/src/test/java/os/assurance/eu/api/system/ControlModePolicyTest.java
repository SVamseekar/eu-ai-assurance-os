package os.assurance.eu.api.system;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ControlModePolicyTest {
  private static final GateControl FUTURE = control(ControlMode.INFORMATIONAL, "FUTURE", false);
  private static final GateControl WARNING = control(ControlMode.WARNING, "IN_FORCE", false);
  private static final GateControl BLOCKING = control(ControlMode.BLOCKING, "IN_FORCE", false);
  private static final GateControl APPROVAL_OPEN = control(ControlMode.APPROVAL_REQUIRED, "IN_FORCE", false);
  private static final GateControl APPROVAL_SIGNED = control(ControlMode.APPROVAL_REQUIRED, "IN_FORCE", true);

  @Test
  void informationalNeverMovesPassReviewOrBlocked() {
    assertThat(ControlModePolicy.apply(ReleaseDecision.PASS, List.of(FUTURE))).isEqualTo(ReleaseDecision.PASS);
    assertThat(ControlModePolicy.apply(ReleaseDecision.REVIEW, List.of(FUTURE))).isEqualTo(ReleaseDecision.REVIEW);
    assertThat(ControlModePolicy.apply(ReleaseDecision.BLOCKED, List.of(FUTURE))).isEqualTo(ReleaseDecision.BLOCKED);
  }

  @Test
  void warningCanForceReviewAndBlockingReturnsBlocked() {
    assertThat(ControlModePolicy.apply(ReleaseDecision.PASS, List.of(WARNING))).isEqualTo(ReleaseDecision.REVIEW);
    assertThat(ControlModePolicy.apply(ReleaseDecision.BLOCKED, List.of(WARNING))).isEqualTo(ReleaseDecision.BLOCKED);
    assertThat(ControlModePolicy.apply(ReleaseDecision.PASS, List.of(BLOCKING))).isEqualTo(ReleaseDecision.BLOCKED);
  }

  @Test
  void approvalRequiredStaysReviewUntilSigned() {
    assertThat(ControlModePolicy.apply(ReleaseDecision.PASS, List.of(APPROVAL_OPEN))).isEqualTo(ReleaseDecision.REVIEW);
    assertThat(ControlModePolicy.apply(ReleaseDecision.PASS, List.of(APPROVAL_SIGNED))).isEqualTo(ReleaseDecision.PASS);
  }

  private static GateControl control(ControlMode mode, String force, boolean signedOff) {
    return new GateControl(UUID.randomUUID(), "key", force, LocalDate.of(2026, 8, 2), mode, signedOff);
  }
}
