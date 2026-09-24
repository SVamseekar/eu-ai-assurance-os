package os.assurance.eu.api.proposal;

import java.util.List;
import java.util.Locale;

/**
 * Deterministic keyword mapper. A hit is kept only when that provision is in the
 * supplied law index. Tenant evidence is never an input.
 */
public class RulesDocumentMapper {
  public MappingDraft map(String title, String text, List<LawProvision> lawIndex) {
    String haystack = ((title == null ? "" : title) + " " + (text == null ? "" : text)).toLowerCase(Locale.ROOT);
    String excerpt = excerpt(text);
    LawProvision annex = find(lawIndex, "02024R1689-20260727#annexIII::");
    if (annex != null && containsAny(haystack, "annex iii", "high-risk", "high risk", "credit-scoring", "credit scoring")) {
      return cite(annex, excerpt);
    }
    LawProvision article50 = find(lawIndex, "02024R1689-20260727#50:1:");
    if (article50 != null && containsAny(haystack, "natural persons", "interact with an ai system")) {
      return cite(article50, excerpt);
    }
    LawProvision dora = find(lawIndex, "02022R2554-20221227#1:1:");
    if (dora != null && containsAny(haystack, "network and information systems", "financial entities")) {
      return cite(dora, excerpt);
    }
    LawProvision gdpr = find(lawIndex, "02016R0679-20160504#5:1:");
    if (gdpr != null && haystack.contains("processed lawfully")) {
      return cite(gdpr, excerpt);
    }
    return MappingDraft.abstain(excerpt);
  }

  private static MappingDraft cite(LawProvision provision, String excerpt) {
    return new MappingDraft(
        "supports",
        provision.provisionKey(),
        provision.forceStatus(),
        provision.forceFrom(),
        excerpt);
  }

  private static LawProvision find(List<LawProvision> lawIndex, String provisionKey) {
    if (lawIndex == null) {
      return null;
    }
    for (LawProvision provision : lawIndex) {
      if (provisionKey.equals(provision.provisionKey())) {
        return provision;
      }
    }
    return null;
  }

  private static boolean containsAny(String text, String... needles) {
    for (String needle : needles) {
      if (text.contains(needle)) {
        return true;
      }
    }
    return false;
  }

  private static String excerpt(String text) {
    if (text == null || text.isBlank()) {
      return null;
    }
    String trimmed = text.strip();
    return trimmed.length() <= 500 ? trimmed : trimmed.substring(0, 500);
  }
}
