package os.assurance.eu.api.corpus;

import java.time.LocalDate;

/**
 * Force dates for the pinned five-act corpus. AI Act dates follow Article 113 as amended by
 * Regulation (EU) 2026/1744: Chapters I and II from 2 February 2025; Chapter III Sections 1 to 3
 * (Articles 6 to 27) and Annex III from 2 December 2027, or 2 August 2028 for Annex I systems;
 * everything else from 2 August 2026. Data Act Article 3(1) follows the placement date in
 * Article 50, not the general 12 September 2025 application date.
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
      if (isAnnex(provision, "IV")) {
        return LocalDate.of(2027, 12, 2);
      }
      int article = articleNumber(provision);
      if (article >= 1 && article <= 5) {
        return LocalDate.of(2025, 2, 2);
      }
      if (article >= 6 && article <= 27) {
        return LocalDate.of(2027, 12, 2);
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
    if (id.contains("2024R1689")) {
      int article = articleNumber(provision);
      if (article == 5) {
        return "Article 5(1), points (ba) and (bb), and Article 5(1a) and (1b) apply from 2 Dec 2026";
      }
      if ((article >= 6 && article <= 27) || isAnnex(provision, "IV")) {
        return "Applies from 2 Aug 2028 for high-risk AI systems under Article 6(1) and Annex I";
      }
    }
    if (id.contains("2023R2854")
        && "3".equals(provision.article())
        && "1".equals(provision.paragraph())) {
      return "Applies to connected products placed on the market after 12 Sep 2026";
    }
    return null;
  }

  private static int articleNumber(ParsedProvision provision) {
    String article = provision.article() == null ? "" : provision.article().trim();
    return article.matches("\\d+") ? Integer.parseInt(article) : -1;
  }

  private static boolean isAnnex(ParsedProvision provision, String roman) {
    String annex = provision.annex() == null ? "" : provision.annex();
    return annex.equals(roman) || annex.equals("annex" + roman);
  }
}
