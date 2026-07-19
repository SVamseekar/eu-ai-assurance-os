package os.assurance.eu.api.sector;

import java.util.UUID;
import os.assurance.eu.api.system.ReleaseDecision;

public record ClaimsModelRegisterResponse(
    UUID systemId,
    String externalModelId,
    String sector,
    String packId,
    ReleaseDecision releaseDecision,
    String note) {
}
