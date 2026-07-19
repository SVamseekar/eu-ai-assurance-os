package os.assurance.eu.api.control;

import java.time.Instant;
import java.util.UUID;

public record SystemControl(
    UUID id,
    UUID systemId,
    UUID controlId,
    String controlCode,
    String controlName,
    String category,
    ControlStatus status,
    boolean evidenceRequired,
    UUID reviewerId,
    String notes,
    Instant updatedAt) {
}
