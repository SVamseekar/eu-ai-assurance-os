package os.assurance.eu.api.system;

import java.time.Clock;
import java.time.Instant;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EvidencePackService {
  public static final String PACK_VERSION = "1.0";

  private final AiSystemRepository repository;
  private final ReleaseGateService releaseGateService;
  private final AuditService auditService;
  private final DataContractService dataContractService;
  private final ApprovalWorkflowService approvalWorkflowService;
  private final Clock clock;
  private final String generator;

  public EvidencePackService(
      AiSystemRepository repository,
      ReleaseGateService releaseGateService,
      AuditService auditService,
      DataContractService dataContractService,
      ApprovalWorkflowService approvalWorkflowService,
      Clock clock,
      @Value("${assurance.evidence-pack.generator:eu-ai-assurance-api/0.1.0}") String generator) {
    this.repository = repository;
    this.releaseGateService = releaseGateService;
    this.auditService = auditService;
    this.dataContractService = dataContractService;
    this.approvalWorkflowService = approvalWorkflowService;
    this.clock = clock;
    this.generator = generator;
  }

  @Transactional
  public EvidencePackResponse buildAndExport(UUID systemId, String exportFormat) {
    AiSystem system = repository.findById(systemId)
        .orElseThrow(() -> new IllegalArgumentException("AI system not found"));
    ReleaseGateResponse releaseGate = releaseGateService.calculate(system);

    Instant generatedAt = clock.instant();
    String auditChainHead = auditService.chainHeadHash().orElse(null);
    List<AuditEvent> auditEvents = auditService.findBySystemId(system.id());
    List<Map<String, Object>> dataContracts = dataContractService.listContracts(system.id()).stream()
        .map(contract -> contractEvidence(system, contract))
        .toList();
    List<Map<String, Object>> approvals = approvalWorkflowService.listBySystemId(system.id()).stream()
        .map(this::workflowEvidence)
        .toList();
    Map<String, Object> riskClassification = riskClassification(system);
    List<Map<String, Object>> evidence = List.of(Map.of(
        "coverage", system.evidenceCoverage(),
        "openGaps", system.openGaps()));
    List<Map<String, Object>> evalRuns = List.of(Map.of(
        "latestScore", system.evalScore(),
        "releaseDecision", system.releaseDecision()));

    Map<String, Object> sealPayload = EvidencePackSealer.sealPayload(
        PACK_VERSION,
        generator,
        auditChainHead,
        system.id(),
        generatedAt,
        releaseGate.decision(),
        riskClassification,
        evidence,
        evalRuns,
        dataContracts,
        approvals,
        auditEvents);
    String contentSha256 = EvidencePackSealer.contentSha256(sealPayload);

    Map<String, Object> auditPayload = new LinkedHashMap<>();
    auditPayload.put("decision", releaseGate.decision().name());
    auditPayload.put("contentSha256", contentSha256);
    auditPayload.put("format", exportFormat == null ? "json" : exportFormat);
    auditPayload.put("evidencePackVersion", PACK_VERSION);
    auditService.append(
        system.id(),
        "evidence_pack.exported",
        "ai_system",
        system.id().toString(),
        auditPayload);

    return new EvidencePackResponse(
        system.id(),
        generatedAt,
        releaseGate.decision(),
        riskClassification,
        evidence,
        evalRuns,
        dataContracts,
        approvals,
        auditEvents,
        PACK_VERSION,
        contentSha256,
        generator,
        auditChainHead);
  }

  public byte[] renderPdf(EvidencePackResponse pack, String systemName) {
    return EvidencePackPdfRenderer.render(pack, systemName);
  }

  public String pdfFilename(EvidencePackResponse pack) {
    return EvidencePackPdfRenderer.filename(pack.systemId(), pack.generatedAt());
  }

  private Map<String, Object> riskClassification(AiSystem system) {
    Map<String, Object> risk = new LinkedHashMap<>();
    risk.put("riskClass", system.riskClass());
    risk.put("basis", system.riskBasis());
    risk.put("deploymentRegion", system.deploymentRegion());
    risk.put("vendorName", system.vendorName() == null ? "" : system.vendorName());
    risk.put("modelName", system.modelName() == null ? "" : system.modelName());
    risk.put("modelVersion", system.modelVersion() == null ? "" : system.modelVersion());
    risk.put("dataSources", system.dataSources());
    risk.put("sector", system.sector() == null ? "" : system.sector());
    risk.put("decisionImpact", system.decisionImpact() == null ? "" : system.decisionImpact());
    risk.put("affectedUsers", system.affectedUsers());
    return risk;
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
}
