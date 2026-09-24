package os.assurance.eu.api.system;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Article 72 stand-in. The Commission template is due 2 Sep 2027 and is not
 * included. Annex III technical-documentation monitoring stays future until
 * 2 Dec 2027. Annex I stays future until 2 Aug 2028.
 */
public final class MonitoringPlan {
  private MonitoringPlan() {
  }

  public static Map<String, Object> standIn() {
    Map<String, Object> plan = new LinkedHashMap<>();
    plan.put("signals", List.of(
        signal("eval drift", "/api/v1/eval-runs"),
        signal("contract drift", "/api/v1/data-contracts/{contractId}/drift-events"),
        signal("reg-monitor diff", "/api/v1/reg-monitor/items")));
    plan.put("commissionTemplateDue", "2027-09-02");
    plan.put("annexIiiTechnicalDocumentationMonitoring", future("2027-12-02"));
    plan.put("annexI", future("2028-08-02"));
    return plan;
  }

  private static Map<String, String> signal(String name, String path) {
    Map<String, String> signal = new LinkedHashMap<>();
    signal.put("name", name);
    signal.put("path", path);
    return signal;
  }

  private static Map<String, String> future(String until) {
    Map<String, String> duty = new LinkedHashMap<>();
    duty.put("forceStatus", "FUTURE");
    duty.put("until", until);
    return duty;
  }
}
