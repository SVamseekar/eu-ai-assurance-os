package os.assurance.eu.api.workflow;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowNotificationJpaRepository
    extends JpaRepository<WorkflowNotificationEntity, UUID> {

  List<WorkflowNotificationEntity> findAllByTenantIdAndRecipientIdOrderByCreatedAtDesc(
      UUID tenantId, UUID recipientId);

  List<WorkflowNotificationEntity> findAllByTenantIdAndWorkflowIdOrderByCreatedAtDesc(
      UUID tenantId, UUID workflowId);
}
