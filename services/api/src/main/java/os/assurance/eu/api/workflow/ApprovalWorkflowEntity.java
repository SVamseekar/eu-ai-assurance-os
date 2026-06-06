package os.assurance.eu.api.workflow;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "approval_workflows")
public class ApprovalWorkflowEntity {
  @Id
  private UUID id;

  @Column(nullable = false)
  private UUID tenantId;

  @Column(nullable = false)
  private UUID systemId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private WorkflowTrigger trigger;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private WorkflowStatus status;

  @Column(nullable = false)
  private Instant openedAt;

  private Instant closedAt;

  @Column(nullable = false)
  private Instant createdAt;

  protected ApprovalWorkflowEntity() {}

  public ApprovalWorkflowEntity(UUID tenantId, ApprovalWorkflow workflow) {
    this.id = workflow.id();
    this.tenantId = tenantId;
    this.systemId = workflow.systemId();
    this.trigger = workflow.trigger();
    this.status = workflow.status();
    this.openedAt = workflow.openedAt();
    this.closedAt = workflow.closedAt();
    this.createdAt = workflow.createdAt();
  }

  public UUID id() { return id; }
  public UUID tenantId() { return tenantId; }
  public UUID systemId() { return systemId; }
  public WorkflowStatus status() { return status; }

  public ApprovalWorkflow toDomain(List<ApprovalStage> stages) {
    return new ApprovalWorkflow(id, systemId, trigger, status, stages,
        openedAt, closedAt, createdAt);
  }
}
