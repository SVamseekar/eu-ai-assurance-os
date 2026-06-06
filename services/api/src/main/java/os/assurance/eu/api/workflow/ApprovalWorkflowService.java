package os.assurance.eu.api.workflow;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import os.assurance.eu.api.audit.AuditService;
import os.assurance.eu.api.system.AiSystem;
import os.assurance.eu.api.system.ReleaseDecision;
import os.assurance.eu.api.system.RiskClass;
import os.assurance.eu.api.tenant.TenantContext;
import os.assurance.eu.api.tenant.UserEntity;
import os.assurance.eu.api.tenant.UserJpaRepository;
import os.assurance.eu.api.tenant.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ApprovalWorkflowService {

  private final ApprovalWorkflowRepository repository;
  private final UserJpaRepository users;
  private final AuditService auditService;
  private final TenantContext tenantContext;

  public ApprovalWorkflowService(
      ApprovalWorkflowRepository repository,
      UserJpaRepository users,
      AuditService auditService,
      TenantContext tenantContext) {
    this.repository = repository;
    this.users = users;
    this.auditService = auditService;
    this.tenantContext = tenantContext;
  }

  /**
   * Opens a new approval workflow cycle for the given AI system.
   *
   * <p>Skips if:
   * <ul>
   *   <li>System is PROHIBITED</li>
   *   <li>Non-HIGH risk system with PASS decision (unless HIGH_RISK_PASS trigger)</li>
   *   <li>HIGH_RISK_PASS trigger and prior workflows already exist</li>
   * </ul>
   */
  @Transactional
  public void openCycle(AiSystem system, WorkflowTrigger trigger) {
    // Never open workflow for PROHIBITED systems
    if (system.riskClass() == RiskClass.PROHIBITED) {
      return;
    }

    // For non-HIGH risk systems, only open if decision is not PASS
    if (system.riskClass() != RiskClass.HIGH && system.releaseDecision() == ReleaseDecision.PASS) {
      return;
    }

    // For HIGH_RISK_PASS trigger, skip if prior workflows already exist
    if (trigger == WorkflowTrigger.HIGH_RISK_PASS) {
      List<ApprovalWorkflow> prior = repository.findAllBySystemId(system.id());
      if (!prior.isEmpty()) {
        return;
      }
    }

    // Supersede any existing OPEN workflow
    Optional<ApprovalWorkflow> existing = repository.findOpenBySystemId(system.id());
    existing.ifPresent(w -> {
      ApprovalWorkflow superseded = new ApprovalWorkflow(
          w.id(), w.systemId(), w.trigger(), WorkflowStatus.SUPERSEDED,
          w.stages(), w.openedAt(), Instant.now(), w.createdAt());
      repository.saveWorkflow(superseded);
    });

    // Determine stage statuses based on risk class and release decision
    List<StageStatus> stageStatuses = resolveStageStatuses(system);

    // Create the new workflow
    Instant now = Instant.now();
    ApprovalWorkflow newWorkflow = new ApprovalWorkflow(
        UUID.randomUUID(), system.id(), trigger, WorkflowStatus.OPEN,
        List.of(), now, null, now);
    ApprovalWorkflow saved = repository.saveWorkflow(newWorkflow);

    // Create stages — for HIGH risk, persist all 3 (including SKIPPED for auditability);
    // for non-HIGH, only persist active (PENDING) stages.
    boolean isHighRisk = system.riskClass() == RiskClass.HIGH;
    StageDefinition[] defs = stageDefinitions();
    for (int i = 0; i < defs.length; i++) {
      StageStatus stageStatus = stageStatuses.get(i);
      if (!isHighRisk && stageStatus == StageStatus.SKIPPED) {
        continue; // non-HIGH: don't persist SKIPPED stages
      }
      StageDefinition def = defs[i];
      ApprovalStage stage = new ApprovalStage(
          UUID.randomUUID(), saved.id(), def.order(), def.type(),
          def.requiredRole(), stageStatus, null, null, null, now);
      repository.saveStage(stage);
    }
  }

  /**
   * Approve a specific stage in a workflow.
   */
  @Transactional
  public ApprovalWorkflow approveStage(UUID workflowId, UUID stageId, String rationale) {
    UserEntity actor = resolveActor();
    ApprovalWorkflow workflow = resolveWorkflow(workflowId);
    ApprovalStage stage = resolveStage(workflow, stageId);

    // Check stage is not already completed
    if (isDone(stage.status())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT,
          "409: Stage is already in terminal status: " + stage.status());
    }

    // Check role
    checkRole(actor, stage);

    // Mark stage approved
    ApprovalStage updated = new ApprovalStage(
        stage.id(), stage.workflowId(), stage.stageOrder(), stage.stageType(),
        stage.requiredRole(), StageStatus.APPROVED,
        actor.id(), rationale, Instant.now(), stage.createdAt());
    repository.saveStage(updated);

    // Rebuild stages list with the update
    List<ApprovalStage> updatedStages = replaceStage(workflow.stages(), updated);

    // Audit
    auditService.append(workflow.systemId(), "WORKFLOW_STAGE_APPROVED",
        "ApprovalStage", stageId.toString(),
        Map.of("workflowId", workflowId.toString(), "rationale", rationale));

    // Check if all stages are done
    if (allDone(updatedStages)) {
      return closeWorkflow(workflow, WorkflowStatus.APPROVED, updatedStages);
    }

    return new ApprovalWorkflow(workflow.id(), workflow.systemId(), workflow.trigger(),
        WorkflowStatus.OPEN, updatedStages, workflow.openedAt(), null, workflow.createdAt());
  }

  /**
   * Reject a specific stage in a workflow.
   */
  @Transactional
  public ApprovalWorkflow rejectStage(UUID workflowId, UUID stageId, String rationale) {
    UserEntity actor = resolveActor();
    ApprovalWorkflow workflow = resolveWorkflow(workflowId);
    ApprovalStage stage = resolveStage(workflow, stageId);

    // Check stage is not already completed
    if (isDone(stage.status())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT,
          "409: Stage is already in terminal status: " + stage.status());
    }

    // Check role
    checkRole(actor, stage);

    // Mark stage rejected
    ApprovalStage updated = new ApprovalStage(
        stage.id(), stage.workflowId(), stage.stageOrder(), stage.stageType(),
        stage.requiredRole(), StageStatus.REJECTED,
        actor.id(), rationale, Instant.now(), stage.createdAt());
    repository.saveStage(updated);

    // Audit
    auditService.append(workflow.systemId(), "WORKFLOW_STAGE_REJECTED",
        "ApprovalStage", stageId.toString(),
        Map.of("workflowId", workflowId.toString(), "rationale", rationale));

    List<ApprovalStage> updatedStages = replaceStage(workflow.stages(), updated);
    return closeWorkflow(workflow, WorkflowStatus.REJECTED, updatedStages);
  }

  /**
   * Override a specific stage (ADMIN only).
   */
  @Transactional
  public ApprovalWorkflow overrideStage(UUID workflowId, UUID stageId, String rationale) {
    UserEntity actor = resolveActor();

    // Only ADMIN can override
    if (actor.role() != UserRole.ADMIN) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN,
          "403: Only ADMIN role can override stages");
    }

    ApprovalWorkflow workflow = resolveWorkflow(workflowId);
    ApprovalStage stage = resolveStage(workflow, stageId);

    // Mark stage overridden
    ApprovalStage updated = new ApprovalStage(
        stage.id(), stage.workflowId(), stage.stageOrder(), stage.stageType(),
        stage.requiredRole(), StageStatus.OVERRIDDEN,
        actor.id(), rationale, Instant.now(), stage.createdAt());
    repository.saveStage(updated);

    // Audit
    auditService.append(workflow.systemId(), "WORKFLOW_STAGE_OVERRIDDEN",
        "ApprovalStage", stageId.toString(),
        Map.of("workflowId", workflowId.toString(), "rationale", rationale));

    List<ApprovalStage> updatedStages = replaceStage(workflow.stages(), updated);

    if (allDone(updatedStages)) {
      return closeWorkflow(workflow, WorkflowStatus.APPROVED, updatedStages);
    }

    return new ApprovalWorkflow(workflow.id(), workflow.systemId(), workflow.trigger(),
        WorkflowStatus.OPEN, updatedStages, workflow.openedAt(), null, workflow.createdAt());
  }

  @Transactional(readOnly = true)
  public List<ApprovalWorkflow> listBySystemId(UUID systemId) {
    return repository.findAllBySystemId(systemId);
  }

  @Transactional(readOnly = true)
  public Optional<ApprovalWorkflow> findActive(UUID systemId) {
    return repository.findOpenBySystemId(systemId);
  }

  // ---- private helpers ----

  private UserEntity resolveActor() {
    UUID actorId = tenantContext.actorId();
    UUID tenantId = tenantContext.tenantId();
    return users.findByIdAndTenantId(actorId, tenantId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "404: Actor not found: " + actorId));
  }

  private ApprovalWorkflow resolveWorkflow(UUID workflowId) {
    return repository.findById(workflowId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "404: Workflow not found: " + workflowId));
  }

  private ApprovalStage resolveStage(ApprovalWorkflow workflow, UUID stageId) {
    return workflow.stages().stream()
        .filter(s -> s.id().equals(stageId))
        .findFirst()
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "404: Stage not found: " + stageId));
  }

  private void checkRole(UserEntity actor, ApprovalStage stage) {
    if (actor.role() == UserRole.ADMIN) {
      return; // ADMIN can act on any stage
    }
    if (!actor.role().name().equals(stage.requiredRole())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN,
          "403: Actor role " + actor.role() + " cannot act on stage requiring " + stage.requiredRole());
    }
  }

  private boolean isDone(StageStatus status) {
    return status == StageStatus.APPROVED
        || status == StageStatus.OVERRIDDEN
        || status == StageStatus.SKIPPED
        || status == StageStatus.REJECTED;
  }

  private boolean allDone(List<ApprovalStage> stages) {
    return stages.stream().allMatch(s -> isDone(s.status()));
  }

  private List<ApprovalStage> replaceStage(List<ApprovalStage> stages, ApprovalStage updated) {
    List<ApprovalStage> result = new ArrayList<>();
    for (ApprovalStage s : stages) {
      result.add(s.id().equals(updated.id()) ? updated : s);
    }
    return result;
  }

  private ApprovalWorkflow closeWorkflow(
      ApprovalWorkflow workflow, WorkflowStatus finalStatus, List<ApprovalStage> stages) {
    ApprovalWorkflow closed = new ApprovalWorkflow(
        workflow.id(), workflow.systemId(), workflow.trigger(), finalStatus,
        stages, workflow.openedAt(), Instant.now(), workflow.createdAt());
    repository.saveWorkflow(closed);

    auditService.append(workflow.systemId(), "WORKFLOW_CLOSED",
        "ApprovalWorkflow", workflow.id().toString(),
        Map.of("finalStatus", finalStatus.name()));

    return closed;
  }

  /**
   * Determine per-stage statuses based on risk class and release decision.
   *
   * <p>Returns a list of 3 statuses (one per stage: ENG_LEAD, COMPLIANCE, LEGAL).
   *
   * <ul>
   *   <li>HIGH + PASS: stage 1 SKIPPED, stages 2+3 PENDING</li>
   *   <li>HIGH (not PASS): all 3 PENDING</li>
   *   <li>non-HIGH BLOCKED: all 3 PENDING</li>
   *   <li>non-HIGH REVIEW: stages 1+2 PENDING, stage 3 SKIPPED</li>
   * </ul>
   */
  private List<StageStatus> resolveStageStatuses(AiSystem system) {
    if (system.riskClass() == RiskClass.HIGH) {
      if (system.releaseDecision() == ReleaseDecision.PASS) {
        // Stage 1 skipped — compliance + legal review still required
        return List.of(StageStatus.SKIPPED, StageStatus.PENDING, StageStatus.PENDING);
      } else {
        // All 3 stages required
        return List.of(StageStatus.PENDING, StageStatus.PENDING, StageStatus.PENDING);
      }
    } else {
      // non-HIGH
      if (system.releaseDecision() == ReleaseDecision.BLOCKED) {
        return List.of(StageStatus.PENDING, StageStatus.PENDING, StageStatus.PENDING);
      } else {
        // REVIEW — stages 1+2 pending, legal skipped
        return List.of(StageStatus.PENDING, StageStatus.PENDING, StageStatus.SKIPPED);
      }
    }
  }

  private record StageDefinition(int order, StageType type, String requiredRole) {}

  private StageDefinition[] stageDefinitions() {
    return new StageDefinition[]{
        new StageDefinition(1, StageType.ENG_LEAD_REVIEW, "AI_ENGINEERING_LEAD"),
        new StageDefinition(2, StageType.COMPLIANCE_REVIEW, "COMPLIANCE_OFFICER"),
        new StageDefinition(3, StageType.LEGAL_SIGNOFF, "LEGAL_COUNSEL")
    };
  }
}
