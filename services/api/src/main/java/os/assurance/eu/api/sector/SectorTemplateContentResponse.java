package os.assurance.eu.api.sector;

public record SectorTemplateContentResponse(
    String packId,
    String templateId,
    String title,
    String documentType,
    String resourcePath,
    String contentMarkdown,
    String disclaimer) {
}
