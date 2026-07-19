package os.assurance.eu.api.regmonitor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class RegContentHasher {
  private RegContentHasher() {
  }

  public static String hash(ParsedFeedItem item) {
    String material = String.join("|",
        nullToEmpty(item.externalId()),
        nullToEmpty(item.title()),
        nullToEmpty(item.summary()),
        nullToEmpty(item.url()),
        item.publishedAt() == null ? "" : item.publishedAt().toString());
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(material.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }

  private static String nullToEmpty(String value) {
    return value == null ? "" : value.strip();
  }
}
