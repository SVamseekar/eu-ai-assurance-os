package os.assurance.eu.api.system;

import jakarta.validation.Valid;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import os.assurance.eu.api.audit.AuditEvent;
import os.assurance.eu.api.audit.AuditService;
import os.assurance.eu.api.contract.DataContract;
import os.assurance.eu.api.contract.DataContractService;
import os.assurance.eu.api.contract.DriftEvent;
import os.assurance.eu.api.contract.DriftStatus;
import os.assurance.eu.api.workflow.ApprovalStage;
import os.assurance.eu.api.workflow.ApprovalWorkflow;
import os.assurance.eu.api.workflow.ApprovalWorkflowService;
import os.assurance.eu.api.control.ControlService;
import os.assurance.eu.api.observability.NfrMetrics;
import os.assurance.eu.api.tenant.TenantAuthorizationService;
import os.assurance.eu.api.tenant.UserRole;
import os.assurance.eu.api.workflow.WorkflowTrigger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/systems")
public class AiSystemController {
  private final AiSystemRepository repository;
  private final ReleaseGateService releaseGateService;
  private final AuditService auditService;
  private final DataContractService dataContractService;
  private final ApprovalWorkflowService approvalWorkflowService;
  private final ControlService controlService;
  private final TenantAuthorizationService authorizationService;
  private final NfrMetrics nfrMetrics;

  public AiSystemController(
      AiSystemRepository repository,
      ReleaseGateService releaseGateService,
      AuditService auditService,
      DataContractService dataContractService,
      ApprovalWorkflowService approvalWorkflowService,
      ControlService controlService,
      TenantAuthorizationService authorizationService,
      NfrMetrics nfrMetrics) {
    this.repository = repository;
    this.releaseGateService = releaseGateService;
    this.auditService = auditService;
    this.dataContractService = dataContractService;
    this.approvalWorkflowService = approvalWorkflowService;
    this.controlService = controlService;
    this.authorizationService = authorizationService;
    this.nfrMetrics = nfrMetrics;
  }

  @GetMapping
  public List<AiSystem> listSystems() {
    return nfrMetrics.recordRegistryRead("list", repository::findAll);
  }

  @GetMapping("/{systemId}")
  public AiSystem getSystem(@PathVariable UUID systemId) {
    return nfrMetrics.recordRegistryRead("get", () -> repository.findById(systemId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "AI system not found")));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CreateAiSystemResponse createSystem(@Valid @RequestBody CreateAiSystemRequest request) {
    authorizationService.requireAnyRole(
        UserRole.ADMIN, UserRole.AI_ENGINEERING_LEAD, UserRole.COMPLIANCE_OFFICER);
    Instant now = Instant.now();
    AiSystem draft = new AiSystem(
        UUID.randomUUID(),
        request.name(),
        request.owner(),
        request.purpose(),
        request.riskClass(),
        request.riskBasis(),
        request.deploymentRegion(),
        request.evidenceCoverage() == null ? 0 : request.evidenceCoverage(),
        request.evalScore() == null ? 0 : request.evalScore(),
        request.dataContractStatus() == null ? DataContractStatus.WARNING : request.dataContractStatus(),
        ReleaseDecision.REVIEW,
        request.openGaps() == null ? List.of() : new ArrayList<>(request.openGaps()),
        request.vendorName(),
        request.modelName(),
        request.modelVersion(),
        request.dataSources() == null ? List.of() : new ArrayList<>(request.dataSources()),
        request.sector(),
        request.decisionImpact(),
        request.affectedUsers() == null ? List.of() : new ArrayList<>(request.affectedUsers()),
        now,
        now);
    AiSystem saved = saveWithCalculatedDecision(draft);
    controlService.attachApplicableControls(saved);
    // recompute after control attach so CONTROL:* blockers apply
    saved = saveWithCalculatedDecision(saved);
    auditService.append(
        saved.id(),
        "ai_system.created",
        "ai_system",
        saved.id().toString(),
        Map.of(
            "name", saved.name(),
            "releaseDecision", saved.releaseDecision(),
            "vendorName", saved.vendorName() == null ? "" : saved.vendorName(),
            "modelName", saved.modelName() == null ? "" : saved.modelName()));
    approvalWorkflowService.openCycle(saved, WorkflowTrigger.SYSTEM_CREATED);
    return new CreateAiSystemResponse(saved.id(), saved.releaseDecision(), saved.createdAt());
  }

  @PatchMapping("/{systemId}")
  public AiSystem updateSystem(
      @PathVariable UUID systemId,
      @Valid @RequestBody UpdateAiSystemRequest request) {
    authorizationService.requireAnyRole(
        UserRole.ADMIN, UserRole.AI_ENGINEERING_LEAD, UserRole.COMPLIANCE_OFFICER);
    AiSystem existing = getSystem(systemId);
    AiSystem draft = new AiSystem(
        existing.id(),
        request.name() == null ? existing.name() : request.name(),
        request.owner() == null ? existing.owner() : request.owner(),
        request.purpose() == null ? existing.purpose() : request.purpose(),
        request.riskClass() == null ? existing.riskClass() : request.riskClass(),
        request.riskBasis() == null ? existing.riskBasis() : request.riskBasis(),
        request.deploymentRegion() == null ? existing.deploymentRegion() : request.deploymentRegion(),
        request.evidenceCoverage() == null ? existing.evidenceCoverage() : request.evidenceCoverage(),
        request.evalScore() == null ? existing.evalScore() : request.evalScore(),
        request.dataContractStatus() == null ? existing.dataContractStatus() : request.dataContractStatus(),
        existing.releaseDecision(),
        request.openGaps() == null ? existing.openGaps() : new ArrayList<>(request.openGaps()),
        request.vendorName() == null ? existing.vendorName() : request.vendorName(),
        request.modelName() == null ? existing.modelName() : request.modelName(),
        request.modelVersion() == null ? existing.modelVersion() : request.modelVersion(),
        request.dataSources() == null ? existing.dataSources() : new ArrayList<>(request.dataSources()),
        request.sector() == null ? existing.sector() : request.sector(),
        request.decisionImpact() == null ? existing.decisionImpact() : request.decisionImpact(),
        request.affectedUsers() == null ? existing.affectedUsers() : new ArrayList<>(request.affectedUsers()),
        existing.createdAt(),
        Instant.now());
    if (request.riskClass() != null && request.riskClass() != existing.riskClass()) {
      controlService.attachApplicableControls(draft);
    }
    AiSystem saved = saveWithCalculatedDecision(draft);
    auditService.append(
        saved.id(),
        "ai_system.updated",
        "ai_system",
        saved.id().toString(),
        Map.of("releaseDecision", saved.releaseDecision()));
    return saved;
  }

  @PostMapping("/{systemId}/risk-classification")
  public RiskClassificationResponse classifyRisk(
      @PathVariable UUID systemId,
      @Valid @RequestBody RiskClassificationRequest request) {
    authorizationService.requireAnyRole(
        UserRole.ADMIN,
        UserRole.AI_ENGINEERING_LEAD,
        UserRole.COMPLIANCE_OFFICER,
        UserRole.LEGAL_COUNSEL);
    AiSystem existing = getSystem(systemId);
    List<String> openGaps = new ArrayList<>(existing.openGaps());
    if (request.humanOversightRequired() && openGaps.stream().noneMatch(this::isOversightGap)) {
      openGaps.add("Human oversight evidence required");
    }
    AiSystem draft = new AiSystem(
        existing.id(),
        existing.name(),
        existing.owner(),
        existing.purpose(),
        request.riskClass(),
        request.basis(),
        existing.deploymentRegion(),
        existing.evidenceCoverage(),
        existing.evalScore(),
        existing.dataContractStatus(),
        existing.releaseDecision(),
        openGaps,
        existing.vendorName(),
        existing.modelName(),
        existing.modelVersion(),
        existing.dataSources(),
        request.sector() == null ? existing.sector() : request.sector(),
        request.decisionImpact() == null ? existing.decisionImpact() : request.decisionImpact(),
        request.affectedUsers() == null ? existing.affectedUsers() : new ArrayList<>(request.affectedUsers()),
        existing.createdAt(),
        Instant.now());
    controlService.attachApplicableControls(draft);
    AiSystem saved = saveWithCalculatedDecision(draft);
    auditService.append(
        saved.id(),
        "risk_classification.updated",
        "ai_system",
        saved.id().toString(),
        Map.of(
            "riskClass", saved.riskClass(),
            "humanOversightRequired", request.humanOversightRequired(),
            "affectedUsers", saved.affectedUsers(),
            "sector", saved.sector() == null ? "" : saved.sector(),
            "decisionImpact", saved.decisionImpact() == null ? "" : saved.decisionImpact()));
    approvalWorkflowService.openCycle(saved, WorkflowTrigger.RISK_RECLASSIFIED);
    return new RiskClassificationResponse(
        saved.id(),
        saved.riskClass(),
        saved.riskBasis(),
        request.humanOversightRequired(),
        saved.releaseDecision(),
        saved.updatedAt());
  }

  @GetMapping("/{systemId}/release-gate")
  public ReleaseGateResponse getReleaseGate(@PathVariable UUID systemId) {
    AiSystem system = getSystem(systemId);
    ReleaseGateResponse response = releaseGateService.calculate(system);
    auditService.append(
        system.id(),
        "release_gate.calculated",
        "ai_system",
        system.id().toString(),
        Map.of("decision", response.decision(), "blockers", response.blockers()));
    return response;
  }

  @GetMapping("/{systemId}/evidence-pack")
  public EvidencePackResponse getEvidencePack(@PathVariable UUID systemId) {
    authorizationService.requireAnyRole(
        UserRole.ADMIN,
        UserRole.AI_ENGINEERING_LEAD,
        UserRole.COMPLIANCE_OFFICER,
        UserRole.LEGAL_COUNSEL,
        UserRole.AUDITOR);
    AiSystem system = getSystem(systemId);
    ReleaseGateResponse releaseGate = releaseGateService.calculate(system);
    auditService.append(
        system.id(),
        "evidence_pack.exported",
        "ai_system",
        system.id().toString(),
        Map.of("decision", releaseGate.decision()));
    List<AuditEvent> auditEvents = auditService.findBySystemId(system.id());
    List<Map<String, Object>> dataContracts = dataContractService.listContracts(system.id()).stream()
        .map(contract -> contractEvidence(system, contract))
        .toList();
    List<Map<String, Object>> approvals = approvalWorkflowService.listBySystemId(system.id()).stream()
        .map(this::workflowEvidence)
        .toList();
    return new EvidencePackResponse(
        system.id(),
        Instant.now(),
        releaseGate.decision(),
        Map.of(
            "riskClass", system.riskClass(),
            "basis", system.riskBasis(),
            "deploymentRegion", system.deploymentRegion(),
            "vendorName", system.vendorName() == null ? "" : system.vendorName(),
            "modelName", system.modelName() == null ? "" : system.modelName(),
            "modelVersion", system.modelVersion() == null ? "" : system.modelVersion(),
            "dataSources", system.dataSources(),
            "sector", system.sector() == null ? "" : system.sector(),
            "decisionImpact", system.decisionImpact() == null ? "" : system.decisionImpact(),
            "affectedUsers", system.affectedUsers()),
        List.of(Map.of(
            "coverage", system.evidenceCoverage(),
            "openGaps", system.openGaps())),
        List.of(Map.of(
            "latestScore", system.evalScore(),
            "releaseDecision", system.releaseDecision())),
        dataContracts,
        approvals,
        auditEvents);
  }

  private Map<String, Object> workflowEvidence(ApprovalWorkflow workflow) {
    Map<String, Object> evidence = new LinkedHashMap<>();
    evidence.put("workflowId", workflow.id());
    evidence.put("trigger", workflow.trigger());
    evidence.put("status", workflow.status());
    evidence.put("openedAt", workflow.openedAt());
    evidence.put("closedAt", workflow.closedAt());
    evidence.put("stages", workflow.stages().stream().map(this::stageEvidence).toList());
    return evidence;
  }

  private Map<String, Object> stageEvidence(ApprovalStage stage) {
    Map<String, Object> evidence = new LinkedHashMap<>();
    evidence.put("stageType", stage.stageType());
    evidence.put("status", stage.status());
    evidence.put("actorId", stage.actorId());
    evidence.put("rationale", stage.rationale());
    evidence.put("actedAt", stage.actedAt());
    return evidence;
  }

  private Map<String, Object> contractEvidence(AiSystem system, DataContract contract) {
    List<DriftEvent> driftEvents = dataContractService.listDriftEvents(contract.id());
    List<Map<String, Object>> drift = driftEvents.stream()
        .map(this::driftEvidence)
        .toList();
    List<UUID> openDriftEventIds = driftEvents.stream()
        .filter(event -> event.status() == DriftStatus.OPEN)
        .map(DriftEvent::id)
        .toList();
    Map<String, Object> lineage = new LinkedHashMap<>();
    lineage.put("systemId", system.id());
    lineage.put("systemName", system.name());
    lineage.put("contractId", contract.id());
    lineage.put("contractName", contract.name());
    lineage.put("owner", contract.owner());
    lineage.put("version", contract.version());
    lineage.put("openDriftEventIds", openDriftEventIds);

    Map<String, Object> evidence = new LinkedHashMap<>();
    evidence.put("id", contract.id());
    evidence.put("name", contract.name());
    evidence.put("owner", contract.owner());
    evidence.put("version", contract.version());
    evidence.put("status", contract.status());
    evidence.put("coverage", contract.coverage());
    evidence.put("lineage", lineage);
    evidence.put("driftEvents", drift);
    return evidence;
  }

  private Map<String, Object> driftEvidence(DriftEvent event) {
    Map<String, Object> drift = new LinkedHashMap<>();
    drift.put("id", event.id());
    drift.put("severity", event.severity());
    drift.put("field", event.field());
    drift.put("description", event.description());
    drift.put("status", event.status());
    drift.put("createdAt", event.createdAt());
    drift.put("updatedAt", event.updatedAt());
    return drift;
  }

  private AiSystem saveWithCalculatedDecision(AiSystem draft) {
    ReleaseDecision decision = releaseGateService.calculate(draft).decision();
    return repository.save(new AiSystem(
        draft.id(),
        draft.name(),
        draft.owner(),
        draft.purpose(),
        draft.riskClass(),
        draft.riskBasis(),
        draft.deploymentRegion(),
        draft.evidenceCoverage(),
        draft.evalScore(),
        draft.dataContractStatus(),
        decision,
        draft.openGaps(),
        draft.vendorName(),
        draft.modelName(),
        draft.modelVersion(),
        draft.dataSources(),
        draft.sector(),
        draft.decisionImpact(),
        draft.affectedUsers(),
        draft.createdAt(),
        draft.updatedAt()));
  }

  private boolean isOversightGap(String gap) {
    return gap.toLowerCase().contains("oversight");
  }
}
