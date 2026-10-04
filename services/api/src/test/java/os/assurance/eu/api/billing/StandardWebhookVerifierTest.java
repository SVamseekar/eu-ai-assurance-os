package os.assurance.eu.api.billing;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;

class StandardWebhookVerifierTest {
  private static final byte[] KEY = "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8);
  static final String SECRET = "whsec_MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="; // base64 of KEY
  private static final Instant NOW = Instant.parse("2026-11-10T12:00:00Z");
  private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
  private final StandardWebhookVerifier verifier = new StandardWebhookVerifier(SECRET, Duration.ofMinutes(5), clock);

  static String sign(String id, String ts, byte[] body) throws Exception {
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(KEY, "HmacSHA256"));
    mac.update((id + "." + ts + ".").getBytes(StandardCharsets.UTF_8));
    return "v1," + Base64.getEncoder().encodeToString(mac.doFinal(body));
  }

  @Test
  void acceptsValidSignatureAmongMultiple() throws Exception {
    byte[] body = "{\"type\":\"subscription.active\"}".getBytes(StandardCharsets.UTF_8);
    String ts = String.valueOf(NOW.getEpochSecond());
    String header = "v1,bm90LXZhbGlk " + sign("msg_1", ts, body);
    assertThatCode(() -> verifier.verify("msg_1", ts, header, body)).doesNotThrowAnyException();
  }

  @Test
  void rejectsTamperedBody() throws Exception {
    byte[] body = "{\"a\":1}".getBytes(StandardCharsets.UTF_8);
    String ts = String.valueOf(NOW.getEpochSecond());
    String header = sign("msg_1", ts, body);
    assertThatThrownBy(() -> verifier.verify("msg_1", ts, header, "{\"a\":2}".getBytes(StandardCharsets.UTF_8)))
        .isInstanceOf(WebhookSignatureException.class);
  }

  @Test
  void rejectsOldTimestamp() throws Exception {
    byte[] body = "{}".getBytes(StandardCharsets.UTF_8);
    String ts = String.valueOf(NOW.minusSeconds(600).getEpochSecond());
    assertThatThrownBy(() -> verifier.verify("msg_1", ts, sign("msg_1", ts, body), body))
        .isInstanceOf(WebhookSignatureException.class);
  }

  @Test
  void rejectsFutureTimestampAndMissingHeaders() throws Exception {
    byte[] body = "{}".getBytes(StandardCharsets.UTF_8);
    String future = String.valueOf(NOW.plusSeconds(600).getEpochSecond());
    assertThatThrownBy(() -> verifier.verify("msg_1", future, sign("msg_1", future, body), body))
        .isInstanceOf(WebhookSignatureException.class);
    assertThatThrownBy(() -> verifier.verify(null, null, null, body)).isInstanceOf(WebhookSignatureException.class);
  }

  @Test
  void rejectsSignatureMadeForAnotherMessageId() throws Exception {
    byte[] body = "{}".getBytes(StandardCharsets.UTF_8);
    String ts = String.valueOf(NOW.getEpochSecond());
    assertThatThrownBy(() -> verifier.verify("msg_2", ts, sign("msg_1", ts, body), body))
        .isInstanceOf(WebhookSignatureException.class);
  }
}
