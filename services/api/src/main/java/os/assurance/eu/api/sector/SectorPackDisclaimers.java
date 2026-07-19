package os.assurance.eu.api.sector;

/**
 * Honest product language for sector packs — never claim all-industry coverage
 * or live proprietary vendor connectors without real OAuth apps.
 */
public final class SectorPackDisclaimers {
  public static final String METRICS_LABEL = "3 sector packs + SPI";

  public static final String SCOPE =
      "Sector packs are vertical overlays (controls, questionnaire defaults, sample templates) "
          + "plus a connector SPI. They are not live production connectors to Workday, Guidewire, "
          + "or other proprietary vendors, and they do not constitute legal advice or certification.";

  public static final String NOT_ALL_INDUSTRIES =
      "Shipped as " + METRICS_LABEL + " — not \"all industries integrated\".";

  private SectorPackDisclaimers() {}
}
