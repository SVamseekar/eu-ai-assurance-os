package os.assurance.eu.api.sector;

import java.util.List;

public record SectorPacksResponse(
    List<SectorPackView> packs,
    String metricsLabel,
    String disclaimer,
    String notAllIndustriesNote) {

  public static SectorPacksResponse of(List<SectorPackView> packs) {
    return new SectorPacksResponse(
        packs,
        SectorPackDisclaimers.METRICS_LABEL,
        SectorPackDisclaimers.SCOPE,
        SectorPackDisclaimers.NOT_ALL_INDUSTRIES);
  }
}
