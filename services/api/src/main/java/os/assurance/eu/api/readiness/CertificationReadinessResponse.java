package os.assurance.eu.api.readiness;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import os.assurance.eu.api.system.ReleaseDecision;

/**
 * Certification readiness report for an AI system.
 *
 * <p>Never includes a {@code certified} field. Use {@link #readinessStatus()} only.
 */
public record CertificationReadinessResponse(
    UUID systemId,
    String systemName,
    int score,
    CertificationReadinessStatus readinessStatus,
    String productLabel,
    String disclaimer,
    Instant generatedAt,
    ReleaseDecision releaseDecision,
    List<ReadinessDimensionScore> dimensions,
    List<ReadinessGap> gaps) {

  public CertificationReadinessResponse {
    dimensions = dimensions == null ? List.of() : List.copyOf(dimensions);
    gaps = gaps == null ? List.of() : List.copyOf(gaps);
    productLabel = productLabel == null
        ? CertificationReadinessDisclaimers.PRODUCT_LABEL
        : productLabel;
    disclaimer = disclaimer == null
        ? CertificationReadinessDisclaimers.FULL
        : disclaimer;
  }
}
