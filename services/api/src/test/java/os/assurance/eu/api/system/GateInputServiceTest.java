package os.assurance.eu.api.system;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class GateInputServiceTest {
  @Test
  void highRiskCoverageCountsRequiredTypesOnly() {
    assertThat(GateInputService.coverage(RiskClass.HIGH, List.of("DPIA", "MODEL_CARD", "OTHER"))).isEqualTo(40);
    assertThat(GateInputService.coverage(RiskClass.HIGH,
        List.of("DPIA", "MODEL_CARD", "POLICY", "CONTROL_MAP", "VENDOR_DOC"))).isEqualTo(100);
  }

  @Test
  void minimalRiskNeedsAPolicy() {
    assertThat(GateInputService.coverage(RiskClass.MINIMAL, List.of())).isZero();
    assertThat(GateInputService.coverage(RiskClass.MINIMAL, List.of("POLICY"))).isEqualTo(100);
  }

  @Test
  void missingGapsNameEachMissingType() {
    assertThat(GateInputService.missingEvidenceGaps(RiskClass.LIMITED, List.of("POLICY")))
        .containsExactly("Missing evidence: MODEL_CARD");
  }
}
