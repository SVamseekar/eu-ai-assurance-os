package os.assurance.eu.api.billing;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class EntitlementServiceTest {
  private static final Instant NOW = Instant.parse("2026-11-10T12:00:00Z");

  @Test
  void activeSubscriptionWins() {
    assertThat(EntitlementService.resolve("trial", NOW.plusSeconds(3600), "BUSINESS", "ACTIVE", null, NOW))
        .isEqualTo(PlanCatalog.BUSINESS);
  }

  @Test
  void trialBeforeEndIsTrialAfterEndIsFree() {
    assertThat(EntitlementService.resolve("trial", NOW.plusSeconds(60), null, null, null, NOW)).isEqualTo(PlanCatalog.TRIAL);
    assertThat(EntitlementService.resolve("trial", NOW.minusSeconds(60), null, null, null, NOW)).isEqualTo(PlanCatalog.FREE);
  }

  @Test
  void onHoldKeepsPlanDuringGraceThenFree() {
    assertThat(EntitlementService.resolve("trial", NOW.minusSeconds(60), "TEAM", "ON_HOLD", NOW.plusSeconds(3600), NOW))
        .isEqualTo(PlanCatalog.TEAM);
    assertThat(EntitlementService.resolve("trial", NOW.minusSeconds(60), "TEAM", "ON_HOLD", NOW.minusSeconds(1), NOW))
        .isEqualTo(PlanCatalog.FREE);
  }

  @Test
  void cancelledKeepsPlanUntilPeriodEndViaGraceField() {
    assertThat(EntitlementService.resolve("free", null, "TEAM", "CANCELLED", NOW.plusSeconds(86400), NOW))
        .isEqualTo(PlanCatalog.TEAM);
  }

  @Test
  void operatorSetPlansAreHonoured() {
    assertThat(EntitlementService.resolve("enterprise", null, null, null, null, NOW)).isEqualTo(PlanCatalog.ENTERPRISE);
    assertThat(EntitlementService.resolve("demo", null, null, null, null, NOW)).isEqualTo(PlanCatalog.DEMO);
    assertThat(EntitlementService.resolve("design-partner", null, null, null, null, NOW)).isEqualTo(PlanCatalog.BUSINESS);
    // The bootstrap tenant ("starter") is the operator's own workspace, not a customer plan: it is unlimited.
    assertThat(EntitlementService.resolve("starter", null, null, null, null, NOW)).isEqualTo(PlanCatalog.ENTERPRISE);
  }

  @Test
  void catalogLimitsMatchThePricingTable() {
    assertThat(PlanCatalog.FREE.gatedSystems()).isEqualTo(1);
    assertThat(PlanCatalog.FREE.gateRunsPerMonth()).isEqualTo(300);
    assertThat(PlanCatalog.TEAM.gatedSystems()).isEqualTo(3);
    assertThat(PlanCatalog.TEAM.editorSeats()).isEqualTo(10);
    assertThat(PlanCatalog.BUSINESS.gatedSystems()).isEqualTo(15);
    assertThat(PlanCatalog.TEAM.features()).contains(Feature.SIGNED_PDF).doesNotContain(Feature.SSO);
  }
}
