package os.assurance.eu.api.publicclaims;

public class PublicClaimsNotFoundException extends RuntimeException {
  public PublicClaimsNotFoundException(String slug) {
    super("Unknown public-claims slug: " + slug);
  }
}
