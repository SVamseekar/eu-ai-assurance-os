package os.assurance.eu.api.workflow;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalWorkflowJpaRepository
    extends JpaRepository<ApprovalWorkflowEntity, UUID> {

  List<ApprovalWorkflowEntity> findAllByTenantIdAndSystemIdOrderByCreatedAtDesc(
      UUID tenantId, UUID systemId);

  Optional<ApprovalWorkflowEntity> findByTenantIdAndSystemIdAndStatus(
      UUID tenantId, UUID systemId, WorkflowStatus status);

  List<ApprovalWorkflowEntity> findAllByTenantIdAndStatusOrderByCreatedAtDesc(
      UUID tenantId, WorkflowStatus status);

  Optional<ApprovalWorkflowEntity> findByTenantIdAndId(UUID tenantId, UUID id);
}
