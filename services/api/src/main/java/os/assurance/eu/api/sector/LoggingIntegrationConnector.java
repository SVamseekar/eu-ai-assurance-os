package os.assurance.eu.api.sector;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import os.assurance.eu.api.system.ReleaseDecision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Default connector stub: logs intents only. Replace with a real bean when vendor OAuth is available.
 */
@Component
public class LoggingIntegrationConnector implements IntegrationConnector {
  private static final Logger log = LoggerFactory.getLogger(LoggingIntegrationConnector.class);

  @Override
  public String id() {
    return "logging-stub";
  }

  @Override
  public String displayName() {
    return "Logging connector stub (no live vendor)";
  }

  @Override
  public void pushReleaseDecision(UUID systemId, ReleaseDecision decision, Map<String, Object> context) {
    log.info(
        "IntegrationConnector[{}] pushReleaseDecision systemId={} decision={} contextKeys={} — no external call",
        id(),
        systemId,
        decision,
        context == null ? List.of() : context.keySet());
  }

  @Override
  public List<Map<String, Object>> pullModelInventory() {
    log.info("IntegrationConnector[{}] pullModelInventory — returning empty (stub)", id());
    return List.of();
  }
}
