package os.assurance.eu.api.auth;

import java.util.Locale;
import java.util.Map;

/**
 * Normalized identity claims from an OIDC provider userinfo / ID token.
 * {@code emailVerified} is true only when the provider asserts ownership of the email:
 * Google {@code email_verified=true}; Microsoft {@code xms_edov=true} (optional claim that must be
 * enabled on the Entra app registration). Microsoft {@code preferred_username}/{@code upn} are never proof.
 */
public record OAuthProviderProfile(
    String provider, String subject, String email, String displayName, boolean emailVerified) {

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
    provider = provider.toLowerCase(Locale.ROOT);
    email = email.trim().toLowerCase(Locale.ROOT);
  }

  public OAuthProviderProfile(String provider, String subject, String email, String displayName) {
    this(provider, subject, email, displayName, false);
  }

  public static OAuthProviderProfile fromUserInfo(String provider, Map<String, Object> userInfo) {
    if (userInfo == null) {
      throw new IllegalArgumentException("userinfo is required");
    }
    Object sub = userInfo.get("sub");
    if (sub == null || sub.toString().isBlank()) {
      throw new IllegalArgumentException("OAuth provider did not return subject (sub)");
    }
    String normalizedProvider = provider == null ? "" : provider.toLowerCase(Locale.ROOT);
    String email = firstNonBlank(
        stringVal(userInfo.get("email")),
        stringVal(userInfo.get("preferred_username")),
        stringVal(userInfo.get("upn")));
    if (email == null) {
      throw new IllegalArgumentException("OAuth provider " + provider + " did not return an email address");
    }
    boolean verified = switch (normalizedProvider) {
      case "google" -> isTrue(userInfo.get("email_verified")) && stringVal(userInfo.get("email")) != null;
      case "microsoft" -> isTrue(userInfo.get("xms_edov")) && stringVal(userInfo.get("email")) != null;
      default -> false;
    };
    String name = firstNonBlank(stringVal(userInfo.get("name")), email);
    return new OAuthProviderProfile(provider, sub.toString(), email, name, verified);
  }

  private static boolean isTrue(Object value) {
    if (value instanceof Boolean b) {
      return b;
    }
    return value != null && "true".equalsIgnoreCase(value.toString().trim());
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
