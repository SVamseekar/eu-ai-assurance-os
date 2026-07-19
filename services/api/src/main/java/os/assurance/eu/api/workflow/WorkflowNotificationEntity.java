package os.assurance.eu.api.workflow;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "workflow_notifications")
public class WorkflowNotificationEntity {
  @Id
  private UUID id;

  @Column(nullable = false)
  private UUID tenantId;

  @Column(nullable = false)
  private UUID workflowId;

  private UUID stageId;
  private UUID recipientId;

  @Column(nullable = false)
  private String eventType;

  @Column(nullable = false)
  private String message;

  private Instant readAt;

  @Column(nullable = false)
  private Instant createdAt;

  protected WorkflowNotificationEntity() {}

  public WorkflowNotificationEntity(UUID tenantId, WorkflowNotification notification) {
    this.id = notification.id();
    this.tenantId = tenantId;
    this.workflowId = notification.workflowId();
    this.stageId = notification.stageId();
    this.recipientId = notification.recipientId();
    this.eventType = notification.eventType();
    this.message = notification.message();
    this.readAt = notification.readAt();
    this.createdAt = notification.createdAt();
  }

  public UUID id() { return id; }

  public WorkflowNotification toDomain() {
    return new WorkflowNotification(
        id, workflowId, stageId, recipientId, eventType, message, readAt, createdAt);
  }
}
