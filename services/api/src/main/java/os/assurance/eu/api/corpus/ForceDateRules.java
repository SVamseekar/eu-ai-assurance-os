package os.assurance.eu.api.corpus;

import java.time.LocalDate;

/**
 * Force dates for the pinned five-act corpus. Annex III high-risk duties stay future
 * until 2 December 2027. Data Act Article 3(1) follows the placement date in Article 50,
 * not the general 12 September 2025 application date.
 */
public final class ForceDateRules {
  public ForceAssignment assign(String celex, ParsedProvision provision, LocalDate asOf) {
    LocalDate forceFrom = forceFrom(celex, provision);
    String scopeNote = scopeNote(celex, provision);
    ForceStatus status = forceFrom.isAfter(asOf) ? ForceStatus.FUTURE : ForceStatus.IN_FORCE;
    return new ForceAssignment(status, forceFrom, scopeNote);
  }

  private static LocalDate forceFrom(String celex, ParsedProvision provision) {
    String id = celex == null ? "" : celex.toUpperCase();
    if (id.contains("2024R1689")) {
      if (isAnnex(provision, "III")) {
        return LocalDate.of(2027, 12, 2);
      }
      if (isAnnex(provision, "I")) {
        return LocalDate.of(2028, 8, 2);
      }
      if ("50".equals(provision.article())) {
        return LocalDate.of(2026, 8, 2);
      }
      return LocalDate.of(2026, 8, 2);
    }
    if (id.contains("2026R1744")) {
      return LocalDate.of(2026, 7, 27);
    }
    if (id.contains("2016R0679")) {
      return LocalDate.of(2018, 5, 25);
    }
    if (id.contains("2022R2554")) {
      return LocalDate.of(2025, 1, 17);
    }
    if (id.contains("2022R2065")) {
      return LocalDate.of(2024, 2, 17);
    }
    if (id.contains("2023R2854")) {
      if ("3".equals(provision.article()) && "1".equals(provision.paragraph())) {
        return LocalDate.of(2026, 9, 12);
      }
      return LocalDate.of(2025, 9, 12);
    }
    throw new IllegalArgumentException("No force date for " + celex);
  }

  private static String scopeNote(String celex, ParsedProvision provision) {
    String id = celex == null ? "" : celex.toUpperCase();
    if (id.contains("2023R2854")
        && "3".equals(provision.article())
        && "1".equals(provision.paragraph())) {
      return "Applies to connected products placed on the market after 12 Sep 2026";
    }
    return null;
  }

  private static boolean isAnnex(ParsedProvision provision, String roman) {
    String annex = provision.annex() == null ? "" : provision.annex();
    return annex.equals(roman) || annex.equals("annex" + roman);
  }
}
