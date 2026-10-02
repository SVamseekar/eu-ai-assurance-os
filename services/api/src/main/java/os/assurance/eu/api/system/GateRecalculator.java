package os.assurance.eu.api.system;

import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Recomputes coverage, evidence gaps, and the release decision after evidence or eval changes. */
@Service
public class GateRecalculator {
  private final AiSystemRepository systems;
  private final GateInputService gateInputs;
  private final ReleaseGateService releaseGateService;

  public GateRecalculator(
      AiSystemRepository systems,
      GateInputService gateInputs,
      ReleaseGateService releaseGateService) {
    this.systems = systems;
    this.gateInputs = gateInputs;
    this.releaseGateService = releaseGateService;
  }

  @Transactional
  public void recalculate(UUID systemId) {
    if (gateInputs.manualInputs() || systemId == null) {
      return;
    }
    AiSystem system = systems.findById(systemId).orElse(null);
    if (system == null) {
      return;
    }
    AiSystem updated = gateInputs.recompute(system);
    ReleaseDecision decision = releaseGateService.calculate(updated).decision();
    systems.save(new AiSystem(
        updated.id(),
        updated.name(),
        updated.owner(),
        updated.purpose(),
        updated.riskClass(),
        updated.riskBasis(),
        updated.deploymentRegion(),
        updated.evidenceCoverage(),
        updated.evalScore(),
        updated.dataContractStatus(),
        decision,
        updated.openGaps(),
        updated.vendorName(),
        updated.modelName(),
        updated.modelVersion(),
        updated.dataSources(),
        updated.sector(),
        updated.decisionImpact(),
        updated.affectedUsers(),
        updated.createdAt(),
        Instant.now()));
  }
}
