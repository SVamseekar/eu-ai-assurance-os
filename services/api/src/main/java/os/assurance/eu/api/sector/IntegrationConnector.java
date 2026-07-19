package os.assurance.eu.api.sector;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import os.assurance.eu.api.system.ReleaseDecision;

/**
 * Generic integration boundary for external systems of record (Workday, Guidewire, etc.).
 *
 * <p>v1 ships logging / no-op stubs only. Real proprietary connectors require OAuth apps
 * and vendor credentials and must not be claimed as live without them.
 */
public interface IntegrationConnector {
  String id();

  String displayName();

  /** Push a release-gate decision to an external system (stub may only log). */
  void pushReleaseDecision(UUID systemId, ReleaseDecision decision, Map<String, Object> context);

  /** Pull model inventory from an external registry (stub returns empty list). */
  List<Map<String, Object>> pullModelInventory();
}
