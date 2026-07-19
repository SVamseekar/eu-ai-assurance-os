package os.assurance.eu.api.regmonitor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RegItem(
    UUID id,
    UUID sourceId,
    String externalId,
    String title,
    String summary,
    Instant publishedAt,
    String url,
    String contentHash,
    Instant fetchedAt,
    List<RegImpactHint> impactHints) {

  public RegItem {
    impactHints = impactHints == null ? List.of() : List.copyOf(impactHints);
  }
}
