package os.assurance.eu.api.system;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MonitoringPlanTest {

  @Test
  void standInIsTheThreeExistingPathsUntilTheCommissionDate() {
    Map<String, Object> plan = MonitoringPlan.standIn();

    assertThat(plan.get("signals")).isEqualTo(List.of(
        signal("eval drift", "/api/v1/eval-runs"),
        signal("contract drift", "/api/v1/data-contracts/{contractId}/drift-events"),
        signal("reg-monitor diff", "/api/v1/reg-monitor/items")));
    assertThat(plan.get("commissionTemplateDue")).isEqualTo("2027-09-02");
    assertThat(plan.get("annexIiiTechnicalDocumentationMonitoring")).isEqualTo(future("2027-12-02"));
    assertThat(plan.get("annexI")).isEqualTo(future("2028-08-02"));
    assertThat(plan).doesNotContainKey("template");
    assertThat(plan).doesNotContainKey("connector");
    assertThat(plan.toString().toLowerCase()).doesNotContain("compliant", "certified", "conformity");
  }

  private static Map<String, String> signal(String name, String path) {
    return Map.of("name", name, "path", path);
  }

  private static Map<String, String> future(String until) {
    return Map.of("forceStatus", "FUTURE", "until", until);
  }
}
