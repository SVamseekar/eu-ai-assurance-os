package os.assurance.eu.api.system;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ReleaseGateService {
  private static final int EVIDENCE_PASS_THRESHOLD = 82;
  private static final int EVAL_PASS_THRESHOLD = 85;
  private static final int EVAL_HARD_BLOCK_THRESHOLD = 78;

  public ReleaseGateResponse calculate(AiSystem system) {
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

  private boolean hasOversightGap(List<String> gaps) {
    return gaps.stream().anyMatch(gap -> gap.toLowerCase().contains("oversight"));
  }
}
