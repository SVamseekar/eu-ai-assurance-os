package os.assurance.eu.api.system;

import java.util.ArrayList;
import java.util.List;

public final class ControlModePolicy {
  private ControlModePolicy() {
  }

  public static ReleaseDecision apply(ReleaseDecision decision, List<GateControl> controls) {
    ReleaseDecision next = decision;
    if (controls == null) {
      return next;
    }
    for (GateControl control : controls) {
      if (control.mode() == null || control.mode() == ControlMode.INFORMATIONAL) {
        continue;
      }
      if (!"IN_FORCE".equals(control.forceStatus())) {
        continue;
      }
      if (control.mode() == ControlMode.BLOCKING) {
        next = ReleaseDecision.BLOCKED;
        continue;
      }
      if (next == ReleaseDecision.BLOCKED) {
        continue;
      }
      if (control.mode() == ControlMode.WARNING && next == ReleaseDecision.PASS) {
        next = ReleaseDecision.REVIEW;
      }
      if (control.mode() == ControlMode.APPROVAL_REQUIRED && !control.signedOff()) {
        next = ReleaseDecision.REVIEW;
      }
    }
    return next;
  }

  public static List<String> blockers(ReleaseDecision decision, List<String> existing, List<GateControl> controls) {
    List<String> blockers = new ArrayList<>();
    if (existing != null) {
      blockers.addAll(existing);
    }
    if (controls == null || decision != ReleaseDecision.BLOCKED) {
      return List.copyOf(blockers);
    }
    for (GateControl control : controls) {
      if (control.mode() == ControlMode.BLOCKING && "IN_FORCE".equals(control.forceStatus())) {
        String marker = "CONTROL_MODE:BLOCKING:" + control.provisionKey();
        if (!blockers.contains(marker)) {
          blockers.add(marker);
        }
      }
    }
    return List.copyOf(blockers);
  }
}
