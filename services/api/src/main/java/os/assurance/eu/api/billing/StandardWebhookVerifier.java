package os.assurance.eu.api.billing;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** Standard Webhooks (https://www.standardwebhooks.com) signature verification, as used by Dodo Payments. */
public class StandardWebhookVerifier {
  private final byte[] key;
  private final Duration tolerance;
  private final Clock clock;

  public StandardWebhookVerifier(String secret, Duration tolerance, Clock clock) {
    if (secret == null || secret.isBlank()) {
      throw new IllegalStateException("Webhook secret is not configured");
    }
    String encoded = secret.startsWith("whsec_") ? secret.substring("whsec_".length()) : secret;
    this.key = Base64.getDecoder().decode(encoded);
    this.tolerance = tolerance;
    this.clock = clock;
  }

  public void verify(String webhookId, String webhookTimestamp, String signatureHeader, byte[] rawBody) {
    if (webhookId == null || webhookTimestamp == null || signatureHeader == null) {
      throw new WebhookSignatureException("Missing webhook headers");
    }
    long ts;
    try {
      ts = Long.parseLong(webhookTimestamp.trim());
    } catch (NumberFormatException e) {
      throw new WebhookSignatureException("Bad webhook timestamp");
    }
    long now = clock.instant().getEpochSecond();
    if (Math.abs(now - ts) > tolerance.toSeconds()) {
      throw new WebhookSignatureException("Webhook timestamp outside tolerance");
    }
    byte[] expected = hmac(webhookId + "." + webhookTimestamp.trim() + ".", rawBody);
    for (String part : signatureHeader.trim().split(" ")) {
      int comma = part.indexOf(',');
      if (comma < 0 || !"v1".equals(part.substring(0, comma))) {
        continue;
      }
      byte[] given;
      try {
        given = Base64.getDecoder().decode(part.substring(comma + 1));
      } catch (IllegalArgumentException e) {
        continue;
      }
      if (MessageDigest.isEqual(expected, given)) {
        return;
      }
    }
    throw new WebhookSignatureException("No matching webhook signature");
  }

  private byte[] hmac(String prefix, byte[] body) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(key, "HmacSHA256"));
      mac.update(prefix.getBytes(StandardCharsets.UTF_8));
      return mac.doFinal(body);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }
}
