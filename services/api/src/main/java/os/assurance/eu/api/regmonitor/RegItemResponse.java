package os.assurance.eu.api.regmonitor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RegItemResponse(
    UUID id,
    UUID sourceId,
    String sourceCode,
    String externalId,
    String title,
    String summary,
    Instant publishedAt,
    String url,
    String contentHash,
    Instant fetchedAt,
    List<RegImpactHint> impactHints,
    boolean reviewed,
    Instant reviewedAt,
    String reviewNotes,
    String relevanceReason,
    String productLabel,
    String disclaimer) {
}
