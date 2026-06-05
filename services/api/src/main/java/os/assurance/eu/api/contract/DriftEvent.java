package os.assurance.eu.api.contract;

import java.time.Instant;
import java.util.UUID;

public record DriftEvent(
    UUID id,
    UUID contractId,
    DriftSeverity severity,
    String field,
    String description,
    DriftStatus status,
    Instant createdAt,
    Instant updatedAt) {
}
