package os.assurance.eu.api.workflow;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ApprovalWorkflow(
    UUID id,
    UUID systemId,
    WorkflowTrigger trigger,
    WorkflowStatus status,
    List<ApprovalStage> stages,
    Instant openedAt,
    Instant closedAt,
    Instant createdAt) {
}
