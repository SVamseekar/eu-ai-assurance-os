package os.assurance.eu.api.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OAuth/OIDC settings for Google and Microsoft (Part 4).
 *
 * <p>{@code autoProvision} defaults to {@code false} so production never self-serves
 * unknown accounts unless an operator explicitly enables it.
 *
 * <p>Registered via {@link OAuthConfig} {@code @EnableConfigurationProperties} (do not also
 * annotate with {@code @Component} — that creates a duplicate bean).
 */
@ConfigurationProperties(prefix = "assurance.oauth")
public class OAuthProperties {
  private boolean autoProvision = false;
  private String redirectBaseUrl = "http://localhost:3000";
  private String stateSecret = "local-dev-oauth-state-secret-change-me";
  private Provider google = new Provider();
  private Provider microsoft = new Provider();

  public boolean isAutoProvision() {
    return autoProvision;
  }

  public void setAutoProvision(boolean autoProvision) {
    this.autoProvision = autoProvision;
  }

  public String getRedirectBaseUrl() {
    return redirectBaseUrl;
  }

  public void setRedirectBaseUrl(String redirectBaseUrl) {
    this.redirectBaseUrl = redirectBaseUrl;
  }

  public String getStateSecret() {
    return stateSecret;
  }

  public void setStateSecret(String stateSecret) {
    this.stateSecret = stateSecret;
  }

  public Provider getGoogle() {
    return google;
  }

  public void setGoogle(Provider google) {
    this.google = google;
  }

  public Provider getMicrosoft() {
    return microsoft;
  }

  public void setMicrosoft(Provider microsoft) {
    this.microsoft = microsoft;
  }

  public Provider provider(String name) {
    if ("google".equalsIgnoreCase(name)) {
      return google;
    }
    if ("microsoft".equalsIgnoreCase(name)) {
      return microsoft;
    }
    return null;
  }

  public static class Provider {
    private String clientId = "";
    private String clientSecret = "";

    public String getClientId() {
      return clientId;
    }

    public void setClientId(String clientId) {
      this.clientId = clientId;
    }

    public String getClientSecret() {
      return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
      this.clientSecret = clientSecret;
    }

    public boolean isConfigured() {
      return clientId != null && !clientId.isBlank()
          && clientSecret != null && !clientSecret.isBlank();
    }
  }
}
