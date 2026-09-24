package os.assurance.eu.api.corpus;

public record GuidanceSeed(
    String sourceKey,
    String title,
    String authorityRank,
    String relation,
    String interpretsProvisionKey,
    String bodyResource) {
}
