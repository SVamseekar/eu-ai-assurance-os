package os.assurance.eu.api.system;

import java.time.LocalDate;
import java.util.UUID;

public record GateControl(
    UUID proposalId,
    String provisionKey,
    String forceStatus,
    LocalDate forceFrom,
    ControlMode mode,
    boolean signedOff) {
}
