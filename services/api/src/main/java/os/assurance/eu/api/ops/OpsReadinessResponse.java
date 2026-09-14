package os.assurance.eu.api.ops;

import java.util.List;
import java.util.Map;

/**
 * Operator checklist. Not an uptime SLA and not a SOC 2 report.
 */
public record OpsReadinessResponse(
    String disclaimer,
    boolean productionReady,
    Map<String, Boolean> checks,
    List<String> blockers) {
}
