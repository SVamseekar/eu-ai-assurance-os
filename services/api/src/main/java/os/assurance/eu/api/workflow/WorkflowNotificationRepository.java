package os.assurance.eu.api.workflow;

import java.util.List;
import java.util.UUID;
import os.assurance.eu.api.tenant.TenantContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class WorkflowNotificationRepository {
  private final WorkflowNotificationJpaRepository notifications;
  private final TenantContext tenantContext;

  public WorkflowNotificationRepository(
      WorkflowNotificationJpaRepository notifications,
      TenantContext tenantContext) {
    this.notifications = notifications;
    this.tenantContext = tenantContext;
  }

  @Transactional(readOnly = true)
  public List<WorkflowNotification> findMine() {
    return notifications
        .findAllByTenantIdAndRecipientIdOrderByCreatedAtDesc(
            tenantContext.tenantId(), tenantContext.actorId())
        .stream()
        .map(WorkflowNotificationEntity::toDomain)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<WorkflowNotification> findByWorkflowId(UUID workflowId) {
    return notifications
        .findAllByTenantIdAndWorkflowIdOrderByCreatedAtDesc(tenantContext.tenantId(), workflowId)
        .stream()
        .map(WorkflowNotificationEntity::toDomain)
        .toList();
  }

  @Transactional
  public WorkflowNotification save(WorkflowNotification notification) {
    return notifications.save(
        new WorkflowNotificationEntity(tenantContext.tenantId(), notification)).toDomain();
  }
}
