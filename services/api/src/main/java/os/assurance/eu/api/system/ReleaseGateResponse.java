package os.assurance.eu.api.system;

import java.util.List;
import java.util.UUID;

public record ReleaseGateResponse(
    UUID systemId,
    ReleaseDecision decision,
    List<String> blockers,
    List<GateControl> controls) {

  public ReleaseGateResponse(UUID systemId, ReleaseDecision decision, List<String> blockers) {
    this(systemId, decision, blockers, List.of());
  }

  public ReleaseGateResponse {
    blockers = blockers == null ? List.of() : List.copyOf(blockers);
    controls = controls == null ? List.of() : List.copyOf(controls);
  }
}
