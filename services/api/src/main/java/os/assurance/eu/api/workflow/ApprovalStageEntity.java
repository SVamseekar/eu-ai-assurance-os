package os.assurance.eu.api.workflow;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "approval_stages")
public class ApprovalStageEntity {
  @Id
  private UUID id;

  @Column(nullable = false)
  private UUID tenantId;

  @Column(nullable = false)
  private UUID workflowId;

  @Column(nullable = false)
  private int stageOrder;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private StageType stageType;

  @Column(nullable = false)
  private String requiredRole;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private StageStatus status;

  private UUID actorId;
  private String rationale;
  private Instant actedAt;

  @Column(nullable = false)
  private Instant createdAt;

  protected ApprovalStageEntity() {}

  public ApprovalStageEntity(UUID tenantId, ApprovalStage stage) {
    this.id = stage.id();
    this.tenantId = tenantId;
    this.workflowId = stage.workflowId();
    this.stageOrder = stage.stageOrder();
    this.stageType = stage.stageType();
    this.requiredRole = stage.requiredRole();
    this.status = stage.status();
    this.actorId = stage.actorId();
    this.rationale = stage.rationale();
    this.actedAt = stage.actedAt();
    this.createdAt = stage.createdAt();
  }

  public UUID id() { return id; }
  public UUID tenantId() { return tenantId; }
  public UUID workflowId() { return workflowId; }

  public ApprovalStage toDomain() {
    return new ApprovalStage(id, workflowId, stageOrder, stageType,
        requiredRole, status, actorId, rationale, actedAt, createdAt);
  }
}
