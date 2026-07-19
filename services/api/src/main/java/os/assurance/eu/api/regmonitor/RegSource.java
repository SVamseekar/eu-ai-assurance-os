package os.assurance.eu.api.regmonitor;

import java.time.Instant;
import java.util.UUID;

public record RegSource(
    UUID id,
    String code,
    String name,
    String url,
    FeedType feedType,
    int pollIntervalSeconds,
    boolean enabled,
    Instant lastPolledAt,
    String notes,
    Instant createdAt) {
}
