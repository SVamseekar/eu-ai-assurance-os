package os.assurance.eu.api.sector;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** API view of an enabled sector pack. */
public record SectorPackView(
    String id,
    String displayName,
    String summary,
    Set<String> sectorKeys,
    List<SectorControlDef> extraControls,
    Map<String, Object> questionnaireDefaults,
    List<SampleEvidenceTemplate> sampleEvidenceTemplates,
    String metricsLabel,
    String disclaimer) {

  public static SectorPackView from(SectorPack pack) {
    return new SectorPackView(
        pack.id(),
        pack.displayName(),
        pack.summary(),
        pack.sectorKeys(),
        pack.extraControls(),
        pack.questionnaireDefaults(),
        pack.sampleEvidenceTemplates(),
        SectorPackDisclaimers.METRICS_LABEL,
        SectorPackDisclaimers.SCOPE);
  }
}
