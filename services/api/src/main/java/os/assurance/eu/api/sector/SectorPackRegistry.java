package os.assurance.eu.api.sector;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Loads enabled {@link SectorPack} beans according to {@code assurance.sector.packs}.
 */
@Component
public class SectorPackRegistry {
  private static final Logger log = LoggerFactory.getLogger(SectorPackRegistry.class);

  private final List<SectorPack> enabledPacks;
  private final Map<String, SectorPack> byId;
  private final Map<String, SectorPack> bySectorKey;

  public SectorPackRegistry(List<SectorPack> allPacks, SectorProperties properties) {
    List<String> enabledIds = properties.enabledPackIds();
    Map<String, SectorPack> discovered = new LinkedHashMap<>();
    for (SectorPack pack : allPacks == null ? List.<SectorPack>of() : allPacks) {
      if (pack == null || pack.id() == null || pack.id().isBlank()) {
        continue;
      }
      discovered.put(pack.id().toLowerCase(Locale.ROOT), pack);
    }

    List<SectorPack> enabled = new ArrayList<>();
    Map<String, SectorPack> idMap = new LinkedHashMap<>();
    Map<String, SectorPack> sectorMap = new LinkedHashMap<>();

    for (String id : enabledIds) {
      SectorPack pack = discovered.get(id);
      if (pack == null) {
        log.warn("Sector pack '{}' is configured but no SectorPack bean is registered — skipping", id);
        continue;
      }
      enabled.add(pack);
      idMap.put(pack.id().toLowerCase(Locale.ROOT), pack);
      for (String key : pack.sectorKeys() == null ? Set.<String>of() : pack.sectorKeys()) {
        if (key != null && !key.isBlank()) {
          sectorMap.putIfAbsent(key.toLowerCase(Locale.ROOT), pack);
        }
      }
    }

    for (String id : discovered.keySet()) {
      if (!idMap.containsKey(id) && !enabledIds.isEmpty()) {
        log.debug("Sector pack '{}' present as bean but not in assurance.sector.packs", id);
      }
    }

    this.enabledPacks = List.copyOf(enabled);
    this.byId = Map.copyOf(idMap);
    this.bySectorKey = Map.copyOf(sectorMap);
    log.info(
        "Sector packs enabled: {} ({})",
        enabledPacks.stream().map(SectorPack::id).toList(),
        SectorPackDisclaimers.METRICS_LABEL);
  }

  public List<SectorPack> enabledPacks() {
    return enabledPacks;
  }

  public Optional<SectorPack> findById(String packId) {
    if (packId == null || packId.isBlank()) {
      return Optional.empty();
    }
    return Optional.ofNullable(byId.get(packId.toLowerCase(Locale.ROOT)));
  }

  /**
   * Resolve pack for a free-text system registry sector (case-insensitive; aliases via pack keys).
   */
  public Optional<SectorPack> resolveForSector(String sector) {
    if (sector == null || sector.isBlank()) {
      return Optional.empty();
    }
    String normalized = sector.trim().toLowerCase(Locale.ROOT).replace(' ', '_');
    SectorPack direct = bySectorKey.get(normalized);
    if (direct != null) {
      return Optional.of(direct);
    }
    // loose contains match on pack id (e.g. "financial_services" → finance)
    for (SectorPack pack : enabledPacks) {
      if (normalized.contains(pack.id().toLowerCase(Locale.ROOT))) {
        return Optional.of(pack);
      }
      for (String key : pack.sectorKeys()) {
        if (normalized.contains(key.toLowerCase(Locale.ROOT))) {
          return Optional.of(pack);
        }
      }
    }
    return Optional.empty();
  }
}
