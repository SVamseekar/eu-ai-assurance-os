package os.assurance.eu.api.workflow;

import java.time.Instant;
import java.util.UUID;

public record ApprovalStage(
    UUID id,
    UUID workflowId,
    int stageOrder,
    StageType stageType,
    String requiredRole,
    StageStatus status,
    UUID actorId,
    String rationale,
    Instant actedAt,
    Instant createdAt) {
}
