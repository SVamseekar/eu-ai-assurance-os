package os.assurance.eu.api.regmonitor;

import java.time.Instant;
import java.util.UUID;

public record RegItemReview(
    UUID id,
    UUID regItemId,
    UUID reviewedBy,
    Instant reviewedAt,
    String notes) {
}
