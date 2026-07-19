package os.assurance.eu.api.sector;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Sector integration pack SPI.
 *
 * <p>Each pack is a deep vertical overlay (controls, questionnaire defaults, sample
 * evidence templates) — not a live proprietary vendor connector. Enable packs via
 * {@code assurance.sector.packs=insurance,hr,finance}.
 */
public interface SectorPack {
  /** Stable pack id used in config and APIs (e.g. {@code insurance}). */
  String id();

  String displayName();

  /** Short product-safe blurb for badges and docs. */
  String summary();

  /**
   * Sector registry / questionnaire values this pack applies to
   * (lowercase, e.g. {@code insurance}, {@code claims}).
   */
  Set<String> sectorKeys();

  /** Extra catalog controls overlaid for matching systems. */
  List<SectorControlDef> extraControls();

  /**
   * Default questionnaire answer hints for assisted determination / register UX.
   * Keys match {@code QuestionnaireDefinition} question ids.
   */
  Map<String, Object> questionnaireDefaults();

  /** Sample evidence templates (classpath markdown under {@code sector/}). */
  List<SampleEvidenceTemplate> sampleEvidenceTemplates();
}
