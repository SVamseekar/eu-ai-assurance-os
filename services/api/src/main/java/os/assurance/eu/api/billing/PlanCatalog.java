package os.assurance.eu.api.billing;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

public enum PlanCatalog {
  FREE(1, 3, 300, EnumSet.noneOf(Feature.class)),
  TRIAL(15, -1, -1, EnumSet.allOf(Feature.class)),
  TEAM(3, 10, -1, EnumSet.of(Feature.SIGNED_PDF, Feature.NIST_CROSSWALK)),
  BUSINESS(15, -1, -1, EnumSet.allOf(Feature.class)),
  ENTERPRISE(-1, -1, -1, EnumSet.allOf(Feature.class)),
  DEMO(-1, -1, -1, EnumSet.allOf(Feature.class));

  private final int gatedSystems;
  private final int editorSeats;
  private final int gateRunsPerMonth;
  private final Set<Feature> features;

  PlanCatalog(int gatedSystems, int editorSeats, int gateRunsPerMonth, Set<Feature> features) {
    this.gatedSystems = gatedSystems;
    this.editorSeats = editorSeats;
    this.gateRunsPerMonth = gateRunsPerMonth;
    this.features = features;
  }

  public int gatedSystems() { return gatedSystems; }
  public int editorSeats() { return editorSeats; }
  public int gateRunsPerMonth() { return gateRunsPerMonth; }
  public Set<Feature> features() { return features; }

  /** Maps tenant.plan codes to catalog entries. "starter" is the bootstrap workspace (unlimited, not sold). */
  public static PlanCatalog fromCode(String code) {
    if (code == null) {
      return FREE;
    }
    return switch (code.trim().toLowerCase(Locale.ROOT)) {
      case "trial" -> TRIAL;
      case "team" -> TEAM;
      case "business", "design-partner" -> BUSINESS;
      case "enterprise", "starter" -> ENTERPRISE;
      case "demo" -> DEMO;
      default -> FREE;
    };
  }
}
