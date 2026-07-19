package os.assurance.eu.api.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

/**
 * Signed OAuth {@code state} parameter (HMAC-SHA256). Stateless so multi-instance APIs
 * do not need shared state storage for CSRF protection.
 */
@Service
public class OAuthStateService {
  private static final long STATE_TTL_SECONDS = 600;
  private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
  private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

  private final OAuthProperties properties;
  private final Clock clock;
  private final SecureRandom random = new SecureRandom();

  public OAuthStateService(OAuthProperties properties, Clock clock) {
    this.properties = properties;
    this.clock = clock;
  }

  public String issue(String provider) {
    String nonce = randomNonce();
    long exp = Instant.now(clock).getEpochSecond() + STATE_TTL_SECONDS;
    String payload = provider + "|" + nonce + "|" + exp;
    String signature = sign(payload);
    return URL_ENCODER.encodeToString((payload + "|" + signature).getBytes(StandardCharsets.UTF_8));
  }

  public ValidationResult validate(String state, String expectedProvider) {
    if (state == null || state.isBlank()) {
      return ValidationResult.invalid("missing_state");
    }
    try {
      String decoded = new String(URL_DECODER.decode(state), StandardCharsets.UTF_8);
      String[] parts = decoded.split("\\|", 4);
      if (parts.length != 4) {
        return ValidationResult.invalid("malformed_state");
      }
      String provider = parts[0];
      String nonce = parts[1];
      long exp = Long.parseLong(parts[2]);
      String signature = parts[3];
      String payload = provider + "|" + nonce + "|" + exp;
      if (!constantTimeEquals(signature, sign(payload))) {
        return ValidationResult.invalid("bad_signature");
      }
      if (Instant.now(clock).getEpochSecond() > exp) {
        return ValidationResult.invalid("expired");
      }
      if (!provider.equalsIgnoreCase(expectedProvider)) {
        return ValidationResult.invalid("provider_mismatch");
      }
      return ValidationResult.valid(provider);
    } catch (Exception e) {
      return ValidationResult.invalid("malformed_state");
    }
  }

  private String randomNonce() {
    byte[] bytes = new byte[16];
    random.nextBytes(bytes);
    return URL_ENCODER.encodeToString(bytes);
  }

  private String sign(String payload) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(
          properties.getStateSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      return URL_ENCODER.encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) {
      throw new IllegalStateException("Unable to sign OAuth state", e);
    }
  }

  private static boolean constantTimeEquals(String a, String b) {
    return MessageDigest.isEqual(
        a.getBytes(StandardCharsets.UTF_8),
        b.getBytes(StandardCharsets.UTF_8));
  }

  public sealed interface ValidationResult {
    record Valid(String provider) implements ValidationResult {}
    record Invalid(String reason) implements ValidationResult {}

    static ValidationResult valid(String provider) {
      return new Valid(provider);
    }

    static ValidationResult invalid(String reason) {
      return new Invalid(reason);
    }

    default boolean isValid() {
      return this instanceof Valid;
    }
  }
}
