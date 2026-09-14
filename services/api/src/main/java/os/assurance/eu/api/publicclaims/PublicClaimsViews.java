package os.assurance.eu.api.publicclaims;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import os.assurance.eu.api.publicclaims.PublicClaimsCatalog.PublicClaimsSource;

public final class PublicClaimsViews {
  private PublicClaimsViews() {
  }

  public record Index(
      String disclaimer,
      String retrievedAt,
      boolean seedingEnabled,
      String library,
      String howtoPromotion,
      String howtoDataset,
      List<Teaser> systems) {
  }

  public record Teaser(
      String slug,
      String legalName,
      String hq,
      String systemName,
      String purpose,
      String riskClass,
      String riskBasis,
      String sector,
      String vendorName,
      String modelName,
      UUID registeredSystemId,
      String releaseDecision,
      List<PublicClaimsSource> sources,
      List<String> openGaps) {
  }

  public record Artifacts(
      String slug,
      String disclaimer,
      String library,
      String howtoPromotion,
      String howtoDataset,
      Map<String, Object> model_card,
      Map<String, Object> approval,
      Map<String, Object> deployment,
      String dataset_manifest_csv) {
  }
}
