package os.assurance.eu.api.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class KeyEncryption {
  private static final String PREFIX = "enc:v1:";
  private static final SecureRandom RANDOM = new SecureRandom();

  private KeyEncryption() {
  }

  public static boolean isEncrypted(String stored) {
    return stored != null && stored.startsWith(PREFIX);
  }

  public static String encrypt(String plaintext, String secret) {
    try {
      byte[] iv = new byte[12];
      RANDOM.nextBytes(iv);
      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(Cipher.ENCRYPT_MODE, key(secret), new GCMParameterSpec(128, iv));
      byte[] ct = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
      Base64.Encoder enc = Base64.getUrlEncoder().withoutPadding();
      return PREFIX + enc.encodeToString(iv) + ":" + enc.encodeToString(ct);
    } catch (Exception e) {
      throw new IllegalStateException("Key encryption failed", e);
    }
  }

  public static String decrypt(String stored, String secret) {
    if (!isEncrypted(stored)) {
      return stored;
    }
    try {
      String[] parts = stored.substring(PREFIX.length()).split(":", 2);
      Base64.Decoder dec = Base64.getUrlDecoder();
      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(Cipher.DECRYPT_MODE, key(secret), new GCMParameterSpec(128, dec.decode(parts[0])));
      return new String(cipher.doFinal(dec.decode(parts[1])), StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new IllegalStateException("Key decryption failed — wrong JWT_KEY_ENCRYPTION_SECRET?", e);
    }
  }

  private static SecretKeySpec key(String secret) throws Exception {
    if (secret == null || secret.isBlank()) {
      throw new IllegalStateException("Key encryption secret is not configured");
    }
    byte[] digest = MessageDigest.getInstance("SHA-256").digest(secret.getBytes(StandardCharsets.UTF_8));
    return new SecretKeySpec(digest, "AES");
  }
}
