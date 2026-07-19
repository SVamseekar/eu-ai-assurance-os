package os.assurance.eu.api.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * HMAC-SHA-256 hash chain for append-only audit events.
 *
 * <p>Canonical input:
 * {@code tenantId|id|prevHash|actorId|eventType|resourceType|resourceId|payloadJson|createdAt}
 */
@Component
public class AuditChainHasher {
  private static final ObjectMapper MAPPER = new ObjectMapper()
      .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

  private final byte[] secret;

  public AuditChainHasher(
      @Value("${assurance.audit.chain-secret:local-dev-audit-chain-secret}") String secret) {
    this.secret = (secret == null || secret.isBlank()
        ? "local-dev-audit-chain-secret"
        : secret).getBytes(StandardCharsets.UTF_8);
  }

  public String hash(
      UUID tenantId,
      UUID id,
      String prevHash,
      UUID actorId,
      String eventType,
      String resourceType,
      String resourceId,
      Map<String, Object> payload,
      Instant createdAt) {
    String canonical = String.join("|",
        nullToEmpty(tenantId),
        nullToEmpty(id),
        prevHash == null ? "" : prevHash,
        nullToEmpty(actorId),
        nullToEmpty(eventType),
        nullToEmpty(resourceType),
        resourceId == null ? "" : resourceId,
        canonicalJson(payload),
        createdAt == null ? "" : createdAt.toString());
    return hmacHex(canonical);
  }

  public boolean matches(
      UUID tenantId,
      UUID id,
      String prevHash,
      UUID actorId,
      String eventType,
      String resourceType,
      String resourceId,
      Map<String, Object> payload,
      Instant createdAt,
      String expectedHash) {
    if (expectedHash == null || expectedHash.isBlank()) {
      return false;
    }
    return expectedHash.equals(hash(
        tenantId, id, prevHash, actorId, eventType, resourceType, resourceId, payload, createdAt));
  }

  private String hmacHex(String canonical) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret, "HmacSHA256"));
      return HexFormat.of().formatHex(mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) {
      throw new IllegalStateException("Unable to compute audit chain hash", e);
    }
  }

  private String canonicalJson(Map<String, Object> payload) {
    try {
      return MAPPER.writeValueAsString(payload == null ? Map.of() : payload);
    } catch (Exception e) {
      throw new IllegalArgumentException("Unable to serialize audit payload", e);
    }
  }

  private static String nullToEmpty(Object value) {
    return value == null ? "" : value.toString();
  }
}
