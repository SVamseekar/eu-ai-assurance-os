package os.assurance.eu.api.auth;

/**
 * Normalized identity claims from an OIDC provider userinfo / ID token.
 */
public record OAuthProviderProfile(String provider, String subject, String email, String displayName) {
  public OAuthProviderProfile {
    if (provider == null || provider.isBlank()) {
      throw new IllegalArgumentException("provider is required");
    }
    if (subject == null || subject.isBlank()) {
      throw new IllegalArgumentException("subject is required");
    }
    if (email == null || email.isBlank()) {
      throw new IllegalArgumentException("email is required");
    }
    if (displayName == null || displayName.isBlank()) {
      displayName = email;
    }
    provider = provider.toLowerCase();
    email = email.trim().toLowerCase();
  }

  /**
   * Parse provider userinfo JSON map fields into a profile.
   * Microsoft may return email via preferred_username or upn.
   */
  public static OAuthProviderProfile fromUserInfo(String provider, java.util.Map<String, Object> userInfo) {
    if (userInfo == null) {
      throw new IllegalArgumentException("userinfo is required");
    }
    Object sub = userInfo.get("sub");
    if (sub == null || sub.toString().isBlank()) {
      throw new IllegalArgumentException("OAuth provider did not return subject (sub)");
    }
    String email = firstNonBlank(
        stringVal(userInfo.get("email")),
        stringVal(userInfo.get("preferred_username")),
        stringVal(userInfo.get("upn")));
    if (email == null) {
      throw new IllegalArgumentException("OAuth provider " + provider + " did not return an email address");
    }
    String name = firstNonBlank(stringVal(userInfo.get("name")), email);
    return new OAuthProviderProfile(provider, sub.toString(), email, name);
  }

  private static String stringVal(Object value) {
    return value == null ? null : value.toString();
  }

  private static String firstNonBlank(String... values) {
    if (values == null) {
      return null;
    }
    for (String value : values) {
      if (value != null && !value.isBlank()) {
        return value;
      }
    }
    return null;
  }
}
