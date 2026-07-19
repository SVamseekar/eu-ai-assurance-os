package os.assurance.eu.api.regmonitor;

import java.time.Instant;

/** Intermediate parsed feed entry before persistence. */
public record ParsedFeedItem(
    String externalId,
    String title,
    String summary,
    Instant publishedAt,
    String url) {
}
