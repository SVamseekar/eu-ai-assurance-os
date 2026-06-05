package os.assurance.eu.api.system;

import java.time.Instant;
import java.util.UUID;

public record RiskClassificationResponse(
    UUID systemId,
    RiskClass riskClass,
    String basis,
    boolean humanOversightRequired,
    ReleaseDecision releaseDecision,
    Instant updatedAt) {
}
