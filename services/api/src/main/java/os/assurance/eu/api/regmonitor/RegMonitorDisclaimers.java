package os.assurance.eu.api.regmonitor;

/**
 * Product-safe framing for the regulatory change monitoring feed.
 * Near-real-time poll only — not magical real-time law, not an official bulletin.
 */
public final class RegMonitorDisclaimers {
  public static final String PRODUCT_LABEL = "Regulatory change monitoring feed";

  public static final String SHORT =
      "Assistive polled regulatory change feed with impact hints. "
          + "Not an official legal bulletin and not legal advice.";

  public static final String FULL =
      "Regulatory change monitoring is an assistive feed produced by near-real-time polling "
          + "(configurable interval, default every N minutes when network sources are enabled). "
          + "It is not an official government or Official Journal legal bulletin, not real-time law, "
          + "and not legal advice. Impact hints map keywords to control/obligation codes and prefer "
          + "UNCERTAIN impact. The feed never auto-changes risk class or control status; a human "
          + "must review relevance before any governance mutation.";

  public static final String LATENCY_NOTE =
      "Latency is bounded by poll interval and source availability, not continuous legal push.";

  private RegMonitorDisclaimers() {
  }
}
