package os.assurance.eu.api.workflow;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import os.assurance.eu.api.tenant.TenantContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ApprovalWorkflowRepository {
  private final ApprovalWorkflowJpaRepository workflows;
  private final ApprovalStageJpaRepository stages;
  private final TenantContext tenantContext;

  public ApprovalWorkflowRepository(
      ApprovalWorkflowJpaRepository workflows,
      ApprovalStageJpaRepository stages,
      TenantContext tenantContext) {
    this.workflows = workflows;
    this.stages = stages;
    this.tenantContext = tenantContext;
  }

  @Transactional(readOnly = true)
  public List<ApprovalWorkflow> findAllBySystemId(UUID systemId) {
    return workflows
        .findAllByTenantIdAndSystemIdOrderByCreatedAtDesc(tenantContext.tenantId(), systemId)
        .stream()
        .map(e -> e.toDomain(stagesFor(e.id())))
        .toList();
  }

  @Transactional(readOnly = true)
  public Optional<ApprovalWorkflow> findOpenBySystemId(UUID systemId) {
    return workflows
        .findByTenantIdAndSystemIdAndStatus(tenantContext.tenantId(), systemId, WorkflowStatus.OPEN)
        .map(e -> e.toDomain(stagesFor(e.id())));
  }

  @Transactional(readOnly = true)
  public Optional<ApprovalWorkflow> findById(UUID id) {
    return workflows.findByTenantIdAndId(tenantContext.tenantId(), id)
        .map(e -> e.toDomain(stagesFor(e.id())));
  }

  @Transactional
  public ApprovalWorkflow saveWorkflow(ApprovalWorkflow workflow) {
    ApprovalWorkflowEntity saved = workflows.save(
        new ApprovalWorkflowEntity(tenantContext.tenantId(), workflow));
    return saved.toDomain(stagesFor(saved.id()));
  }

  @Transactional
  public ApprovalStage saveStage(ApprovalStage stage) {
    return stages.save(new ApprovalStageEntity(tenantContext.tenantId(), stage)).toDomain();
  }

  private List<ApprovalStage> stagesFor(UUID workflowId) {
    return stages
        .findAllByTenantIdAndWorkflowIdOrderByStageOrderAsc(tenantContext.tenantId(), workflowId)
        .stream()
        .map(ApprovalStageEntity::toDomain)
        .toList();
  }
}
