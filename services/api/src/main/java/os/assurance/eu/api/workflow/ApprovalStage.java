package os.assurance.eu.api.workflow;

import java.time.Instant;
import java.util.UUID;

public record ApprovalStage(
    UUID id,
    UUID workflowId,
    int stageOrder,
    StageType stageType,
    String requiredRole,
    UUID assignedReviewerId,
    StageStatus status,
    UUID actorId,
    String rationale,
    String oversightEvidence,
    Instant actedAt,
    Instant notificationSentAt,
    Instant createdAt) {
}
