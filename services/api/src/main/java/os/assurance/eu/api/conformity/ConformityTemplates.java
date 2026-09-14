package os.assurance.eu.api.conformity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Annex IV / FRIA / DoC / Art. 49 <strong>checklists</strong>. Not legal instruments.
 */
public final class ConformityTemplates {
  public static final String DISCLAIMER =
      "Assisted conformity dossier. This is a structured checklist mapped to EU AI Act artefacts. "
          + "It is not a technical file, not a Declaration of Conformity, not a FRIA, not EU database "
          + "registration, not legal advice, and not certification.";

  private ConformityTemplates() {
  }

  public static Map<String, Object> emptyAnnexIv() {
    Map<String, Object> root = new LinkedHashMap<>();
    root.put("disclaimer", DISCLAIMER);
    root.put("legalRef", "Art. 11 / Annex IV — indicative sections");
    root.put("sections", List.of(
        section("IV.1", "General description of the AI system", "Annex IV §1"),
        section("IV.2", "Development process, design, and data", "Annex IV §2"),
        section("IV.3", "Monitoring, functioning and control", "Annex IV §3"),
        section("IV.4", "Risk management system (Art. 9)", "Annex IV §4 / Art. 9"),
        section("IV.5", "Changes through the lifetime", "Annex IV §5"),
        section("IV.6", "Harmonised standards applied", "Annex IV §6"),
        section("IV.7", "EU declaration of conformity (copy)", "Annex IV §7 / Art. 47"),
        section("IV.8", "Post-market monitoring system", "Annex IV §8 / Art. 72"),
        section("IV.9", "Performance in production", "Annex IV §9")));
    return root;
  }

  public static Map<String, Object> emptyFria() {
    Map<String, Object> root = new LinkedHashMap<>();
    root.put("disclaimer", DISCLAIMER);
    root.put("legalRef", "Art. 27 FRIA — indicative record");
    root.put("status", "NOT_STARTED");
    root.put("summary", "");
    root.put("evidenceRef", "");
    root.put("reviewer", "");
    return root;
  }

  public static Map<String, Object> emptyDeclaration() {
    Map<String, Object> root = new LinkedHashMap<>();
    root.put("disclaimer", DISCLAIMER);
    root.put("legalRef", "Art. 47 Declaration of Conformity — indicative record");
    root.put("status", "NOT_ISSUED");
    root.put("signedBy", "");
    root.put("signedAt", "");
    root.put("evidenceRef", "");
    return root;
  }

  public static Map<String, Object> emptyArt49() {
    Map<String, Object> root = new LinkedHashMap<>();
    root.put("disclaimer", DISCLAIMER);
    root.put("legalRef", "Art. 49 EU database registration — internal tracker only");
    root.put("status", "NOT_REGISTERED");
    root.put("euDatabaseId", "");
    root.put("submittedAt", "");
    root.put("evidenceRef", "");
    return root;
  }

  static Map<String, Object> section(String id, String title, String legalRef) {
    Map<String, Object> row = new LinkedHashMap<>();
    row.put("id", id);
    row.put("title", title);
    row.put("legalRef", legalRef);
    row.put("status", "MISSING");
    row.put("evidenceRef", "");
    row.put("notes", "");
    return row;
  }
}
