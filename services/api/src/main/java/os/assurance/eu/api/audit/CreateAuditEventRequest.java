package os.assurance.eu.api.audit;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import java.util.UUID;

public record CreateAuditEventRequest(
    UUID systemId,
    @NotBlank String eventType,
    @NotBlank String resourceType,
    String resourceId,
    Map<String, Object> payload) {
}
