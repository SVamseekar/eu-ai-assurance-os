package os.assurance.eu.api.system;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import os.assurance.eu.api.corpus.CorpusQueryService;
import os.assurance.eu.api.proposal.MappingProposalEntity;
import os.assurance.eu.api.proposal.MappingProposalJpaRepository;
import os.assurance.eu.api.tenant.TenantContext;
import os.assurance.eu.api.workflow.ApprovalStage;
import os.assurance.eu.api.workflow.ApprovalWorkflow;
import os.assurance.eu.api.workflow.ApprovalWorkflowRepository;
import os.assurance.eu.api.workflow.StageStatus;
import os.assurance.eu.api.workflow.StageType;
import org.springframework.stereotype.Component;

@Component
public class AcceptedLinkGateControlSource implements GateControlSource {
  private final MappingProposalJpaRepository proposals;
  private final CorpusQueryService corpus;
  private final ApprovalWorkflowRepository workflows;
  private final TenantContext tenantContext;

  public AcceptedLinkGateControlSource(
      MappingProposalJpaRepository proposals,
      CorpusQueryService corpus,
      ApprovalWorkflowRepository workflows,
      TenantContext tenantContext) {
    this.proposals = proposals;
    this.corpus = corpus;
    this.workflows = workflows;
    this.tenantContext = tenantContext;
  }

  @Override
  public List<GateControl> acceptedLinks(UUID systemId) {
    if (systemId == null || tenantContext == null) {
      return List.of();
    }
    List<ApprovalWorkflow> approvals = workflows.findAllBySystemId(systemId);
    List<GateControl> links = new ArrayList<>();
    for (MappingProposalEntity row : proposals.findAllByTenantIdAndSystemIdOrderByCreatedAtAsc(
        tenantContext.tenantId(), systemId)) {
      if (!"ACCEPTED".equals(row.status()) || "abstain".equals(row.relation())) {
        continue;
      }
      CorpusQueryService.ProvisionView provision = provision(row.provisionKey());
      String force = provision == null ? null : provision.forceStatus();
      ControlMode mode = modeFor(row.controlMode(), force);
      links.add(new GateControl(
          row.id(),
          row.provisionKey(),
          force,
          provision == null ? null : provision.forceFrom(),
          mode,
          signedOff(approvals, row.reopenedAt() == null ? row.decidedAt() : row.reopenedAt())));
    }
    return List.copyOf(links);
  }

  private static ControlMode modeFor(String stored, String force) {
    if ("FUTURE".equals(force)) {
      return ControlMode.INFORMATIONAL;
    }
    if (stored == null || stored.isBlank()) {
      return ControlMode.WARNING;
    }
    return ControlMode.valueOf(stored);
  }

  private static boolean signedOff(List<ApprovalWorkflow> approvals, Instant since) {
    if (since == null) {
      return false;
    }
    for (ApprovalWorkflow workflow : approvals) {
      for (ApprovalStage stage : workflow.stages()) {
        boolean reviewStage = stage.stageType() == StageType.COMPLIANCE_REVIEW
            || stage.stageType() == StageType.LEGAL_SIGNOFF;
        boolean done = stage.status() == StageStatus.APPROVED || stage.status() == StageStatus.OVERRIDDEN;
        if (reviewStage && done && stage.actedAt() != null && stage.actedAt().isAfter(since)) {
          return true;
        }
      }
    }
    return false;
  }

  private CorpusQueryService.ProvisionView provision(String provisionKey) {
    if (provisionKey == null) {
      return null;
    }
    for (CorpusQueryService.ProvisionView row : corpus.current().provisions()) {
      if (provisionKey.equals(row.provisionKey())) {
        return row;
      }
    }
    return null;
  }
}
