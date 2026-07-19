package os.assurance.eu.api.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Authorization-code exchange + userinfo for Google and Microsoft OIDC.
 * Extracted for testability (mockable).
 */
@Component
public class OAuthTokenClient {
  private static final Duration TIMEOUT = Duration.ofSeconds(15);

  private final OAuthProperties properties;
  private final ObjectMapper objectMapper;
  private final HttpClient httpClient;

  @Autowired
  public OAuthTokenClient(OAuthProperties properties, ObjectMapper objectMapper) {
    this.properties = properties;
    this.objectMapper = objectMapper;
    this.httpClient = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();
  }

  public Map<String, Object> exchangeCode(String provider, String code, String redirectUri) {
    OAuthProperties.Provider config = requireConfigured(provider);
    String tokenUrl = tokenEndpoint(provider);
    String body = form(
        "grant_type", "authorization_code",
        "code", code,
        "redirect_uri", redirectUri,
        "client_id", config.getClientId(),
        "client_secret", config.getClientSecret());
    try {
      HttpRequest request = HttpRequest.newBuilder(URI.create(tokenUrl))
          .timeout(TIMEOUT)
          .header("Content-Type", "application/x-www-form-urlencoded")
          .header("Accept", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(body))
          .build();
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() < 200 || response.statusCode() >= 300) {
        throw new OAuthExchangeException("token_exchange_failed", "Provider token endpoint returned "
            + response.statusCode());
      }
      return toMap(objectMapper.readTree(response.body()));
    } catch (OAuthExchangeException e) {
      throw e;
    } catch (Exception e) {
      throw new OAuthExchangeException("token_exchange_failed", e.getMessage(), e);
    }
  }

  public Map<String, Object> fetchUserInfo(String provider, Map<String, Object> tokenResponse) {
    Object userinfoNode = tokenResponse.get("userinfo");
    if (userinfoNode instanceof Map<?, ?> map) {
      @SuppressWarnings("unchecked")
      Map<String, Object> asMap = (Map<String, Object>) map;
      return asMap;
    }
    // Prefer ID token claims when present (Google often embeds userinfo there).
    Object idToken = tokenResponse.get("id_token");
    if (idToken != null && !idToken.toString().isBlank()) {
      Map<String, Object> fromIdToken = parseIdTokenClaims(idToken.toString());
      if (fromIdToken.containsKey("sub") && (fromIdToken.containsKey("email")
          || fromIdToken.containsKey("preferred_username")
          || fromIdToken.containsKey("upn"))) {
        return fromIdToken;
      }
    }
    Object accessToken = tokenResponse.get("access_token");
    if (accessToken == null || accessToken.toString().isBlank()) {
      throw new OAuthExchangeException("userinfo_failed", "No access_token or usable id_token");
    }
    String userInfoUrl = userInfoEndpoint(provider);
    try {
      HttpRequest request = HttpRequest.newBuilder(URI.create(userInfoUrl))
          .timeout(TIMEOUT)
          .header("Authorization", "Bearer " + accessToken)
          .header("Accept", "application/json")
          .GET()
          .build();
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() < 200 || response.statusCode() >= 300) {
        throw new OAuthExchangeException("userinfo_failed", "Userinfo endpoint returned "
            + response.statusCode());
      }
      return toMap(objectMapper.readTree(response.body()));
    } catch (OAuthExchangeException e) {
      throw e;
    } catch (Exception e) {
      throw new OAuthExchangeException("userinfo_failed", e.getMessage(), e);
    }
  }

  public String buildAuthorizationUrl(String provider, String state, String redirectUri) {
    OAuthProperties.Provider config = requireConfigured(provider);
    String base = authorizeEndpoint(provider);
    String url = base
        + "?client_id=" + enc(config.getClientId())
        + "&response_type=code"
        + "&scope=" + enc("openid email profile")
        + "&redirect_uri=" + enc(redirectUri)
        + "&state=" + enc(state);
    if ("google".equalsIgnoreCase(provider)) {
      // Local/dev UX: always show account picker; online access is enough for sign-in.
      url += "&access_type=online&prompt=select_account&include_granted_scopes=true";
    }
    if ("microsoft".equalsIgnoreCase(provider)) {
      url += "&response_mode=query";
    }
    return url;
  }

  public String callbackRedirectUri(String provider) {
    String base = properties.getRedirectBaseUrl();
    if (base.endsWith("/")) {
      base = base.substring(0, base.length() - 1);
    }
    return base + "/api/auth/oauth/" + provider.toLowerCase() + "/callback";
  }

  private OAuthProperties.Provider requireConfigured(String provider) {
    OAuthProperties.Provider config = properties.provider(provider);
    if (config == null || !config.isConfigured()) {
      throw new OAuthExchangeException("not_configured", "OAuth provider not configured: " + provider);
    }
    return config;
  }

  private static String authorizeEndpoint(String provider) {
    if ("google".equalsIgnoreCase(provider)) {
      return "https://accounts.google.com/o/oauth2/v2/auth";
    }
    if ("microsoft".equalsIgnoreCase(provider)) {
      return "https://login.microsoftonline.com/common/oauth2/v2.0/authorize";
    }
    throw new OAuthExchangeException("unsupported_provider", "Unknown provider: " + provider);
  }

  private static String tokenEndpoint(String provider) {
    if ("google".equalsIgnoreCase(provider)) {
      return "https://oauth2.googleapis.com/token";
    }
    if ("microsoft".equalsIgnoreCase(provider)) {
      return "https://login.microsoftonline.com/common/oauth2/v2.0/token";
    }
    throw new OAuthExchangeException("unsupported_provider", "Unknown provider: " + provider);
  }

  private static String userInfoEndpoint(String provider) {
    if ("google".equalsIgnoreCase(provider)) {
      return "https://openidconnect.googleapis.com/v1/userinfo";
    }
    if ("microsoft".equalsIgnoreCase(provider)) {
      return "https://graph.microsoft.com/oidc/userinfo";
    }
    throw new OAuthExchangeException("unsupported_provider", "Unknown provider: " + provider);
  }

  private Map<String, Object> parseIdTokenClaims(String idToken) {
    try {
      String[] parts = idToken.split("\\.");
      if (parts.length < 2) {
        throw new IllegalArgumentException("malformed id_token");
      }
      byte[] payload = Base64Url.decode(parts[1]);
      return toMap(objectMapper.readTree(payload));
    } catch (Exception e) {
      throw new OAuthExchangeException("userinfo_failed", "Failed to parse id_token claims", e);
    }
  }

  private Map<String, Object> toMap(JsonNode node) {
    Map<String, Object> map = new HashMap<>();
    if (node == null || !node.isObject()) {
      return map;
    }
    Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
    while (fields.hasNext()) {
      Map.Entry<String, JsonNode> entry = fields.next();
      JsonNode value = entry.getValue();
      if (value == null || value.isNull()) {
        continue;
      }
      if (value.isTextual()) {
        map.put(entry.getKey(), value.asText());
      } else if (value.isNumber()) {
        map.put(entry.getKey(), value.numberValue());
      } else if (value.isBoolean()) {
        map.put(entry.getKey(), value.asBoolean());
      } else {
        map.put(entry.getKey(), value.toString());
      }
    }
    return map;
  }

  private static String form(String... keyValues) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < keyValues.length; i += 2) {
      if (i > 0) {
        sb.append('&');
      }
      sb.append(enc(keyValues[i])).append('=').append(enc(keyValues[i + 1]));
    }
    return sb.toString();
  }

  private static String enc(String value) {
    return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
  }

  /** Minimal URL-safe Base64 decoder for JWT payload segments. */
  static final class Base64Url {
    static byte[] decode(String input) {
      String normalized = input.replace('-', '+').replace('_', '/');
      int pad = (4 - (normalized.length() % 4)) % 4;
      normalized = normalized + "====".substring(0, pad);
      return java.util.Base64.getDecoder().decode(normalized);
    }
  }

  public static class OAuthExchangeException extends RuntimeException {
    private final String code;

    public OAuthExchangeException(String code, String message) {
      super(message);
      this.code = code;
    }

    public OAuthExchangeException(String code, String message, Throwable cause) {
      super(message, cause);
      this.code = code;
    }

    public String code() {
      return code;
    }
  }
}
