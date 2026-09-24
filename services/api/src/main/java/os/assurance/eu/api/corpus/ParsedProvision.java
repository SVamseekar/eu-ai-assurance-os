package os.assurance.eu.api.corpus;

public record ParsedProvision(
    String provisionKey,
    String article,
    String paragraph,
    String point,
    String annex,
    String text) {
}
