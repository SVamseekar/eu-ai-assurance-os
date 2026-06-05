package os.assurance.eu.api.eval;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class EvalCallbackSignatureVerifier {
  public static final String SIGNATURE_HEADER = "X-Eval-Signature";
  public static final String TIMESTAMP_HEADER = "X-Eval-Timestamp";

  private final String secret;
  private final long toleranceSeconds;
  private final Clock clock;
  private final EvalRunMetrics metrics;

  @Autowired
  public EvalCallbackSignatureVerifier(
      @Value("${assurance.eval.callback.secret:}") String secret,
      @Value("${assurance.eval.callback.signature-tolerance-seconds:300}") long toleranceSeconds,
      EvalRunMetrics metrics) {
    this(secret, toleranceSeconds, Clock.systemUTC(), metrics);
  }

  EvalCallbackSignatureVerifier(String secret, long toleranceSeconds, Clock clock, EvalRunMetrics metrics) {
    this.secret = secret;
    this.toleranceSeconds = toleranceSeconds;
    this.clock = clock;
    this.metrics = metrics;
  }

  @PostConstruct
  void validateConfiguration() {
    if (secret == null || secret.isBlank()) {
      throw new IllegalStateException("assurance.eval.callback.secret must be configured");
    }
  }

  public void verify(String rawBody, String timestampHeader, String signatureHeader) {
    long timestamp = parseTimestamp(timestampHeader);
    long now = clock.instant().getEpochSecond();
    if (Math.abs(now - timestamp) > toleranceSeconds) {
      metrics.callbackSignatureRejected("timestamp_outside_tolerance");
      throw new ResponseStatusException(
          HttpStatus.UNAUTHORIZED,
          "Eval callback signature timestamp is outside tolerance");
    }
    String expected = "v1=" + hmacHex(timestamp + "." + rawBody);
    if (signatureHeader == null || !constantTimeEquals(expected, signatureHeader.trim())) {
      metrics.callbackSignatureRejected("invalid_signature");
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid eval callback signature");
    }
  }

  private long parseTimestamp(String timestampHeader) {
    if (timestampHeader == null || timestampHeader.isBlank()) {
      metrics.callbackSignatureRejected("missing_timestamp");
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing eval callback signature timestamp");
    }
    try {
      return Long.parseLong(timestampHeader);
    } catch (NumberFormatException exception) {
      metrics.callbackSignatureRejected("invalid_timestamp");
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid eval callback signature timestamp");
    }
  }

  private String hmacHex(String signedPayload) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      byte[] digest = mac.doFinal(signedPayload.getBytes(StandardCharsets.UTF_8));
      StringBuilder hex = new StringBuilder(digest.length * 2);
      for (byte b : digest) {
        hex.append(String.format("%02x", b));
      }
      return hex.toString();
    } catch (Exception exception) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not verify eval callback signature");
    }
  }

  private boolean constantTimeEquals(String expected, String provided) {
    byte[] expectedBytes = expected.getBytes(StandardCharsets.UTF_8);
    byte[] providedBytes = provided.getBytes(StandardCharsets.UTF_8);
    if (expectedBytes.length != providedBytes.length) {
      return false;
    }
    int mismatch = 0;
    for (int i = 0; i < expectedBytes.length; i++) {
      mismatch |= expectedBytes[i] ^ providedBytes[i];
    }
    return mismatch == 0;
  }
}
