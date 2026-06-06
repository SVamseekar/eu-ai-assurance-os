package os.assurance.eu.api.workflow;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalStageJpaRepository
    extends JpaRepository<ApprovalStageEntity, UUID> {

  List<ApprovalStageEntity> findAllByTenantIdAndWorkflowIdOrderByStageOrderAsc(
      UUID tenantId, UUID workflowId);

  Optional<ApprovalStageEntity> findByTenantIdAndId(UUID tenantId, UUID id);
}
