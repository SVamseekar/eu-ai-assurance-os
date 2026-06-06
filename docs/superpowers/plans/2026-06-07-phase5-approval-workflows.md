# Phase 5: Approval Workflows — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement a three-stage sequential approval workflow (Eng Lead → Compliance → Legal) for AI systems requiring human oversight, with role-based routing, admin override, audit trail integration, and a reviewer inbox in the Next.js dashboard.

**Architecture:** A new `workflow` domain package in the Spring Boot API follows the same Controller → Service → Repository → JpaRepository → Entity layer pattern as `contract/` and `eval/`. `ApprovalWorkflowService` is the state machine; existing services (`AiSystemController`, `EvalRunCompletionService`, `DataContractService`) call `openCycle()` at their respective substantial-modification points. The dashboard gains a new `/approvals` route and an Approval tab in the system details sheet.

**Tech Stack:** Java 17 / Spring Boot 3 / Flyway / H2 (test) / JPA; Next.js 16 / TypeScript / Tailwind CSS v4 / shadcn/ui / TanStack Query v5

---

## File Map

### Backend — new files
- `services/api/src/main/resources/db/migration/V8__phase5_approval_workflows.sql`
- `services/api/src/main/java/os/assurance/eu/api/workflow/WorkflowTrigger.java`
- `services/api/src/main/java/os/assurance/eu/api/workflow/WorkflowStatus.java`
- `services/api/src/main/java/os/assurance/eu/api/workflow/StageType.java`
- `services/api/src/main/java/os/assurance/eu/api/workflow/StageStatus.java`
- `services/api/src/main/java/os/assurance/eu/api/workflow/ApprovalWorkflow.java`
- `services/api/src/main/java/os/assurance/eu/api/workflow/ApprovalStage.java`
- `services/api/src/main/java/os/assurance/eu/api/workflow/ApprovalWorkflowEntity.java`
- `services/api/src/main/java/os/assurance/eu/api/workflow/ApprovalStageEntity.java`
- `services/api/src/main/java/os/assurance/eu/api/workflow/ApprovalWorkflowJpaRepository.java`
- `services/api/src/main/java/os/assurance/eu/api/workflow/ApprovalStageJpaRepository.java`
- `services/api/src/main/java/os/assurance/eu/api/workflow/ApprovalWorkflowRepository.java`
- `services/api/src/main/java/os/assurance/eu/api/workflow/StageActionRequest.java`
- `services/api/src/main/java/os/assurance/eu/api/workflow/ApprovalWorkflowService.java`
- `services/api/src/main/java/os/assurance/eu/api/workflow/ApprovalWorkflowController.java`
- `services/api/src/test/java/os/assurance/eu/api/workflow/ApprovalWorkflowServiceTest.java`

### Backend — modified files
- `services/api/src/main/java/os/assurance/eu/api/tenant/UserEntity.java` (add `id()` accessor needed by ApprovalWorkflowService)
- `services/api/src/main/java/os/assurance/eu/api/system/AiSystemController.java` (call `openCycle` after `saveWithCalculatedDecision`)
- `services/api/src/main/java/os/assurance/eu/api/eval/EvalRunCompletionService.java` (call `openCycle` after gate drops)
- `services/api/src/main/java/os/assurance/eu/api/contract/DataContractService.java` (call `openCycle` on BREACH drift event)
- `services/api/src/main/java/os/assurance/eu/api/system/EvidencePackResponse.java` (add `approvalHistory` field)
- `services/api/src/test/java/os/assurance/eu/api/ApiControllerTest.java` (add workflow approval flow integration tests)

### Frontend — new files
- `apps/dashboard/app/(dashboard)/approvals/page.tsx`
- `apps/dashboard/components/approval-workflow.tsx`
- `apps/dashboard/components/approval-action-modal.tsx`

### Frontend — modified files
- `apps/dashboard/lib/types.ts` (add `ApprovalWorkflow`, `ApprovalStage` types)
- `apps/dashboard/lib/mock-data.ts` (seed workflow data on Claims Triage AI and Support RAG Copilot)
- `apps/dashboard/lib/api.ts` (add `workflows` API methods)
- `apps/dashboard/components/sidebar.tsx` (add `/approvals` nav item with badge)
- `apps/dashboard/components/details-sheets.tsx` (add Approval tab to `SystemDetailsSheet`)
- `apps/dashboard/components/release-gate-table.tsx` (add Workflow column)
- `apps/dashboard/app/(dashboard)/layout.tsx` (add `/approvals` to `PAGE_META`)
- `apps/dashboard/context/dashboard-context.tsx` (add workflow state and actions)

---

## Task 1: Flyway migration — approval_workflows and approval_stages tables

**Files:**
- Create: `services/api/src/main/resources/db/migration/V8__phase5_approval_workflows.sql`

- [ ] **Step 1: Write the migration**

```sql
create table approval_workflows (
  id          uuid primary key,
  tenant_id   uuid not null references tenants(id),
  system_id   uuid not null references ai_systems(id),
  trigger     varchar(64) not null check (trigger in (
                'SYSTEM_CREATED', 'EVAL_REGRESSION', 'CONTRACT_BREACH',
                'RISK_RECLASSIFIED', 'HIGH_RISK_PASS')),
  status      varchar(32) not null check (status in (
                'OPEN', 'APPROVED', 'REJECTED', 'SUPERSEDED')),
  opened_at   timestamp with time zone not null,
  closed_at   timestamp with time zone,
  created_at  timestamp with time zone not null
);

create index idx_approval_workflows_tenant_system
  on approval_workflows(tenant_id, system_id);
create index idx_approval_workflows_tenant_status
  on approval_workflows(tenant_id, status);

create table approval_stages (
  id            uuid primary key,
  tenant_id     uuid not null references tenants(id),
  workflow_id   uuid not null references approval_workflows(id),
  stage_order   integer not null check (stage_order in (1, 2, 3)),
  stage_type    varchar(64) not null check (stage_type in (
                  'ENG_LEAD_REVIEW', 'COMPLIANCE_REVIEW', 'LEGAL_SIGNOFF')),
  required_role varchar(64) not null,
  status        varchar(32) not null check (status in (
                  'PENDING', 'APPROVED', 'REJECTED', 'OVERRIDDEN', 'SKIPPED')),
  actor_id      uuid references users(id),
  rationale     varchar(2048),
  acted_at      timestamp with time zone,
  created_at    timestamp with time zone not null
);

create index idx_approval_stages_workflow
  on approval_stages(workflow_id);
create index idx_approval_stages_tenant_workflow
  on approval_stages(tenant_id, workflow_id);
```

- [ ] **Step 2: Verify the migration runs cleanly**

```bash
cd services/api && mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active= &
sleep 8 && curl -s http://localhost:8080/actuator/health | grep '"status":"UP"'
kill %1
```

Expected: `"status":"UP"` printed (Flyway applies V8 on startup without errors).

- [ ] **Step 3: Commit**

```bash
git add services/api/src/main/resources/db/migration/V8__phase5_approval_workflows.sql
git commit -m "feat(workflow): add approval_workflows and approval_stages Flyway migration"
```

---

## Task 2: Domain enums and records

**Files:**
- Create: `services/api/src/main/java/os/assurance/eu/api/workflow/WorkflowTrigger.java`
- Create: `services/api/src/main/java/os/assurance/eu/api/workflow/WorkflowStatus.java`
- Create: `services/api/src/main/java/os/assurance/eu/api/workflow/StageType.java`
- Create: `services/api/src/main/java/os/assurance/eu/api/workflow/StageStatus.java`
- Create: `services/api/src/main/java/os/assurance/eu/api/workflow/ApprovalStage.java`
- Create: `services/api/src/main/java/os/assurance/eu/api/workflow/ApprovalWorkflow.java`
- Create: `services/api/src/main/java/os/assurance/eu/api/workflow/StageActionRequest.java`

- [ ] **Step 1: Write the enums**

`WorkflowTrigger.java`:
```java
package os.assurance.eu.api.workflow;

public enum WorkflowTrigger {
  SYSTEM_CREATED,
  EVAL_REGRESSION,
  CONTRACT_BREACH,
  RISK_RECLASSIFIED,
  HIGH_RISK_PASS
}
```

`WorkflowStatus.java`:
```java
package os.assurance.eu.api.workflow;

public enum WorkflowStatus {
  OPEN,
  APPROVED,
  REJECTED,
  SUPERSEDED
}
```

`StageType.java`:
```java
package os.assurance.eu.api.workflow;

public enum StageType {
  ENG_LEAD_REVIEW,
  COMPLIANCE_REVIEW,
  LEGAL_SIGNOFF
}
```

`StageStatus.java`:
```java
package os.assurance.eu.api.workflow;

public enum StageStatus {
  PENDING,
  APPROVED,
  REJECTED,
  OVERRIDDEN,
  SKIPPED
}
```

- [ ] **Step 2: Write the domain records**

`ApprovalStage.java`:
```java
package os.assurance.eu.api.workflow;

import java.time.Instant;
import java.util.UUID;

public record ApprovalStage(
    UUID id,
    UUID workflowId,
    int stageOrder,
    StageType stageType,
    String requiredRole,
    StageStatus status,
    UUID actorId,
    String rationale,
    Instant actedAt,
    Instant createdAt) {
}
```

`ApprovalWorkflow.java`:
```java
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
```

`StageActionRequest.java`:
```java
package os.assurance.eu.api.workflow;

public record StageActionRequest(String rationale) {
}
```

- [ ] **Step 3: Compile check**

```bash
cd services/api && mvn compile -q
```

Expected: BUILD SUCCESS with no errors.

- [ ] **Step 4: Commit**

```bash
git add services/api/src/main/java/os/assurance/eu/api/workflow/
git commit -m "feat(workflow): add domain enums and records for approval workflow"
```

---

## Task 3: JPA entities

**Files:**
- Create: `services/api/src/main/java/os/assurance/eu/api/workflow/ApprovalStageEntity.java`
- Create: `services/api/src/main/java/os/assurance/eu/api/workflow/ApprovalWorkflowEntity.java`

- [ ] **Step 1: Write ApprovalStageEntity**

```java
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
```

- [ ] **Step 2: Write ApprovalWorkflowEntity**

```java
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
```

- [ ] **Step 3: Compile check**

```bash
cd services/api && mvn compile -q
```

Expected: BUILD SUCCESS.

- [ ] **Step 4: Commit**

```bash
git add services/api/src/main/java/os/assurance/eu/api/workflow/
git commit -m "feat(workflow): add ApprovalWorkflowEntity and ApprovalStageEntity JPA entities"
```

---

## Task 4: JPA repositories and domain repository

**Files:**
- Create: `services/api/src/main/java/os/assurance/eu/api/workflow/ApprovalWorkflowJpaRepository.java`
- Create: `services/api/src/main/java/os/assurance/eu/api/workflow/ApprovalStageJpaRepository.java`
- Create: `services/api/src/main/java/os/assurance/eu/api/workflow/ApprovalWorkflowRepository.java`

- [ ] **Step 1: Write the JPA repositories**

`ApprovalWorkflowJpaRepository.java`:
```java
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

  Optional<ApprovalWorkflowEntity> findByTenantIdAndId(UUID tenantId, UUID id);
}
```

`ApprovalStageJpaRepository.java`:
```java
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
```

- [ ] **Step 2: Write the domain repository**

`ApprovalWorkflowRepository.java`:
```java
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
```

- [ ] **Step 3: Compile check**

```bash
cd services/api && mvn compile -q
```

Expected: BUILD SUCCESS.

- [ ] **Step 4: Commit**

```bash
git add services/api/src/main/java/os/assurance/eu/api/workflow/
git commit -m "feat(workflow): add JPA repositories and ApprovalWorkflowRepository domain seam"
```

---

## Task 5: ApprovalWorkflowService — state machine

**Files:**
- Create: `services/api/src/main/java/os/assurance/eu/api/workflow/ApprovalWorkflowService.java`
- Create: `services/api/src/test/java/os/assurance/eu/api/workflow/ApprovalWorkflowServiceTest.java`

- [ ] **Step 1: Write the failing tests**

`ApprovalWorkflowServiceTest.java`:
```java
package os.assurance.eu.api.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import os.assurance.eu.api.audit.AuditService;
import os.assurance.eu.api.system.AiSystem;
import os.assurance.eu.api.system.DataContractStatus;
import os.assurance.eu.api.system.ReleaseDecision;
import os.assurance.eu.api.system.RiskClass;
import os.assurance.eu.api.tenant.TenantContext;
import os.assurance.eu.api.tenant.UserEntity;
import os.assurance.eu.api.tenant.UserJpaRepository;
import os.assurance.eu.api.tenant.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class ApprovalWorkflowServiceTest {
  private ApprovalWorkflowRepository repository;
  private UserJpaRepository users;
  private AuditService auditService;
  private TenantContext tenantContext;
  private ApprovalWorkflowService service;

  private static final UUID TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID SYSTEM_ID = UUID.randomUUID();
  private static final UUID ACTOR_ID = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    repository = mock(ApprovalWorkflowRepository.class);
    users = mock(UserJpaRepository.class);
    auditService = mock(AuditService.class);
    tenantContext = mock(TenantContext.class);
    when(tenantContext.tenantId()).thenReturn(TENANT_ID);
    when(tenantContext.actorId()).thenReturn(ACTOR_ID);
    service = new ApprovalWorkflowService(repository, users, auditService, tenantContext);
  }

  @Test
  void opensThreeStageWorkflowForHighRiskBlocked() {
    AiSystem system = system(RiskClass.HIGH, ReleaseDecision.BLOCKED);
    when(repository.findOpenBySystemId(SYSTEM_ID)).thenReturn(Optional.empty());
    when(repository.saveWorkflow(any())).thenAnswer(i -> i.getArgument(0));
    when(repository.saveStage(any())).thenAnswer(i -> i.getArgument(0));

    service.openCycle(system, WorkflowTrigger.SYSTEM_CREATED);

    verify(repository).saveWorkflow(any());
    // 3 stages saved
    verify(repository, org.mockito.Mockito.times(3)).saveStage(any());
  }

  @Test
  void opensTwoStageWorkflowForLimitedReview() {
    AiSystem system = system(RiskClass.LIMITED, ReleaseDecision.REVIEW);
    when(repository.findOpenBySystemId(SYSTEM_ID)).thenReturn(Optional.empty());
    when(repository.saveWorkflow(any())).thenAnswer(i -> i.getArgument(0));
    when(repository.saveStage(any())).thenAnswer(i -> i.getArgument(0));

    service.openCycle(system, WorkflowTrigger.SYSTEM_CREATED);

    verify(repository, org.mockito.Mockito.times(2)).saveStage(any());
  }

  @Test
  void supersedesPriorOpenWorkflowBeforeOpeningNew() {
    ApprovalWorkflow existing = workflow(WorkflowStatus.OPEN, List.of());
    when(repository.findOpenBySystemId(SYSTEM_ID)).thenReturn(Optional.of(existing));
    when(repository.saveWorkflow(any())).thenAnswer(i -> i.getArgument(0));
    when(repository.saveStage(any())).thenAnswer(i -> i.getArgument(0));

    AiSystem system = system(RiskClass.HIGH, ReleaseDecision.REVIEW);
    service.openCycle(system, WorkflowTrigger.EVAL_REGRESSION);

    // first save is the SUPERSEDED close, second is new OPEN
    verify(repository, org.mockito.Mockito.atLeast(2)).saveWorkflow(any());
  }

  @Test
  void approveStageAdvancesWorkflow() {
    ApprovalStage pendingStage = stage(UUID.randomUUID(), 1, StageType.ENG_LEAD_REVIEW,
        "AI_ENGINEERING_LEAD", StageStatus.PENDING);
    ApprovalStage nextStage = stage(UUID.randomUUID(), 2, StageType.COMPLIANCE_REVIEW,
        "COMPLIANCE_OFFICER", StageStatus.PENDING);
    ApprovalWorkflow openWorkflow = workflow(WorkflowStatus.OPEN,
        List.of(pendingStage, nextStage));

    UserEntity actor = actor(UserRole.AI_ENGINEERING_LEAD);
    when(users.findByIdAndTenantId(ACTOR_ID, TENANT_ID)).thenReturn(Optional.of(actor));
    when(repository.findById(openWorkflow.id())).thenReturn(Optional.of(openWorkflow));
    when(repository.saveStage(any())).thenAnswer(i -> i.getArgument(0));
    when(repository.saveWorkflow(any())).thenAnswer(i -> i.getArgument(0));

    ApprovalWorkflow result = service.approveStage(
        openWorkflow.id(), pendingStage.id(), "Looks good");

    assertThat(result.status()).isEqualTo(WorkflowStatus.OPEN);
  }

  @Test
  void approveLastStageClosesWorkflowAsApproved() {
    ApprovalStage onlyStage = stage(UUID.randomUUID(), 1, StageType.ENG_LEAD_REVIEW,
        "AI_ENGINEERING_LEAD", StageStatus.PENDING);
    ApprovalWorkflow openWorkflow = workflow(WorkflowStatus.OPEN, List.of(onlyStage));

    UserEntity actor = actor(UserRole.AI_ENGINEERING_LEAD);
    when(users.findByIdAndTenantId(ACTOR_ID, TENANT_ID)).thenReturn(Optional.of(actor));
    when(repository.findById(openWorkflow.id())).thenReturn(Optional.of(openWorkflow));
    when(repository.saveStage(any())).thenAnswer(i -> i.getArgument(0));
    when(repository.saveWorkflow(any())).thenAnswer(i -> i.getArgument(0));

    ApprovalWorkflow result = service.approveStage(
        openWorkflow.id(), onlyStage.id(), "Approved");

    assertThat(result.status()).isEqualTo(WorkflowStatus.APPROVED);
  }

  @Test
  void rejectStageClosesWorkflowAsRejected() {
    ApprovalStage pendingStage = stage(UUID.randomUUID(), 1, StageType.ENG_LEAD_REVIEW,
        "AI_ENGINEERING_LEAD", StageStatus.PENDING);
    ApprovalWorkflow openWorkflow = workflow(WorkflowStatus.OPEN, List.of(pendingStage));

    UserEntity actor = actor(UserRole.AI_ENGINEERING_LEAD);
    when(users.findByIdAndTenantId(ACTOR_ID, TENANT_ID)).thenReturn(Optional.of(actor));
    when(repository.findById(openWorkflow.id())).thenReturn(Optional.of(openWorkflow));
    when(repository.saveStage(any())).thenAnswer(i -> i.getArgument(0));
    when(repository.saveWorkflow(any())).thenAnswer(i -> i.getArgument(0));

    ApprovalWorkflow result = service.rejectStage(
        openWorkflow.id(), pendingStage.id(), "Missing bias eval");

    assertThat(result.status()).isEqualTo(WorkflowStatus.REJECTED);
  }

  @Test
  void rejectsApproveFromWrongRole() {
    ApprovalStage pendingStage = stage(UUID.randomUUID(), 1, StageType.ENG_LEAD_REVIEW,
        "AI_ENGINEERING_LEAD", StageStatus.PENDING);
    ApprovalWorkflow openWorkflow = workflow(WorkflowStatus.OPEN, List.of(pendingStage));

    UserEntity actor = actor(UserRole.AUDITOR);
    when(users.findByIdAndTenantId(ACTOR_ID, TENANT_ID)).thenReturn(Optional.of(actor));
    when(repository.findById(openWorkflow.id())).thenReturn(Optional.of(openWorkflow));

    assertThatThrownBy(() ->
        service.approveStage(openWorkflow.id(), pendingStage.id(), "ok"))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("403");
  }

  @Test
  void rejectsActingOnAlreadyApprovedStage() {
    ApprovalStage doneStage = stage(UUID.randomUUID(), 1, StageType.ENG_LEAD_REVIEW,
        "AI_ENGINEERING_LEAD", StageStatus.APPROVED);
    ApprovalWorkflow openWorkflow = workflow(WorkflowStatus.OPEN, List.of(doneStage));

    UserEntity actor = actor(UserRole.AI_ENGINEERING_LEAD);
    when(users.findByIdAndTenantId(ACTOR_ID, TENANT_ID)).thenReturn(Optional.of(actor));
    when(repository.findById(openWorkflow.id())).thenReturn(Optional.of(openWorkflow));

    assertThatThrownBy(() ->
        service.approveStage(openWorkflow.id(), doneStage.id(), "again"))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("409");
  }

  @Test
  void adminCanOverrideAnyStage() {
    ApprovalStage pendingStage = stage(UUID.randomUUID(), 1, StageType.ENG_LEAD_REVIEW,
        "AI_ENGINEERING_LEAD", StageStatus.PENDING);
    ApprovalWorkflow openWorkflow = workflow(WorkflowStatus.OPEN, List.of(pendingStage));

    UserEntity actor = actor(UserRole.ADMIN);
    when(users.findByIdAndTenantId(ACTOR_ID, TENANT_ID)).thenReturn(Optional.of(actor));
    when(repository.findById(openWorkflow.id())).thenReturn(Optional.of(openWorkflow));
    when(repository.saveStage(any())).thenAnswer(i -> i.getArgument(0));
    when(repository.saveWorkflow(any())).thenAnswer(i -> i.getArgument(0));

    ApprovalWorkflow result = service.overrideStage(
        openWorkflow.id(), pendingStage.id(), "Urgent release");

    assertThat(result).isNotNull();
  }

  @Test
  void doesNotOpenWorkflowForProhibitedSystem() {
    AiSystem system = system(RiskClass.PROHIBITED, ReleaseDecision.BLOCKED);
    service.openCycle(system, WorkflowTrigger.SYSTEM_CREATED);
    verify(repository, org.mockito.Mockito.never()).saveWorkflow(any());
  }

  @Test
  void doesNotOpenWorkflowForLimitedPassSystem() {
    AiSystem system = system(RiskClass.LIMITED, ReleaseDecision.PASS);
    service.openCycle(system, WorkflowTrigger.SYSTEM_CREATED);
    verify(repository, org.mockito.Mockito.never()).saveWorkflow(any());
  }

  @Test
  void highRiskPassOpensComplianceAndLegalOnlyWhenNoPriorWorkflow() {
    AiSystem system = system(RiskClass.HIGH, ReleaseDecision.PASS);
    when(repository.findOpenBySystemId(SYSTEM_ID)).thenReturn(Optional.empty());
    when(repository.findAllBySystemId(SYSTEM_ID)).thenReturn(List.of());
    when(repository.saveWorkflow(any())).thenAnswer(i -> i.getArgument(0));
    when(repository.saveStage(any())).thenAnswer(i -> i.getArgument(0));

    service.openCycle(system, WorkflowTrigger.HIGH_RISK_PASS);

    // 3 stages: stage 1 = SKIPPED, stages 2+3 = PENDING
    verify(repository, org.mockito.Mockito.times(3)).saveStage(any());
  }

  // ---- helpers ----

  private AiSystem system(RiskClass riskClass, ReleaseDecision decision) {
    Instant now = Instant.now();
    return new AiSystem(SYSTEM_ID, "Test", "Owner", "Purpose", riskClass,
        "Basis", "EU", 80, 80, DataContractStatus.HEALTHY, decision,
        List.of(), now, now);
  }

  private ApprovalWorkflow workflow(WorkflowStatus status, List<ApprovalStage> stages) {
    Instant now = Instant.now();
    return new ApprovalWorkflow(UUID.randomUUID(), SYSTEM_ID,
        WorkflowTrigger.SYSTEM_CREATED, status, stages, now, null, now);
  }

  private ApprovalStage stage(UUID id, int order, StageType type,
      String requiredRole, StageStatus status) {
    return new ApprovalStage(id, UUID.randomUUID(), order, type,
        requiredRole, status, null, null, null, Instant.now());
  }

  private UserEntity actor(UserRole role) {
    return new UserEntity(ACTOR_ID, TENANT_ID, "actor@example.com", role, Instant.now());
  }
}
```

- [ ] **Step 2: Run tests to verify they fail**

```bash
cd services/api && mvn test -Dtest=ApprovalWorkflowServiceTest -q 2>&1 | tail -5
```

Expected: compilation failure — `ApprovalWorkflowService` does not exist yet.

- [ ] **Step 3: Write ApprovalWorkflowService**

```java
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

  @Transactional
  public void openCycle(AiSystem system, WorkflowTrigger trigger) {
    if (system.riskClass() == RiskClass.PROHIBITED) {
      return;
    }
    if (system.riskClass() != RiskClass.HIGH && system.releaseDecision() == ReleaseDecision.PASS) {
      return;
    }
    if (trigger == WorkflowTrigger.HIGH_RISK_PASS) {
      boolean hasPrior = !repository.findAllBySystemId(system.id()).isEmpty();
      if (hasPrior) {
        return;
      }
    }

    // Supersede any open workflow
    repository.findOpenBySystemId(system.id()).ifPresent(existing -> {
      Instant now = Instant.now();
      repository.saveWorkflow(new ApprovalWorkflow(
          existing.id(), existing.systemId(), existing.trigger(),
          WorkflowStatus.SUPERSEDED, existing.stages(),
          existing.openedAt(), now, existing.createdAt()));
      auditService.append(system.id(), "approval.workflow.superseded",
          "approval_workflow", existing.id().toString(),
          Map.of("newTrigger", trigger.name()));
    });

    Instant now = Instant.now();
    UUID workflowId = UUID.randomUUID();
    ApprovalWorkflow workflow = repository.saveWorkflow(new ApprovalWorkflow(
        workflowId, system.id(), trigger, WorkflowStatus.OPEN,
        List.of(), now, null, now));

    List<StageDefinition> definitions = stageDefinitions(system);
    for (StageDefinition def : definitions) {
      repository.saveStage(new ApprovalStage(
          UUID.randomUUID(), workflowId, def.order(), def.type(),
          def.requiredRole(), def.initialStatus(), null, null, null, now));
    }

    auditService.append(system.id(), "approval.workflow.opened",
        "approval_workflow", workflowId.toString(),
        Map.of("trigger", trigger.name(), "stages", definitions.size()));
  }

  @Transactional
  public ApprovalWorkflow approveStage(UUID workflowId, UUID stageId, String rationale) {
    UserEntity actor = resolveActor();
    ApprovalWorkflow workflow = requireWorkflow(workflowId);
    ApprovalStage stage = requireActiveStage(workflow, stageId);
    requireRoleOrAdmin(actor, stage.requiredRole());

    ApprovalStage approved = repository.saveStage(new ApprovalStage(
        stage.id(), stage.workflowId(), stage.stageOrder(), stage.stageType(),
        stage.requiredRole(), StageStatus.APPROVED, actor.id(),
        rationale, Instant.now(), stage.createdAt()));

    auditService.append(workflow.systemId(), "approval.stage.approved",
        "approval_stage", stageId.toString(),
        Map.of("workflowId", workflowId, "stageType", stage.stageType().name(),
            "actorRole", actor.role().name()));

    return closeOrAdvance(workflow, approved);
  }

  @Transactional
  public ApprovalWorkflow rejectStage(UUID workflowId, UUID stageId, String rationale) {
    if (rationale == null || rationale.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rationale is required for rejection");
    }
    UserEntity actor = resolveActor();
    ApprovalWorkflow workflow = requireWorkflow(workflowId);
    ApprovalStage stage = requireActiveStage(workflow, stageId);
    requireRoleOrAdmin(actor, stage.requiredRole());

    repository.saveStage(new ApprovalStage(
        stage.id(), stage.workflowId(), stage.stageOrder(), stage.stageType(),
        stage.requiredRole(), StageStatus.REJECTED, actor.id(),
        rationale, Instant.now(), stage.createdAt()));

    auditService.append(workflow.systemId(), "approval.stage.rejected",
        "approval_stage", stageId.toString(),
        Map.of("workflowId", workflowId, "stageType", stage.stageType().name(),
            "rationale", rationale));

    Instant now = Instant.now();
    ApprovalWorkflow closed = repository.saveWorkflow(new ApprovalWorkflow(
        workflow.id(), workflow.systemId(), workflow.trigger(),
        WorkflowStatus.REJECTED, workflow.stages(),
        workflow.openedAt(), now, workflow.createdAt()));

    auditService.append(workflow.systemId(), "approval.workflow.rejected",
        "approval_workflow", workflowId.toString(), Map.of());

    return closed;
  }

  @Transactional
  public ApprovalWorkflow overrideStage(UUID workflowId, UUID stageId, String rationale) {
    if (rationale == null || rationale.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rationale is required for override");
    }
    UserEntity actor = resolveActor();
    if (actor.role() != UserRole.ADMIN) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only ADMIN can override stages");
    }
    ApprovalWorkflow workflow = requireWorkflow(workflowId);
    ApprovalStage stage = requireActiveStage(workflow, stageId);

    ApprovalStage overridden = repository.saveStage(new ApprovalStage(
        stage.id(), stage.workflowId(), stage.stageOrder(), stage.stageType(),
        stage.requiredRole(), StageStatus.OVERRIDDEN, actor.id(),
        rationale, Instant.now(), stage.createdAt()));

    auditService.append(workflow.systemId(), "approval.stage.overridden",
        "approval_stage", stageId.toString(),
        Map.of("workflowId", workflowId, "stageType", stage.stageType().name(),
            "rationale", rationale));

    return closeOrAdvance(workflow, overridden);
  }

  @Transactional(readOnly = true)
  public List<ApprovalWorkflow> listBySystemId(UUID systemId) {
    return repository.findAllBySystemId(systemId);
  }

  @Transactional(readOnly = true)
  public Optional<ApprovalWorkflow> findActive(UUID systemId) {
    return repository.findOpenBySystemId(systemId);
  }

  private ApprovalWorkflow closeOrAdvance(ApprovalWorkflow workflow, ApprovalStage actedStage) {
    List<ApprovalStage> updatedStages = workflow.stages().stream()
        .map(s -> s.id().equals(actedStage.id()) ? actedStage : s)
        .toList();

    boolean allDone = updatedStages.stream()
        .allMatch(s -> s.status() == StageStatus.APPROVED
            || s.status() == StageStatus.OVERRIDDEN
            || s.status() == StageStatus.SKIPPED);

    if (allDone) {
      Instant now = Instant.now();
      ApprovalWorkflow closed = repository.saveWorkflow(new ApprovalWorkflow(
          workflow.id(), workflow.systemId(), workflow.trigger(),
          WorkflowStatus.APPROVED, updatedStages,
          workflow.openedAt(), now, workflow.createdAt()));
      auditService.append(workflow.systemId(), "approval.workflow.approved",
          "approval_workflow", workflow.id().toString(), Map.of());
      return closed;
    }

    return repository.findById(workflow.id()).orElseThrow();
  }

  private ApprovalWorkflow requireWorkflow(UUID workflowId) {
    return repository.findById(workflowId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workflow not found"));
  }

  private ApprovalStage requireActiveStage(ApprovalWorkflow workflow, UUID stageId) {
    ApprovalStage stage = workflow.stages().stream()
        .filter(s -> s.id().equals(stageId))
        .findFirst()
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Stage not found"));

    if (stage.status() != StageStatus.PENDING) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Stage is not pending");
    }

    boolean priorPending = workflow.stages().stream()
        .anyMatch(s -> s.stageOrder() < stage.stageOrder()
            && s.status() == StageStatus.PENDING);
    if (priorPending) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Prior stage not yet complete");
    }

    return stage;
  }

  private void requireRoleOrAdmin(UserEntity actor, String requiredRole) {
    if (actor.role() == UserRole.ADMIN) return;
    if (!actor.role().name().equals(requiredRole)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN,
          "Actor role " + actor.role() + " cannot act on stage requiring " + requiredRole);
    }
  }

  private UserEntity resolveActor() {
    return users.findByIdAndTenantId(tenantContext.actorId(), tenantContext.tenantId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unknown actor"));
  }

  private List<StageDefinition> stageDefinitions(AiSystem system) {
    boolean isHigh = system.riskClass() == RiskClass.HIGH;
    boolean isPass = system.releaseDecision() == ReleaseDecision.PASS;
    List<StageDefinition> defs = new ArrayList<>();

    if (isHigh && isPass) {
      defs.add(new StageDefinition(1, StageType.ENG_LEAD_REVIEW, "AI_ENGINEERING_LEAD", StageStatus.SKIPPED));
      defs.add(new StageDefinition(2, StageType.COMPLIANCE_REVIEW, "COMPLIANCE_OFFICER", StageStatus.PENDING));
      defs.add(new StageDefinition(3, StageType.LEGAL_SIGNOFF, "LEGAL_COUNSEL", StageStatus.PENDING));
    } else if (isHigh) {
      defs.add(new StageDefinition(1, StageType.ENG_LEAD_REVIEW, "AI_ENGINEERING_LEAD", StageStatus.PENDING));
      defs.add(new StageDefinition(2, StageType.COMPLIANCE_REVIEW, "COMPLIANCE_OFFICER", StageStatus.PENDING));
      defs.add(new StageDefinition(3, StageType.LEGAL_SIGNOFF, "LEGAL_COUNSEL", StageStatus.PENDING));
    } else if (system.releaseDecision() == ReleaseDecision.BLOCKED) {
      defs.add(new StageDefinition(1, StageType.ENG_LEAD_REVIEW, "AI_ENGINEERING_LEAD", StageStatus.PENDING));
      defs.add(new StageDefinition(2, StageType.COMPLIANCE_REVIEW, "COMPLIANCE_OFFICER", StageStatus.PENDING));
      defs.add(new StageDefinition(3, StageType.LEGAL_SIGNOFF, "LEGAL_COUNSEL", StageStatus.PENDING));
    } else {
      // REVIEW: 2 stages
      defs.add(new StageDefinition(1, StageType.ENG_LEAD_REVIEW, "AI_ENGINEERING_LEAD", StageStatus.PENDING));
      defs.add(new StageDefinition(2, StageType.COMPLIANCE_REVIEW, "COMPLIANCE_OFFICER", StageStatus.PENDING));
      defs.add(new StageDefinition(3, StageType.LEGAL_SIGNOFF, "LEGAL_COUNSEL", StageStatus.SKIPPED));
    }
    return defs;
  }

  private record StageDefinition(int order, StageType type, String requiredRole, StageStatus initialStatus) {}
}
```

- [ ] **Step 4: Run tests**

```bash
cd services/api && mvn test -Dtest=ApprovalWorkflowServiceTest -q 2>&1 | tail -10
```

Expected: BUILD SUCCESS, all tests pass.

- [ ] **Step 5: Run full test suite**

```bash
cd services/api && mvn test -q 2>&1 | tail -5
```

Expected: BUILD SUCCESS.

- [ ] **Step 6: Commit**

```bash
git add services/api/src/main/java/os/assurance/eu/api/workflow/ApprovalWorkflowService.java
git add services/api/src/test/java/os/assurance/eu/api/workflow/ApprovalWorkflowServiceTest.java
git commit -m "feat(workflow): add ApprovalWorkflowService state machine with full test coverage"
```

---

## Task 6: ApprovalWorkflowController

**Files:**
- Create: `services/api/src/main/java/os/assurance/eu/api/workflow/ApprovalWorkflowController.java`

- [ ] **Step 1: Write the controller**

```java
package os.assurance.eu.api.workflow;

import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/systems/{systemId}/workflows")
public class ApprovalWorkflowController {
  private final ApprovalWorkflowService service;

  public ApprovalWorkflowController(ApprovalWorkflowService service) {
    this.service = service;
  }

  @GetMapping
  public List<ApprovalWorkflow> listWorkflows(@PathVariable UUID systemId) {
    return service.listBySystemId(systemId);
  }

  @GetMapping("/active")
  public ApprovalWorkflow getActiveWorkflow(@PathVariable UUID systemId) {
    return service.findActive(systemId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "No active workflow for system"));
  }

  @PostMapping("/{workflowId}/stages/{stageId}/approve")
  public ApprovalWorkflow approveStage(
      @PathVariable UUID systemId,
      @PathVariable UUID workflowId,
      @PathVariable UUID stageId,
      @RequestBody(required = false) StageActionRequest request) {
    String rationale = request != null ? request.rationale() : null;
    return service.approveStage(workflowId, stageId, rationale);
  }

  @PostMapping("/{workflowId}/stages/{stageId}/reject")
  public ApprovalWorkflow rejectStage(
      @PathVariable UUID systemId,
      @PathVariable UUID workflowId,
      @PathVariable UUID stageId,
      @RequestBody StageActionRequest request) {
    return service.rejectStage(workflowId, stageId, request.rationale());
  }

  @PostMapping("/{workflowId}/stages/{stageId}/override")
  public ApprovalWorkflow overrideStage(
      @PathVariable UUID systemId,
      @PathVariable UUID workflowId,
      @PathVariable UUID stageId,
      @RequestBody StageActionRequest request) {
    return service.overrideStage(workflowId, stageId, request.rationale());
  }
}
```

- [ ] **Step 2: Compile check**

```bash
cd services/api && mvn compile -q
```

Expected: BUILD SUCCESS.

- [ ] **Step 3: Commit**

```bash
git add services/api/src/main/java/os/assurance/eu/api/workflow/ApprovalWorkflowController.java
git commit -m "feat(workflow): add ApprovalWorkflowController REST endpoints"
```

---

## Task 7: Wire openCycle into AiSystemController, EvalRunCompletionService, DataContractService

**Files:**
- Modify: `services/api/src/main/java/os/assurance/eu/api/tenant/UserEntity.java`
- Modify: `services/api/src/main/java/os/assurance/eu/api/system/AiSystemController.java`
- Modify: `services/api/src/main/java/os/assurance/eu/api/eval/EvalRunCompletionService.java`
- Modify: `services/api/src/main/java/os/assurance/eu/api/contract/DataContractService.java`

- [ ] **Step 1: Add id() accessor to UserEntity**

`ApprovalWorkflowService` calls `actor.id()` to record which actor acted on a stage. `UserEntity` currently only exposes `role()`. Add:

```java
public UUID id() {
  return id;
}
```

- [ ] **Step 2: Wire into AiSystemController**

Add `ApprovalWorkflowService` as a constructor dependency and call `openCycle` from `createSystem` and `classifyRisk`.

In `AiSystemController.java`, add the import and field:
```java
import os.assurance.eu.api.workflow.ApprovalWorkflowService;
import os.assurance.eu.api.workflow.WorkflowTrigger;
```

Add `approvalWorkflowService` to the constructor and field:
```java
private final ApprovalWorkflowService approvalWorkflowService;

public AiSystemController(
    AiSystemRepository repository,
    ReleaseGateService releaseGateService,
    AuditService auditService,
    DataContractService dataContractService,
    ApprovalWorkflowService approvalWorkflowService) {
  this.repository = repository;
  this.releaseGateService = releaseGateService;
  this.auditService = auditService;
  this.dataContractService = dataContractService;
  this.approvalWorkflowService = approvalWorkflowService;
}
```

At the end of `createSystem`, after the `auditService.append(...)` call:
```java
approvalWorkflowService.openCycle(saved, WorkflowTrigger.SYSTEM_CREATED);
```

At the end of `classifyRisk`, after the `auditService.append(...)` call:
```java
approvalWorkflowService.openCycle(saved, WorkflowTrigger.RISK_RECLASSIFIED);
```

- [ ] **Step 2: Wire into EvalRunCompletionService**

Add `ApprovalWorkflowService` as a constructor dependency. At the end of the `complete()` method, after `evalRunMetrics.completed(source)`, add:

```java
import os.assurance.eu.api.workflow.ApprovalWorkflowService;
import os.assurance.eu.api.workflow.WorkflowTrigger;
```

Add field and constructor param:
```java
private final ApprovalWorkflowService approvalWorkflowService;
```

After `evalRunMetrics.completed(source);` in `complete()`:
```java
approvalWorkflowService.openCycle(updated, WorkflowTrigger.EVAL_REGRESSION);
```

- [ ] **Step 3: Wire into DataContractService**

Add `ApprovalWorkflowService` as a constructor dependency. In `createDriftEvent()`, after `recalculateContractAndSystemStatus(contract.id())`, look up the updated system and call `openCycle` only when the new system status is BREACH:

```java
import os.assurance.eu.api.workflow.ApprovalWorkflowService;
import os.assurance.eu.api.workflow.WorkflowTrigger;
```

Add field and constructor param. In `createDriftEvent()`, after the existing `recalculateContractAndSystemStatus` call:
```java
if (saved.severity() == DriftSeverity.BREACH) {
  systems.findById(contract.systemId()).ifPresent(sys ->
      approvalWorkflowService.openCycle(sys, WorkflowTrigger.CONTRACT_BREACH));
}
```

- [ ] **Step 4: Run full test suite**

```bash
cd services/api && mvn test -q 2>&1 | tail -5
```

Expected: BUILD SUCCESS.

- [ ] **Step 5: Commit**

```bash
git add services/api/src/main/java/os/assurance/eu/api/system/AiSystemController.java
git add services/api/src/main/java/os/assurance/eu/api/eval/EvalRunCompletionService.java
git add services/api/src/main/java/os/assurance/eu/api/contract/DataContractService.java
git commit -m "feat(workflow): wire openCycle into system, eval, and contract substantial-modification points"
```

---

## Task 8: Evidence pack — add approvalHistory field

**Files:**
- Modify: `services/api/src/main/java/os/assurance/eu/api/system/EvidencePackResponse.java`
- Modify: `services/api/src/main/java/os/assurance/eu/api/system/AiSystemController.java`

- [ ] **Step 1: Update EvidencePackResponse**

Read the current file first, then add `approvalHistory` as the last field:

```java
public record EvidencePackResponse(
    UUID systemId,
    Instant generatedAt,
    ReleaseDecision decision,
    Map<String, Object> riskClassification,
    List<Map<String, Object>> evidenceSummary,
    List<Map<String, Object>> evalSummary,
    List<Map<String, Object>> dataContracts,
    List<Map<String, Object>> openGaps,
    List<AuditEvent> auditEvents,
    List<Map<String, Object>> approvalHistory) {
}
```

- [ ] **Step 2: Update getEvidencePack in AiSystemController**

Add `ApprovalWorkflowService` dependency (already added in Task 7). In `getEvidencePack`, before the return statement, build the approval history:

```java
import os.assurance.eu.api.workflow.ApprovalWorkflow;
import os.assurance.eu.api.workflow.ApprovalStage;
```

In `getEvidencePack()`, replace the final return with:
```java
List<Map<String, Object>> approvalHistory = approvalWorkflowService
    .listBySystemId(system.id()).stream()
    .map(this::workflowEvidence)
    .toList();

return new EvidencePackResponse(
    system.id(),
    Instant.now(),
    releaseGate.decision(),
    Map.of(
        "riskClass", system.riskClass(),
        "basis", system.riskBasis(),
        "deploymentRegion", system.deploymentRegion()),
    List.of(Map.of(
        "coverage", system.evidenceCoverage(),
        "openGaps", system.openGaps())),
    List.of(Map.of(
        "latestScore", system.evalScore(),
        "releaseDecision", system.releaseDecision())),
    dataContracts,
    List.of(),
    auditEvents,
    approvalHistory);
```

Add the helper method:
```java
private Map<String, Object> workflowEvidence(ApprovalWorkflow workflow) {
  List<Map<String, Object>> stages = workflow.stages().stream().map(stage -> {
    Map<String, Object> s = new java.util.LinkedHashMap<>();
    s.put("stageType", stage.stageType());
    s.put("status", stage.status());
    s.put("actorId", stage.actorId());
    s.put("rationale", stage.rationale());
    s.put("actedAt", stage.actedAt());
    return s;
  }).toList();
  Map<String, Object> w = new java.util.LinkedHashMap<>();
  w.put("workflowId", workflow.id());
  w.put("trigger", workflow.trigger());
  w.put("status", workflow.status());
  w.put("openedAt", workflow.openedAt());
  w.put("closedAt", workflow.closedAt());
  w.put("stages", stages);
  return w;
}
```

- [ ] **Step 3: Run full test suite**

```bash
cd services/api && mvn test -q 2>&1 | tail -5
```

Expected: BUILD SUCCESS.

- [ ] **Step 4: Commit**

```bash
git add services/api/src/main/java/os/assurance/eu/api/system/EvidencePackResponse.java
git add services/api/src/main/java/os/assurance/eu/api/system/AiSystemController.java
git commit -m "feat(workflow): add approvalHistory to evidence pack response"
```

---

## Task 9: Integration tests for approval workflow API

**Files:**
- Modify: `services/api/src/test/java/os/assurance/eu/api/ApiControllerTest.java`

- [ ] **Step 1: Add seeded users for Legal and Admin roles in `seedTestActors()`**

```java
private static final String LEGAL_ACTOR_ID = "00000000-0000-0000-0000-000000000104";
private static final String ADMIN_ACTOR_ID  = "00000000-0000-0000-0000-000000000105";
```

In `seedTestActors()`:
```java
users.findById(UUID.fromString(LEGAL_ACTOR_ID))
    .orElseGet(() -> users.save(new UserEntity(
        UUID.fromString(LEGAL_ACTOR_ID),
        UUID.fromString(DEFAULT_TENANT_ID),
        "legal@example.com",
        UserRole.LEGAL_COUNSEL,
        now)));
users.findById(UUID.fromString(ADMIN_ACTOR_ID))
    .orElseGet(() -> users.save(new UserEntity(
        UUID.fromString(ADMIN_ACTOR_ID),
        UUID.fromString(DEFAULT_TENANT_ID),
        "admin@example.com",
        UserRole.ADMIN,
        now)));
```

- [ ] **Step 2: Add workflow integration tests**

```java
@Test
void creatingReviewSystemOpensWorkflow() throws Exception {
  String systemId = createSystem(); // riskClass=limited, review decision

  mockMvc.perform(get("/api/v1/systems/{id}/workflows/active", systemId)
          .header("X-Actor-Id", DEFAULT_ACTOR_ID))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value("OPEN"))
      .andExpect(jsonPath("$.stages").isArray());
}

@Test
void approveAndRejectFlowThroughStages() throws Exception {
  String systemId = createHighRiskBlockedSystem();

  // Get active workflow
  MvcResult wfResult = mockMvc.perform(get("/api/v1/systems/{id}/workflows/active", systemId)
          .header("X-Actor-Id", ENGINEERING_ACTOR_ID))
      .andExpect(status().isOk())
      .andReturn();
  JsonNode wf = read(wfResult);
  String workflowId = wf.get("id").asText();
  String stage1Id = wf.get("stages").get(0).get("id").asText();

  // Approve stage 1 as engineering lead
  mockMvc.perform(post("/api/v1/systems/{sId}/workflows/{wId}/stages/{stId}/approve",
              systemId, workflowId, stage1Id)
          .header("X-Actor-Id", ENGINEERING_ACTOR_ID)
          .contentType(MediaType.APPLICATION_JSON)
          .content("{\"rationale\": \"Eval scores reviewed and acceptable\"}"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value("OPEN"));

  // Reject stage 2 as compliance officer
  MvcResult wfResult2 = mockMvc.perform(get("/api/v1/systems/{id}/workflows/active", systemId)
          .header("X-Actor-Id", DEFAULT_ACTOR_ID))
      .andReturn();
  String stage2Id = read(wfResult2).get("stages").get(1).get("id").asText();

  mockMvc.perform(post("/api/v1/systems/{sId}/workflows/{wId}/stages/{stId}/reject",
              systemId, workflowId, stage2Id)
          .header("X-Actor-Id", DEFAULT_ACTOR_ID)
          .contentType(MediaType.APPLICATION_JSON)
          .content("{\"rationale\": \"Bias eval missing for protected categories\"}"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value("REJECTED"));
}

@Test
void wrongRoleCannotApproveStage() throws Exception {
  String systemId = createHighRiskBlockedSystem();
  MvcResult wfResult = mockMvc.perform(get("/api/v1/systems/{id}/workflows/active", systemId)
          .header("X-Actor-Id", DEFAULT_ACTOR_ID))
      .andExpect(status().isOk())
      .andReturn();
  String workflowId = read(wfResult).get("id").asText();
  String stage1Id = read(wfResult).get("stages").get(0).get("id").asText();

  // DEFAULT_ACTOR_ID is COMPLIANCE_OFFICER; stage 1 requires AI_ENGINEERING_LEAD
  mockMvc.perform(post("/api/v1/systems/{sId}/workflows/{wId}/stages/{stId}/approve",
              systemId, workflowId, stage1Id)
          .header("X-Actor-Id", DEFAULT_ACTOR_ID)
          .contentType(MediaType.APPLICATION_JSON)
          .content("{\"rationale\": \"ok\"}"))
      .andExpect(status().isForbidden());
}

@Test
void adminCanOverrideAnyStage() throws Exception {
  String systemId = createHighRiskBlockedSystem();
  MvcResult wfResult = mockMvc.perform(get("/api/v1/systems/{id}/workflows/active", systemId)
          .header("X-Actor-Id", ADMIN_ACTOR_ID))
      .andExpect(status().isOk())
      .andReturn();
  String workflowId = read(wfResult).get("id").asText();
  String stage1Id = read(wfResult).get("stages").get(0).get("id").asText();

  mockMvc.perform(post("/api/v1/systems/{sId}/workflows/{wId}/stages/{stId}/override",
              systemId, workflowId, stage1Id)
          .header("X-Actor-Id", ADMIN_ACTOR_ID)
          .contentType(MediaType.APPLICATION_JSON)
          .content("{\"rationale\": \"Emergency compliance override - board approval obtained\"}"))
      .andExpect(status().isOk());
}

@Test
void evidencePackIncludesApprovalHistory() throws Exception {
  String systemId = createSystem();
  mockMvc.perform(get("/api/v1/systems/{id}/evidence-pack", systemId)
          .header("X-Actor-Id", DEFAULT_ACTOR_ID))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.approvalHistory").isArray());
}
```

Add helper:
```java
private String createHighRiskBlockedSystem() throws Exception {
  MvcResult result = mockMvc.perform(post("/api/v1/systems")
          .contentType(MediaType.APPLICATION_JSON)
          .content("""
              {
                "name": "High Risk Blocked System",
                "owner": "Risk Team",
                "purpose": "Insurance claims triage",
                "riskClass": "high",
                "riskBasis": "Art. 6(2) Annex III",
                "deploymentRegion": "EU",
                "evidenceCoverage": 50,
                "evalScore": 70,
                "dataContractStatus": "breach",
                "openGaps": ["Human oversight SOP missing"]
              }
              """))
      .andExpect(status().isCreated())
      .andReturn();
  return read(result).get("id").asText();
}
```

- [ ] **Step 3: Run all tests**

```bash
cd services/api && mvn test -q 2>&1 | tail -10
```

Expected: BUILD SUCCESS, all tests pass.

- [ ] **Step 4: Commit**

```bash
git add services/api/src/test/java/os/assurance/eu/api/ApiControllerTest.java
git commit -m "test(workflow): add integration tests for approval workflow API endpoints"
```

---

## Task 10: Frontend types, mock data, and API client

**Files:**
- Modify: `apps/dashboard/lib/types.ts`
- Modify: `apps/dashboard/lib/mock-data.ts`
- Modify: `apps/dashboard/lib/api.ts`

- [ ] **Step 1: Add workflow types to types.ts**

Append to `apps/dashboard/lib/types.ts`:

```typescript
export type WorkflowStatus = "OPEN" | "APPROVED" | "REJECTED" | "SUPERSEDED";
export type WorkflowTrigger =
  | "SYSTEM_CREATED"
  | "EVAL_REGRESSION"
  | "CONTRACT_BREACH"
  | "RISK_RECLASSIFIED"
  | "HIGH_RISK_PASS";
export type StageStatus = "PENDING" | "APPROVED" | "REJECTED" | "OVERRIDDEN" | "SKIPPED";
export type StageType = "ENG_LEAD_REVIEW" | "COMPLIANCE_REVIEW" | "LEGAL_SIGNOFF";

export interface ApprovalStage {
  id: string;
  workflowId: string;
  stageOrder: number;
  stageType: StageType;
  requiredRole: string;
  status: StageStatus;
  actorId: string | null;
  rationale: string | null;
  actedAt: string | null;
  createdAt: string;
}

export interface ApprovalWorkflow {
  id: string;
  systemId: string;
  trigger: WorkflowTrigger;
  status: WorkflowStatus;
  stages: ApprovalStage[];
  openedAt: string;
  closedAt: string | null;
  createdAt: string;
}
```

- [ ] **Step 2: Seed mock workflow data in mock-data.ts**

Add after the existing mock data exports:

```typescript
export const MOCK_WORKFLOWS: Record<string, ApprovalWorkflow[]> = {
  "mock-sys-001": [
    {
      id: "mock-wf-001",
      systemId: "mock-sys-001",
      trigger: "SYSTEM_CREATED",
      status: "OPEN",
      openedAt: d(1, 3),
      closedAt: null,
      createdAt: d(1, 3),
      stages: [
        {
          id: "mock-stage-001",
          workflowId: "mock-wf-001",
          stageOrder: 1,
          stageType: "ENG_LEAD_REVIEW",
          requiredRole: "AI_ENGINEERING_LEAD",
          status: "PENDING",
          actorId: null,
          rationale: null,
          actedAt: null,
          createdAt: d(1, 3),
        },
        {
          id: "mock-stage-002",
          workflowId: "mock-wf-001",
          stageOrder: 2,
          stageType: "COMPLIANCE_REVIEW",
          requiredRole: "COMPLIANCE_OFFICER",
          status: "PENDING",
          actorId: null,
          rationale: null,
          actedAt: null,
          createdAt: d(1, 3),
        },
        {
          id: "mock-stage-003",
          workflowId: "mock-wf-001",
          stageOrder: 3,
          stageType: "LEGAL_SIGNOFF",
          requiredRole: "LEGAL_COUNSEL",
          status: "PENDING",
          actorId: null,
          rationale: null,
          actedAt: null,
          createdAt: d(1, 3),
        },
      ],
    },
  ],
  "mock-sys-004": [
    {
      id: "mock-wf-004",
      systemId: "mock-sys-004",
      trigger: "SYSTEM_CREATED",
      status: "APPROVED",
      openedAt: d(30),
      closedAt: d(28),
      createdAt: d(30),
      stages: [
        {
          id: "mock-stage-010",
          workflowId: "mock-wf-004",
          stageOrder: 1,
          stageType: "ENG_LEAD_REVIEW",
          requiredRole: "AI_ENGINEERING_LEAD",
          status: "APPROVED",
          actorId: "00000000-0000-0000-0000-000000000102",
          rationale: "Eval metrics all green. Faithfulness 0.91, bias pass rate 0.94.",
          actedAt: d(29),
          createdAt: d(30),
        },
        {
          id: "mock-stage-011",
          workflowId: "mock-wf-004",
          stageOrder: 2,
          stageType: "COMPLIANCE_REVIEW",
          requiredRole: "COMPLIANCE_OFFICER",
          status: "APPROVED",
          actorId: "00000000-0000-0000-0000-000000000101",
          rationale: "Evidence coverage 88%. Art. 52 transparency obligation noted in open gaps — acceptable for LIMITED risk.",
          actedAt: d(28),
          createdAt: d(30),
        },
        {
          id: "mock-stage-012",
          workflowId: "mock-wf-004",
          stageOrder: 3,
          stageType: "LEGAL_SIGNOFF",
          requiredRole: "LEGAL_COUNSEL",
          status: "SKIPPED",
          actorId: null,
          rationale: null,
          actedAt: null,
          createdAt: d(30),
        },
      ],
    },
  ],
};
```

Add `ApprovalWorkflow` to the import in mock-data.ts:
```typescript
import type { AiSystem, DataContract, DriftEvent, AuditEvent, ApprovalWorkflow } from "./types";
```

- [ ] **Step 3: Add workflows API methods to api.ts**

```typescript
workflows: {
  list: (systemId: string) =>
    request<ApprovalWorkflow[]>(`/systems/${systemId}/workflows`),
  active: (systemId: string) =>
    request<ApprovalWorkflow>(`/systems/${systemId}/workflows/active`),
  approve: (systemId: string, workflowId: string, stageId: string, rationale?: string) =>
    request<ApprovalWorkflow>(
      `/systems/${systemId}/workflows/${workflowId}/stages/${stageId}/approve`,
      { method: "POST", body: JSON.stringify({ rationale }) }
    ),
  reject: (systemId: string, workflowId: string, stageId: string, rationale: string) =>
    request<ApprovalWorkflow>(
      `/systems/${systemId}/workflows/${workflowId}/stages/${stageId}/reject`,
      { method: "POST", body: JSON.stringify({ rationale }) }
    ),
  override: (systemId: string, workflowId: string, stageId: string, rationale: string) =>
    request<ApprovalWorkflow>(
      `/systems/${systemId}/workflows/${workflowId}/stages/${stageId}/override`,
      { method: "POST", body: JSON.stringify({ rationale }) }
    ),
},
```

Add `ApprovalWorkflow` to the import at the top of `api.ts`.

- [ ] **Step 4: TypeScript check**

```bash
cd apps/dashboard && npx tsc --noEmit 2>&1 | head -20
```

Expected: no errors.

- [ ] **Step 5: Commit**

```bash
git add apps/dashboard/lib/types.ts apps/dashboard/lib/mock-data.ts apps/dashboard/lib/api.ts
git commit -m "feat(dashboard): add ApprovalWorkflow types, mock data, and API client methods"
```

---

## Task 11: ApprovalActionModal component

**Files:**
- Create: `apps/dashboard/components/approval-action-modal.tsx`

- [ ] **Step 1: Write the modal**

```tsx
"use client";

import { useState } from "react";
import type { ApprovalStage } from "@/lib/types";

interface ApprovalActionModalProps {
  stage: ApprovalStage;
  action: "approve" | "reject" | "override";
  onConfirm: (rationale: string) => void;
  onClose: () => void;
}

const STAGE_LABELS: Record<string, string> = {
  ENG_LEAD_REVIEW: "Engineering Lead Review",
  COMPLIANCE_REVIEW: "Compliance Review",
  LEGAL_SIGNOFF: "Legal Sign-off",
};

const ACTION_CONFIG = {
  approve: {
    title: "Approve Stage",
    buttonLabel: "Approve",
    buttonClass: "bg-emerald-600 hover:bg-emerald-700 text-white",
    rationaleRequired: false,
  },
  reject: {
    title: "Reject Stage",
    buttonLabel: "Reject",
    buttonClass: "bg-red-600 hover:bg-red-700 text-white",
    rationaleRequired: true,
  },
  override: {
    title: "Override Stage",
    buttonLabel: "Override",
    buttonClass: "bg-amber-600 hover:bg-amber-700 text-white",
    rationaleRequired: true,
  },
};

export function ApprovalActionModal({
  stage,
  action,
  onConfirm,
  onClose,
}: ApprovalActionModalProps) {
  const [rationale, setRationale] = useState("");
  const config = ACTION_CONFIG[action];
  const canSubmit = !config.rationaleRequired || rationale.trim().length > 0;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
      <div className="bg-card border border-border rounded-xl shadow-xl w-full max-w-md p-6 space-y-4">
        <div>
          <h3 className="text-sm font-semibold text-foreground">{config.title}</h3>
          <p className="text-xs text-muted-foreground mt-1">
            Stage: {STAGE_LABELS[stage.stageType]}
          </p>
        </div>

        <div className="space-y-1.5">
          <label className="text-xs font-medium text-foreground">
            Rationale{config.rationaleRequired ? " (required)" : " (optional)"}
          </label>
          <textarea
            className="w-full text-xs border border-border rounded-lg p-2.5 bg-background text-foreground placeholder:text-muted-foreground resize-none focus:outline-none focus:ring-2 focus:ring-ring/50"
            rows={4}
            placeholder={
              action === "override"
                ? "Explain the business or regulatory justification for this override..."
                : action === "reject"
                ? "Describe what must be resolved before this system can proceed..."
                : "Optional notes for the audit record..."
            }
            value={rationale}
            onChange={(e) => setRationale(e.target.value)}
          />
        </div>

        <div className="flex gap-2 justify-end">
          <button
            onClick={onClose}
            className="text-xs px-3 py-1.5 rounded-lg border border-border text-muted-foreground hover:bg-muted/50 transition-colors"
          >
            Cancel
          </button>
          <button
            onClick={() => onConfirm(rationale)}
            disabled={!canSubmit}
            className={`text-xs px-3 py-1.5 rounded-lg font-medium transition-colors disabled:opacity-40 disabled:cursor-not-allowed ${config.buttonClass}`}
          >
            {config.buttonLabel}
          </button>
        </div>
      </div>
    </div>
  );
}
```

- [ ] **Step 2: TypeScript check**

```bash
cd apps/dashboard && npx tsc --noEmit 2>&1 | head -20
```

Expected: no errors.

- [ ] **Step 3: Commit**

```bash
git add apps/dashboard/components/approval-action-modal.tsx
git commit -m "feat(dashboard): add ApprovalActionModal for approve/reject/override actions"
```

---

## Task 12: ApprovalWorkflow component (approval tab content + stage timeline)

**Files:**
- Create: `apps/dashboard/components/approval-workflow.tsx`

- [ ] **Step 1: Write the component**

```tsx
"use client";

import { useState } from "react";
import { cn } from "@/lib/utils";
import type { ApprovalWorkflow, ApprovalStage, StageStatus } from "@/lib/types";
import { ApprovalActionModal } from "./approval-action-modal";
import { CheckCircle2, XCircle, AlertTriangle, Clock, SkipForward } from "lucide-react";

interface ApprovalWorkflowPanelProps {
  workflows: ApprovalWorkflow[];
  activeWorkflow: ApprovalWorkflow | null;
  activeRole: string;
  onApprove: (workflowId: string, stageId: string, rationale: string) => void;
  onReject: (workflowId: string, stageId: string, rationale: string) => void;
  onOverride: (workflowId: string, stageId: string, rationale: string) => void;
}

const STAGE_LABELS: Record<string, string> = {
  ENG_LEAD_REVIEW: "Engineering Lead Review",
  COMPLIANCE_REVIEW: "Compliance Review",
  LEGAL_SIGNOFF: "Legal Sign-off",
};

const ROLE_TO_STAGE: Record<string, string> = {
  "actor-marco": "AI_ENGINEERING_LEAD",
  "actor-priya": "COMPLIANCE_OFFICER",
  "actor-leo": "LEGAL_COUNSEL",
  "actor-sofia": "COMPLIANCE_OFFICER",
};

function StageStatusIcon({ status }: { status: StageStatus }) {
  if (status === "APPROVED") return <CheckCircle2 className="w-4 h-4 text-emerald-500" />;
  if (status === "REJECTED") return <XCircle className="w-4 h-4 text-red-500" />;
  if (status === "OVERRIDDEN") return <AlertTriangle className="w-4 h-4 text-amber-500" />;
  if (status === "SKIPPED") return <SkipForward className="w-4 h-4 text-muted-foreground/40" />;
  return <Clock className="w-4 h-4 text-muted-foreground" />;
}

function isActionableStage(stage: ApprovalStage, stages: ApprovalStage[], activeRole: string): boolean {
  if (stage.status !== "PENDING") return false;
  const priorPending = stages.some(
    (s) => s.stageOrder < stage.stageOrder && s.status === "PENDING"
  );
  if (priorPending) return false;
  const actorRole = ROLE_TO_STAGE[activeRole];
  return actorRole === stage.requiredRole || activeRole === "actor-admin";
}

export function ApprovalWorkflowPanel({
  workflows,
  activeWorkflow,
  activeRole,
  onApprove,
  onReject,
  onOverride,
}: ApprovalWorkflowPanelProps) {
  const [modal, setModal] = useState<{
    stage: ApprovalStage;
    action: "approve" | "reject" | "override";
    workflowId: string;
  } | null>(null);

  if (!activeWorkflow && workflows.length === 0) {
    return (
      <p className="text-xs text-muted-foreground py-4 text-center">
        No approval workflow for this system.
      </p>
    );
  }

  return (
    <div className="space-y-4">
      {activeWorkflow && (
        <div className="border border-border rounded-xl p-4 space-y-3">
          <div className="flex items-center justify-between">
            <p className="text-xs font-semibold text-foreground uppercase tracking-wider">
              Active Workflow
            </p>
            <span className="text-[10px] font-medium px-2 py-0.5 rounded-full bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400">
              OPEN
            </span>
          </div>
          <div className="space-y-2">
            {activeWorkflow.stages.map((stage) => {
              const actionable = isActionableStage(stage, activeWorkflow.stages, activeRole);
              const isAdmin = activeRole === "actor-admin";
              return (
                <div
                  key={stage.id}
                  className={cn(
                    "flex items-center justify-between rounded-lg px-3 py-2 border",
                    stage.status === "PENDING" && !actionable
                      ? "border-border bg-muted/20 opacity-50"
                      : "border-border bg-muted/30"
                  )}
                >
                  <div className="flex items-center gap-2">
                    <StageStatusIcon status={stage.status} />
                    <span className="text-xs text-foreground">
                      {STAGE_LABELS[stage.stageType]}
                    </span>
                  </div>
                  {actionable && (
                    <div className="flex gap-1.5">
                      <button
                        onClick={() => setModal({ stage, action: "approve", workflowId: activeWorkflow.id })}
                        className="text-[10px] px-2 py-1 rounded bg-emerald-600 hover:bg-emerald-700 text-white font-medium"
                      >
                        Approve
                      </button>
                      <button
                        onClick={() => setModal({ stage, action: "reject", workflowId: activeWorkflow.id })}
                        className="text-[10px] px-2 py-1 rounded bg-red-600 hover:bg-red-700 text-white font-medium"
                      >
                        Reject
                      </button>
                    </div>
                  )}
                  {!actionable && stage.status === "PENDING" && isAdmin && (
                    <button
                      onClick={() => setModal({ stage, action: "override", workflowId: activeWorkflow.id })}
                      className="text-[10px] px-2 py-1 rounded bg-amber-600 hover:bg-amber-700 text-white font-medium"
                    >
                      Override
                    </button>
                  )}
                  {stage.rationale && (
                    <p className="text-[10px] text-muted-foreground ml-2 truncate max-w-32" title={stage.rationale}>
                      {stage.rationale}
                    </p>
                  )}
                </div>
              );
            })}
          </div>
        </div>
      )}

      {workflows.filter((w) => w.status !== "OPEN").length > 0 && (
        <div className="space-y-2">
          <p className="text-[10px] font-semibold text-muted-foreground uppercase tracking-wider">
            History
          </p>
          {workflows
            .filter((w) => w.status !== "OPEN")
            .map((w) => (
              <div key={w.id} className="border border-border rounded-xl p-3 space-y-2">
                <div className="flex items-center justify-between">
                  <span className="text-[10px] text-muted-foreground">{w.trigger.replace(/_/g, " ")}</span>
                  <span
                    className={cn(
                      "text-[10px] font-medium px-2 py-0.5 rounded-full",
                      w.status === "APPROVED"
                        ? "bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400"
                        : w.status === "REJECTED"
                        ? "bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400"
                        : "bg-muted text-muted-foreground"
                    )}
                  >
                    {w.status}
                  </span>
                </div>
                <div className="space-y-1">
                  {w.stages.map((stage) => (
                    <div key={stage.id} className="flex items-start gap-2">
                      <StageStatusIcon status={stage.status} />
                      <div className="flex-1 min-w-0">
                        <p className="text-[10px] text-foreground">{STAGE_LABELS[stage.stageType]}</p>
                        {stage.rationale && (
                          <p className="text-[10px] text-muted-foreground mt-0.5 leading-relaxed">
                            {stage.rationale}
                          </p>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            ))}
        </div>
      )}

      {modal && (
        <ApprovalActionModal
          stage={modal.stage}
          action={modal.action}
          onClose={() => setModal(null)}
          onConfirm={(rationale) => {
            if (modal.action === "approve") onApprove(modal.workflowId, modal.stage.id, rationale);
            else if (modal.action === "reject") onReject(modal.workflowId, modal.stage.id, rationale);
            else onOverride(modal.workflowId, modal.stage.id, rationale);
            setModal(null);
          }}
        />
      )}
    </div>
  );
}
```

- [ ] **Step 2: TypeScript check**

```bash
cd apps/dashboard && npx tsc --noEmit 2>&1 | head -20
```

Expected: no errors.

- [ ] **Step 3: Commit**

```bash
git add apps/dashboard/components/approval-workflow.tsx
git commit -m "feat(dashboard): add ApprovalWorkflowPanel component with stage timeline and actions"
```

---

## Task 13: Add Approval tab to SystemDetailsSheet and Workflow column to ReleaseGateTable

**Files:**
- Modify: `apps/dashboard/components/details-sheets.tsx`
- Modify: `apps/dashboard/components/release-gate-table.tsx`

- [ ] **Step 1: Add Approval tab to SystemDetailsSheet**

In `details-sheets.tsx`, import the panel and mock data:

```typescript
import { ApprovalWorkflowPanel } from "./approval-workflow";
import { MOCK_WORKFLOWS } from "@/lib/mock-data";
```

Add a tab state inside `SystemDetailsSheet`:
```typescript
const [activeTab, setActiveTab] = useState<"details" | "approval">("details");
const workflows = MOCK_WORKFLOWS[system.id] ?? [];
const activeWorkflow = workflows.find((w) => w.status === "OPEN") ?? null;
const { activeRole } = useDashboard();
```

Add the tab switcher UI at the top of the Sheet content (after the decision/risk badge row):
```tsx
<div className="flex gap-1 bg-muted/40 rounded-lg p-1 border border-border">
  <button
    onClick={() => setActiveTab("details")}
    className={cn(
      "flex-1 text-xs py-1.5 rounded-md font-medium transition-colors",
      activeTab === "details"
        ? "bg-card text-foreground shadow-sm"
        : "text-muted-foreground hover:text-foreground"
    )}
  >
    Details
  </button>
  <button
    onClick={() => setActiveTab("approval")}
    className={cn(
      "flex-1 text-xs py-1.5 rounded-md font-medium transition-colors",
      activeTab === "approval"
        ? "bg-card text-foreground shadow-sm"
        : "text-muted-foreground hover:text-foreground"
    )}
  >
    Approval
    {activeWorkflow && (
      <span className="ml-1.5 inline-flex items-center justify-center w-3.5 h-3.5 rounded-full bg-amber-500 text-white text-[9px] font-bold">
        !
      </span>
    )}
  </button>
</div>
```

Wrap existing content in `{activeTab === "details" && (...)}` and add:
```tsx
{activeTab === "approval" && (
  <ApprovalWorkflowPanel
    workflows={workflows}
    activeWorkflow={activeWorkflow}
    activeRole={activeRole}
    onApprove={(wId, sId, r) => console.log("approve", wId, sId, r)}
    onReject={(wId, sId, r) => console.log("reject", wId, sId, r)}
    onOverride={(wId, sId, r) => console.log("override", wId, sId, r)}
  />
)}
```

- [ ] **Step 2: Add Workflow column to ReleaseGateTable**

In `release-gate-table.tsx`, import workflow types and mock data:
```typescript
import { MOCK_WORKFLOWS } from "@/lib/mock-data";
import type { ApprovalWorkflow } from "@/lib/types";
```

Add a "Workflow" column header after the last existing column header. In the row rendering, add:
```tsx
{(() => {
  const wfs = MOCK_WORKFLOWS[system.id] ?? [];
  const open = wfs.find((w) => w.status === "OPEN");
  const last = wfs[0];
  if (open) {
    const activeStage = open.stages.find((s) => s.status === "PENDING");
    const stagePos = activeStage ? `Stage ${activeStage.stageOrder}/${open.stages.filter(s => s.status !== "SKIPPED").length}` : "";
    return (
      <span className="inline-flex items-center gap-1 text-[10px] font-medium text-amber-600 dark:text-amber-400">
        <span className="w-1.5 h-1.5 rounded-full bg-amber-500 inline-block" />
        OPEN · {stagePos}
      </span>
    );
  }
  if (last?.status === "APPROVED") {
    return <span className="text-[10px] text-emerald-600 dark:text-emerald-400 font-medium">✓ Approved</span>;
  }
  if (last?.status === "REJECTED") {
    return <span className="text-[10px] text-red-600 dark:text-red-400 font-medium">✗ Rejected</span>;
  }
  return <span className="text-[10px] text-muted-foreground">—</span>;
})()}
```

- [ ] **Step 3: TypeScript check**

```bash
cd apps/dashboard && npx tsc --noEmit 2>&1 | head -20
```

Expected: no errors.

- [ ] **Step 4: Commit**

```bash
git add apps/dashboard/components/details-sheets.tsx
git add apps/dashboard/components/release-gate-table.tsx
git commit -m "feat(dashboard): add Approval tab to system details sheet and Workflow column to gate table"
```

---

## Task 14: /approvals inbox page and sidebar nav item

**Files:**
- Create: `apps/dashboard/app/(dashboard)/approvals/page.tsx`
- Modify: `apps/dashboard/components/sidebar.tsx`
- Modify: `apps/dashboard/app/(dashboard)/layout.tsx`

- [ ] **Step 1: Write the approvals page**

```tsx
"use client";

import { useDashboard } from "@/context/dashboard-context";
import { MOCK_WORKFLOWS } from "@/lib/mock-data";
import type { ApprovalWorkflow, ApprovalStage } from "@/lib/types";
import { cn } from "@/lib/utils";
import { Clock } from "lucide-react";
import { useState } from "react";
import { ApprovalActionModal } from "@/components/approval-action-modal";

const STAGE_LABELS: Record<string, string> = {
  ENG_LEAD_REVIEW: "Engineering Lead Review",
  COMPLIANCE_REVIEW: "Compliance Review",
  LEGAL_SIGNOFF: "Legal Sign-off",
};

const ROLE_TO_STAGE: Record<string, string> = {
  "actor-marco": "AI_ENGINEERING_LEAD",
  "actor-priya": "COMPLIANCE_OFFICER",
  "actor-leo": "LEGAL_COUNSEL",
  "actor-sofia": "COMPLIANCE_OFFICER",
};

export default function ApprovalsPage() {
  const { allSystems, activeRole } = useDashboard();
  const actorRole = ROLE_TO_STAGE[activeRole];

  const [modal, setModal] = useState<{
    stage: ApprovalStage;
    action: "approve" | "reject" | "override";
    systemId: string;
    workflowId: string;
  } | null>(null);

  const openWorkflows = allSystems.flatMap((system) => {
    const wfs = MOCK_WORKFLOWS[system.id] ?? [];
    return wfs
      .filter((w) => w.status === "OPEN")
      .map((w) => ({ system, workflow: w }));
  });

  const myItems = openWorkflows.filter(({ workflow }) =>
    workflow.stages.some(
      (s) =>
        s.status === "PENDING" &&
        s.requiredRole === actorRole &&
        !workflow.stages.some(
          (prior) => prior.stageOrder < s.stageOrder && prior.status === "PENDING"
        )
    )
  );

  const otherItems = openWorkflows.filter(
    ({ workflow }) =>
      !myItems.some(({ workflow: w }) => w.id === workflow.id)
  );

  return (
    <div className="space-y-6">
      <section>
        <h2 className="text-xs font-semibold uppercase tracking-widest text-muted-foreground mb-3">
          Awaiting your action
        </h2>
        {myItems.length === 0 ? (
          <p className="text-xs text-muted-foreground py-6 text-center border border-dashed border-border rounded-xl">
            No stages require your review right now.
          </p>
        ) : (
          <div className="space-y-2">
            {myItems.map(({ system, workflow }) => {
              const activeStage = workflow.stages.find(
                (s) =>
                  s.status === "PENDING" &&
                  s.requiredRole === actorRole &&
                  !workflow.stages.some(
                    (p) => p.stageOrder < s.stageOrder && p.status === "PENDING"
                  )
              )!;
              const openedMs = Date.now() - new Date(workflow.openedAt).getTime();
              const openedDays = Math.floor(openedMs / 86_400_000);
              return (
                <div
                  key={workflow.id}
                  className="flex items-center justify-between border border-border rounded-xl px-4 py-3 bg-card"
                >
                  <div className="flex items-center gap-3">
                    <div className="w-2 h-2 rounded-full bg-amber-500 flex-shrink-0" />
                    <div>
                      <p className="text-sm font-medium text-foreground">{system.name}</p>
                      <p className="text-[10px] text-muted-foreground mt-0.5 flex items-center gap-1">
                        <Clock className="w-3 h-3" />
                        {STAGE_LABELS[activeStage.stageType]} · opened {openedDays}d ago
                      </p>
                    </div>
                  </div>
                  <div className="flex gap-1.5">
                    <button
                      onClick={() =>
                        setModal({ stage: activeStage, action: "approve", systemId: system.id, workflowId: workflow.id })
                      }
                      className="text-[10px] px-2.5 py-1.5 rounded-lg bg-emerald-600 hover:bg-emerald-700 text-white font-medium"
                    >
                      Approve
                    </button>
                    <button
                      onClick={() =>
                        setModal({ stage: activeStage, action: "reject", systemId: system.id, workflowId: workflow.id })
                      }
                      className="text-[10px] px-2.5 py-1.5 rounded-lg bg-red-600 hover:bg-red-700 text-white font-medium"
                    >
                      Reject
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </section>

      <section>
        <h2 className="text-xs font-semibold uppercase tracking-widest text-muted-foreground mb-3">
          In progress — other stages
        </h2>
        {otherItems.length === 0 ? (
          <p className="text-xs text-muted-foreground py-6 text-center border border-dashed border-border rounded-xl">
            No other workflows in progress.
          </p>
        ) : (
          <div className="space-y-2">
            {otherItems.map(({ system, workflow }) => {
              const activeStage = workflow.stages.find((s) => s.status === "PENDING");
              return (
                <div
                  key={workflow.id}
                  className="flex items-center justify-between border border-border rounded-xl px-4 py-3 bg-muted/20"
                >
                  <div className="flex items-center gap-3">
                    <div className="w-2 h-2 rounded-full bg-muted-foreground/40 flex-shrink-0" />
                    <div>
                      <p className="text-sm font-medium text-foreground">{system.name}</p>
                      {activeStage && (
                        <p className="text-[10px] text-muted-foreground mt-0.5">
                          {STAGE_LABELS[activeStage.stageType]} · waiting for {activeStage.requiredRole.replace(/_/g, " ")}
                        </p>
                      )}
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </section>

      {modal && (
        <ApprovalActionModal
          stage={modal.stage}
          action={modal.action}
          onClose={() => setModal(null)}
          onConfirm={(rationale) => {
            console.log("action", modal.action, modal.workflowId, modal.stage.id, rationale);
            setModal(null);
          }}
        />
      )}
    </div>
  );
}
```

- [ ] **Step 2: Add /approvals to sidebar nav**

In `sidebar.tsx`, add to `NAV_ITEMS` between Systems and Evidence:

```typescript
import { ClipboardCheck } from "lucide-react";
```

```typescript
{ href: "/approvals", label: "Approvals", icon: ClipboardCheck },
```

Add pending count badge: In the `Sidebar` component, compute the count from `allSystems`:
```typescript
const { activeRole, setActiveRole, allSystems } = useDashboard();
const pendingCount = allSystems.reduce((acc, sys) => {
  const wfs = Object.values(MOCK_WORKFLOWS)[Object.keys(MOCK_WORKFLOWS).indexOf(sys.id)];
  // simplified: count open workflows
  return acc;
}, 0);
```

For simplicity, import and count directly:
```typescript
import { MOCK_WORKFLOWS } from "@/lib/mock-data";

const openCount = Object.values(MOCK_WORKFLOWS).filter((wfs) =>
  wfs.some((w) => w.status === "OPEN")
).length;
```

Render the badge on the Approvals nav item:
```tsx
{item.label === "Approvals" && openCount > 0 && (
  <span className="ml-auto text-[9px] font-bold bg-amber-500 text-white rounded-full w-4 h-4 flex items-center justify-center">
    {openCount}
  </span>
)}
```

- [ ] **Step 3: Add /approvals to PAGE_META in layout.tsx**

```typescript
"/approvals": {
  title: "Approval Workflows",
  subtitle: "Review and sign off AI system releases. Staged approvals for engineering, compliance, and legal.",
},
```

- [ ] **Step 4: TypeScript check**

```bash
cd apps/dashboard && npx tsc --noEmit 2>&1 | head -20
```

Expected: no errors.

- [ ] **Step 5: Commit**

```bash
git add apps/dashboard/app/\(dashboard\)/approvals/page.tsx
git add apps/dashboard/components/sidebar.tsx
git add apps/dashboard/app/\(dashboard\)/layout.tsx
git commit -m "feat(dashboard): add /approvals inbox page and sidebar nav item"
```

---

## Task 15: Final verification

- [ ] **Step 1: Run full backend test suite**

```bash
cd services/api && mvn test -q 2>&1 | tail -10
```

Expected: BUILD SUCCESS, 0 failures.

- [ ] **Step 2: Run full TypeScript check**

```bash
cd apps/dashboard && npx tsc --noEmit 2>&1 | head -20
```

Expected: no errors.

- [ ] **Step 3: Start the dashboard and verify approvals page loads**

```bash
cd apps/dashboard && npm run dev &
sleep 6 && curl -s -o /dev/null -w "%{http_code}" http://localhost:3000/approvals
```

Expected: `200`.

- [ ] **Step 4: Start the API and verify workflow endpoint responds**

```bash
cd services/api && mvn spring-boot:run -q &
sleep 12
# Create a system and check its workflow
SYS=$(curl -s -X POST http://localhost:8080/api/v1/systems \
  -H "Content-Type: application/json" \
  -d '{"name":"Test WF","owner":"Ops","purpose":"Test","riskClass":"high","riskBasis":"Test","deploymentRegion":"EU","evidenceCoverage":50,"evalScore":70,"dataContractStatus":"breach","openGaps":["Human oversight SOP missing"]}' | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])")
curl -s http://localhost:8080/api/v1/systems/$SYS/workflows/active | python3 -m json.tool | grep '"status"'
kill %2
```

Expected: `"status": "OPEN"` printed.

- [ ] **Step 5: Final commit if any loose files**

```bash
git status
# Commit any remaining unstaged changes
```
