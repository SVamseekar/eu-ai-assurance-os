package os.assurance.eu.api.contract;

import java.time.Instant;
import java.util.UUID;

public record DriftEventResponse(UUID id, UUID contractId, DriftSeverity severity, String status, Instant createdAt) {
}
