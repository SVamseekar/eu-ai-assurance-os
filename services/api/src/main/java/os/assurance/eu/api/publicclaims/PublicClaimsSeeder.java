package os.assurance.eu.api.publicclaims;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import os.assurance.eu.api.contract.CreateDataContractRequest;
import os.assurance.eu.api.contract.DataContractService;
import os.assurance.eu.api.evidence.CreateEvidenceDocumentRequest;
import os.assurance.eu.api.evidence.EvidenceService;
import os.assurance.eu.api.publicclaims.PublicClaimsCatalog.PublicClaimsSystemSpec;
import os.assurance.eu.api.system.AiSystem;
import os.assurance.eu.api.system.AiSystemRepository;
import os.assurance.eu.api.system.DataContractStatus;
import os.assurance.eu.api.system.ReleaseDecision;
import os.assurance.eu.api.system.ReleaseGateService;
import os.assurance.eu.api.tenant.TenantContext;
import os.assurance.eu.api.workflow.ApprovalWorkflowService;
import os.assurance.eu.api.workflow.WorkflowTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(2)
public class PublicClaimsSeeder implements CommandLineRunner {
  private static final Logger log = LoggerFactory.getLogger(PublicClaimsSeeder.class);

  private final PublicClaimsProperties properties;
  private final PublicClaimsService publicClaims;
  private final AiSystemRepository systems;
  private final ReleaseGateService releaseGateService;
  private final ApprovalWorkflowService approvalWorkflowService;
  private final EvidenceService evidenceService;
  private final DataContractService dataContractService;
  private final TenantContext tenantContext;

  public PublicClaimsSeeder(
      PublicClaimsProperties properties,
      PublicClaimsService publicClaims,
      AiSystemRepository systems,
      ReleaseGateService releaseGateService,
      ApprovalWorkflowService approvalWorkflowService,
      EvidenceService evidenceService,
      DataContractService dataContractService,
      TenantContext tenantContext) {
    this.properties = properties;
    this.publicClaims = publicClaims;
    this.systems = systems;
    this.releaseGateService = releaseGateService;
    this.approvalWorkflowService = approvalWorkflowService;
    this.evidenceService = evidenceService;
    this.dataContractService = dataContractService;
    this.tenantContext = tenantContext;
  }

  @Override
  @Transactional
  public void run(String... args) {
    if (!properties.isPublicClaims()) {
      return;
    }
    tenantContext.setOverrides(TenantContext.DEFAULT_TENANT_ID, TenantContext.DEFAULT_USER_ID);
    try {
      int created = 0;
      for (PublicClaimsSystemSpec spec : publicClaims.catalog().systems()) {
        if (seedOne(spec)) {
          created++;
        }
      }
      log.info("Public-claims teasers ready (created {}, catalog {})",
          created, publicClaims.catalog().systems().size());
    } finally {
      tenantContext.clearOverrides();
    }
  }

  private boolean seedOne(PublicClaimsSystemSpec spec) {
    if (systems.findByModelName(spec.modelName()).isPresent()) {
      return false;
    }
    Instant now = Instant.now();
    AiSystem draft = new AiSystem(
        UUID.randomUUID(),
        spec.systemName(),
        spec.owner(),
        spec.purpose(),
        spec.riskClass(),
        spec.riskBasis(),
        "EU",
        spec.evidenceCoverage(),
        spec.evalScore(),
        DataContractStatus.BREACH,
        ReleaseDecision.REVIEW,
        new ArrayList<>(spec.openGaps()),
        spec.vendorName(),
        spec.modelName(),
        spec.modelVersion(),
        List.of(PublicClaimsCatalog.dataSource(spec.slug())),
        spec.sector(),
        spec.decisionImpact(),
        spec.affectedUsers(),
        now,
        now);
    ReleaseDecision decision = releaseGateService.calculate(draft).decision();
    AiSystem saved = systems.save(new AiSystem(
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
    approvalWorkflowService.openCycle(saved, WorkflowTrigger.SYSTEM_CREATED);
    evidenceService.ingest(new CreateEvidenceDocumentRequest(
        saved.id(),
        "PUBLIC_CLAIMS",
        "Public claims sources — " + spec.legalName(),
        publicClaims.firstSourceUri(spec),
        publicClaims.evidenceBody(spec),
        null,
        null));
    dataContractService.createContract(new CreateDataContractRequest(
        saved.id(),
        spec.slug() + ".public-claims.v1",
        "Public-claims teaser",
        "public-claims-2026-09-15",
        DataContractStatus.BREACH,
        spec.evidenceCoverage()));
    return true;
  }
}
