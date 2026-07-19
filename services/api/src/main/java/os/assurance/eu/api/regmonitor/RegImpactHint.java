package os.assurance.eu.api.regmonitor;

import java.time.Instant;
import java.util.UUID;

public record RegImpactHint(
    UUID id,
    UUID regItemId,
    String controlCode,
    String obligationCode,
    ImpactLevel impactLevel,
    String impactNote,
    Instant createdAt) {
}
