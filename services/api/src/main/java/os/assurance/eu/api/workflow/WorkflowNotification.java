package os.assurance.eu.api.workflow;

import java.time.Instant;
import java.util.UUID;

public record WorkflowNotification(
    UUID id,
    UUID workflowId,
    UUID stageId,
    UUID recipientId,
    String eventType,
    String message,
    Instant readAt,
    Instant createdAt) {
}
