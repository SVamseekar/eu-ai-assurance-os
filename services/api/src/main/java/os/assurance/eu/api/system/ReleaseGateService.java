package os.assurance.eu.api.system;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import os.assurance.eu.api.control.ControlEntity;
import os.assurance.eu.api.control.ControlJpaRepository;
import os.assurance.eu.api.control.ControlStatus;
import os.assurance.eu.api.control.SystemControlEntity;
import os.assurance.eu.api.control.SystemControlJpaRepository;
import os.assurance.eu.api.tenant.TenantContext;
import org.springframework.stereotype.Service;

@Service
public class ReleaseGateService {
  private static final int EVIDENCE_PASS_THRESHOLD = 82;
  private static final int EVAL_PASS_THRESHOLD = 85;
  private static final int EVAL_HARD_BLOCK_THRESHOLD = 78;

  private final SystemControlJpaRepository systemControls;
  private final ControlJpaRepository controls;
  private final TenantContext tenantContext;

  public ReleaseGateService(
      SystemControlJpaRepository systemControls,
      ControlJpaRepository controls,
      TenantContext tenantContext) {
    this.systemControls = systemControls;
    this.controls = controls;
    this.tenantContext = tenantContext;
  }

  /** Test/local constructor without control lookup. */
  public ReleaseGateService() {
    this.systemControls = null;
    this.controls = null;
    this.tenantContext = null;
  }

  public ReleaseGateResponse calculate(AiSystem system) {
    return calculate(system, loadControlBlockers(system.id()));
  }

  public ReleaseGateResponse calculate(AiSystem system, List<String> controlBlockers) {
    List<String> blockers = new ArrayList<>();

    if (system.riskClass() == RiskClass.PROHIBITED) {
      blockers.add("Risk class is prohibited for release");
    }
    if (system.riskClass() == RiskClass.HIGH && hasOversightGap(system.openGaps())) {
      blockers.add("High-risk system is missing required human oversight evidence");
    }
    if (system.evalScore() < EVAL_HARD_BLOCK_THRESHOLD) {
      blockers.add("Eval score is below hard release threshold");
    }
    if (system.dataContractStatus() == DataContractStatus.BREACH) {
      blockers.add("Data contract breach is open");
    }
    if (controlBlockers != null) {
      for (String controlBlocker : controlBlockers) {
        if (controlBlocker != null && !controlBlocker.isBlank() && !blockers.contains(controlBlocker)) {
          blockers.add(controlBlocker);
        }
      }
    }

    if (!blockers.isEmpty()) {
      return new ReleaseGateResponse(system.id(), ReleaseDecision.BLOCKED, blockers);
    }

    boolean needsReview = system.evidenceCoverage() < EVIDENCE_PASS_THRESHOLD
        || system.evalScore() < EVAL_PASS_THRESHOLD
        || system.dataContractStatus() == DataContractStatus.WARNING
        || !system.openGaps().isEmpty();

    return new ReleaseGateResponse(
        system.id(),
        needsReview ? ReleaseDecision.REVIEW : ReleaseDecision.PASS,
        List.of());
  }

  private List<String> loadControlBlockers(UUID systemId) {
    if (systemControls == null || controls == null || tenantContext == null || systemId == null) {
      return List.of();
    }
    Map<UUID, String> codes = controls.findAll().stream()
        .map(ControlEntity::toDomain)
        .collect(Collectors.toMap(c -> c.id(), c -> c.code(), (a, b) -> a));
    return systemControls
        .findAllByTenantIdAndSystemIdAndStatus(
            tenantContext.tenantId(), systemId, ControlStatus.BLOCKED)
        .stream()
        .map(SystemControlEntity::controlId)
        .map(id -> "CONTROL:" + codes.getOrDefault(id, "UNKNOWN"))
        .distinct()
        .toList();
  }

  private boolean hasOversightGap(List<String> gaps) {
    return gaps.stream().anyMatch(gap -> gap.toLowerCase().contains("oversight"));
  }
}
