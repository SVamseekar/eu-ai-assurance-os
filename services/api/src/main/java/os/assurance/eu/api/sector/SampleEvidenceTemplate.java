package os.assurance.eu.api.sector;

/**
 * Sample evidence template shipped with a sector pack (markdown resource path + metadata).
 * Templates are illustrative starter content, not legal advice or certified documents.
 */
public record SampleEvidenceTemplate(
    String id,
    String title,
    String documentType,
    String resourcePath,
    String description) {
}
