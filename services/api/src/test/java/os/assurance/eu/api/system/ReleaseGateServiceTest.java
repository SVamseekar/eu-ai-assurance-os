package os.assurance.eu.api.system;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ReleaseGateServiceTest {
  private final ReleaseGateService service = new ReleaseGateService();

  @Test
  void blocksHighRiskSystemWithOversightGap() {
    ReleaseGateResponse response = service.calculate(system(
        RiskClass.HIGH,
        90,
        88,
        DataContractStatus.HEALTHY,
        List.of("Human oversight SOP missing")));

    assertThat(response.decision()).isEqualTo(ReleaseDecision.BLOCKED);
    assertThat(response.blockers()).contains("High-risk system is missing required human oversight evidence");
  }

  @Test
  void blocksDataContractBreach() {
    ReleaseGateResponse response = service.calculate(system(
        RiskClass.LIMITED,
        90,
        88,
        DataContractStatus.BREACH,
        List.of()));

    assertThat(response.decision()).isEqualTo(ReleaseDecision.BLOCKED);
    assertThat(response.blockers()).contains("Data contract breach is open");
  }

  @Test
  void sendsNearMissesToReview() {
    ReleaseGateResponse response = service.calculate(system(
        RiskClass.LIMITED,
        81,
        84,
        DataContractStatus.WARNING,
        List.of()));

    assertThat(response.decision()).isEqualTo(ReleaseDecision.REVIEW);
    assertThat(response.blockers()).isEmpty();
  }

  @Test
  void passesCleanSystem() {
    ReleaseGateResponse response = service.calculate(system(
        RiskClass.MINIMAL,
        90,
        90,
        DataContractStatus.HEALTHY,
        List.of()));

    assertThat(response.decision()).isEqualTo(ReleaseDecision.PASS);
    assertThat(response.blockers()).isEmpty();
  }

  private AiSystem system(
      RiskClass riskClass,
      int evidenceCoverage,
      int evalScore,
      DataContractStatus dataContractStatus,
      List<String> openGaps) {
    Instant now = Instant.now();
    return new AiSystem(
        UUID.randomUUID(),
        "Test System",
        "Test Owner",
        "Test purpose",
        riskClass,
        "Test basis",
        "EU",
        evidenceCoverage,
        evalScore,
        dataContractStatus,
        ReleaseDecision.REVIEW,
        openGaps,
        now,
        now);
  }
}
