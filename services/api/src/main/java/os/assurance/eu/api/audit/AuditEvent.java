package os.assurance.eu.api.audit;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AuditEvent(
    UUID id,
    UUID systemId,
    UUID actorId,
    String eventType,
    String resourceType,
    String resourceId,
    Map<String, Object> payload,
    Instant createdAt) {
}
