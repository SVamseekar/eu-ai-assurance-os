package os.assurance.eu.api.publicclaims;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import os.assurance.eu.api.system.RiskClass;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PublicClaimsCatalog(
    String disclaimer,
    String retrievedAt,
    String library,
    String howtoPromotion,
    String howtoDataset,
    List<PublicClaimsSystemSpec> systems) {

  public static final String DATA_SOURCE_PREFIX = "public-claims:";
  public static final String CLASSPATH_ROOT = "public-claims";

  public PublicClaimsCatalog {
    disclaimer = disclaimer == null ? "" : disclaimer;
    retrievedAt = retrievedAt == null ? "" : retrievedAt;
    library = library == null ? "evgraph (PyPI). Not bundled in this API." : library;
    howtoPromotion = howtoPromotion == null ? "" : howtoPromotion;
    howtoDataset = howtoDataset == null ? "" : howtoDataset;
    systems = systems == null ? List.of() : List.copyOf(systems);
  }

  public PublicClaimsSystemSpec require(String slug) {
    return systems.stream()
        .filter(spec -> spec.slug().equals(slug))
        .findFirst()
        .orElseThrow(() -> new PublicClaimsNotFoundException(slug));
  }

  public static String dataSource(String slug) {
    return DATA_SOURCE_PREFIX + slug;
  }

  public static String slugFromDataSources(List<String> dataSources) {
    if (dataSources == null) {
      return null;
    }
    for (String source : dataSources) {
      if (source != null && source.startsWith(DATA_SOURCE_PREFIX)) {
        return source.substring(DATA_SOURCE_PREFIX.length());
      }
    }
    return null;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record PublicClaimsSystemSpec(
      String slug,
      String legalName,
      String hq,
      String systemName,
      String owner,
      String purpose,
      RiskClass riskClass,
      String riskBasis,
      String sector,
      String decisionImpact,
      String vendorName,
      String modelName,
      String modelVersion,
      List<String> affectedUsers,
      int evidenceCoverage,
      int evalScore,
      String claimedInProductionAt,
      boolean includeDeployedAt,
      List<String> openGaps,
      List<PublicClaimsSource> sources) {

    public PublicClaimsSystemSpec {
      affectedUsers = affectedUsers == null ? List.of() : List.copyOf(affectedUsers);
      openGaps = openGaps == null ? List.of() : List.copyOf(openGaps);
      sources = sources == null ? List.of() : List.copyOf(sources);
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record PublicClaimsSource(
      String title,
      String url,
      String retrievedAt,
      String quote) {
  }
}
