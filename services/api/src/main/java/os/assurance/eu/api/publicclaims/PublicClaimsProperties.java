package os.assurance.eu.api.publicclaims;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "assurance.demo")
public class PublicClaimsProperties {
  /**
   * When true, register the public-claims teasers as systems in the current
   * default tenant. Must stay false on a paying customer's postgres tenant.
   */
  private boolean publicClaims = false;

  public boolean isPublicClaims() {
    return publicClaims;
  }

  public void setPublicClaims(boolean publicClaims) {
    this.publicClaims = publicClaims;
  }
}
