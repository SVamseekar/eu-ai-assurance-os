package os.assurance.eu.api.sector;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import os.assurance.eu.api.audit.AuditService;
import os.assurance.eu.api.system.AiSystem;
import os.assurance.eu.api.system.AiSystemRepository;
import os.assurance.eu.api.system.DataContractStatus;
import os.assurance.eu.api.system.ReleaseDecision;
import os.assurance.eu.api.system.RiskClass;
import os.assurance.eu.api.control.ControlService;
import os.assurance.eu.api.system.ReleaseGateService;
import os.assurance.eu.api.tenant.TenantAuthorizationService;
import os.assurance.eu.api.tenant.UserRole;
import os.assurance.eu.api.workflow.ApprovalWorkflowService;
import os.assurance.eu.api.workflow.WorkflowTrigger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SectorPackService {
  private final SectorPackRegistry registry;
  private final AiSystemRepository systems;
  private final ControlService controlService;
  private final ReleaseGateService releaseGateService;
  private final AuditService auditService;
  private final ApprovalWorkflowService approvalWorkflowService;
  private final TenantAuthorizationService authorizationService;
  private final List<IntegrationConnector> connectors;

  public SectorPackService(
      SectorPackRegistry registry,
      AiSystemRepository systems,
      ControlService controlService,
      ReleaseGateService releaseGateService,
      AuditService auditService,
      ApprovalWorkflowService approvalWorkflowService,
      TenantAuthorizationService authorizationService,
      List<IntegrationConnector> connectors) {
    this.registry = registry;
    this.systems = systems;
    this.controlService = controlService;
    this.releaseGateService = releaseGateService;
    this.auditService = auditService;
    this.approvalWorkflowService = approvalWorkflowService;
    this.authorizationService = authorizationService;
    this.connectors = connectors == null ? List.of() : connectors;
  }

  public SectorPacksResponse listPacks() {
    List<SectorPackView> views = registry.enabledPacks().stream()
        .map(SectorPackView::from)
        .toList();
    return SectorPacksResponse.of(views);
  }

  public SectorPackView getPack(String packId) {
    return registry.findById(packId)
        .map(SectorPackView::from)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sector pack not found"));
  }

  public SectorPackView resolveForSector(String sector) {
    return registry.resolveForSector(sector)
        .map(SectorPackView::from)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "No enabled sector pack for sector: " + sector));
  }

  public SectorTemplateContentResponse loadTemplate(String packId, String templateId) {
    SectorPack pack = registry.findById(packId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sector pack not found"));
    SampleEvidenceTemplate template = pack.sampleEvidenceTemplates().stream()
        .filter(t -> t.id().equalsIgnoreCase(templateId))
        .findFirst()
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Template not found"));
    String content = readClasspath(template.resourcePath());
    return new SectorTemplateContentResponse(
        pack.id(),
        template.id(),
        template.title(),
        template.documentType(),
        template.resourcePath(),
        content,
        SectorPackDisclaimers.SCOPE);
  }

  /**
   * Optional insurance integration stub: maps external claims model metadata into a new
   * AI system registry entry with sector=insurance (triggers insurance pack controls).
   */
  @Transactional
  public ClaimsModelRegisterResponse registerClaimsModel(ClaimsModelRegisterRequest request) {
    authorizationService.requireAnyRole(
        UserRole.ADMIN, UserRole.AI_ENGINEERING_LEAD, UserRole.COMPLIANCE_OFFICER);
    if (registry.findById("insurance").isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE, "Insurance sector pack is not enabled");
    }

    Instant now = Instant.now();
    String owner = blankToDefault(request.owner(), "Claims Operations");
    String purpose = blankToDefault(
        request.purpose(),
        "External claims model registration: " + request.externalModelId());
    String modelName = blankToDefault(request.modelName(), request.name());
    List<String> dataSources = request.dataSources() == null
        ? List.of()
        : new ArrayList<>(request.dataSources());

    AiSystem draft = new AiSystem(
        UUID.randomUUID(),
        request.name(),
        owner,
        purpose,
        RiskClass.HIGH,
        "Insurance claims model registered via integration stub — human risk confirmation still required",
        "EU",
        0,
        0,
        DataContractStatus.WARNING,
        ReleaseDecision.REVIEW,
        new ArrayList<>(List.of("Claims model card and oversight evidence pending")),
        request.vendorName(),
        modelName,
        request.modelVersion(),
        dataSources,
        "insurance",
        blankToDefault(request.decisionImpact(), "eligibility"),
        List.of("claimants"),
        now,
        now);

    AiSystem saved = saveWithCalculatedDecision(draft);
    controlService.attachApplicableControls(saved);
    saved = saveWithCalculatedDecision(saved);

    auditService.append(
        saved.id(),
        "integration.insurance.claims_model_registered",
        "ai_system",
        saved.id().toString(),
        Map.of(
            "externalModelId", request.externalModelId(),
            "packId", "insurance",
            "sector", "insurance",
            "stub", true));

    approvalWorkflowService.openCycle(saved, WorkflowTrigger.SYSTEM_CREATED);

    for (IntegrationConnector connector : connectors) {
      connector.pushReleaseDecision(
          saved.id(),
          saved.releaseDecision(),
          Map.of("source", "claims-model-register", "externalModelId", request.externalModelId()));
    }

    return new ClaimsModelRegisterResponse(
        saved.id(),
        request.externalModelId(),
        "insurance",
        "insurance",
        saved.releaseDecision(),
        "Stub mapping only — no proprietary vendor API called. "
            + SectorPackDisclaimers.METRICS_LABEL + ".");
  }

  public List<Map<String, Object>> pullModelInventoryStub() {
    List<Map<String, Object>> out = new ArrayList<>();
    for (IntegrationConnector connector : connectors) {
      out.addAll(connector.pullModelInventory());
    }
    return out;
  }

  private AiSystem saveWithCalculatedDecision(AiSystem draft) {
    ReleaseDecision decision = releaseGateService.calculate(draft).decision();
    return systems.save(new AiSystem(
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

  private static String blankToDefault(String value, String defaultValue) {
    return value == null || value.isBlank() ? defaultValue : value;
  }

  private static String readClasspath(String path) {
    try {
      ClassPathResource resource = new ClassPathResource(path);
      if (!resource.exists()) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Template resource missing: " + path);
      }
      try (InputStream in = resource.getInputStream()) {
        return new String(in.readAllBytes(), StandardCharsets.UTF_8);
      }
    } catch (IOException e) {
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read template: " + path);
    }
  }

  /** Package-private helper for tests — normalize sector token. */
  static String normalizeSector(String sector) {
    if (sector == null) {
      return "";
    }
    return sector.trim().toLowerCase(Locale.ROOT).replace(' ', '_');
  }
}
