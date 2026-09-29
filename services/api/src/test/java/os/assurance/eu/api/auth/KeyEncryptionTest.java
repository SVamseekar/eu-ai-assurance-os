package os.assurance.eu.api.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class KeyEncryptionTest {
  private static final String SECRET = "k".repeat(40);

  @Test
  void roundTripsAndMarksCiphertext() {
    String stored = KeyEncryption.encrypt("private-key-material", SECRET);
    assertThat(stored).startsWith("enc:v1:");
    assertThat(KeyEncryption.isEncrypted(stored)).isTrue();
    assertThat(KeyEncryption.decrypt(stored, SECRET)).isEqualTo("private-key-material");
  }

  @Test
  void legacyPlaintextPassesThrough() {
    assertThat(KeyEncryption.isEncrypted("MIIEvQIBADANBgkq")).isFalse();
    assertThat(KeyEncryption.decrypt("MIIEvQIBADANBgkq", SECRET)).isEqualTo("MIIEvQIBADANBgkq");
  }

  @Test
  void wrongSecretFails() {
    String stored = KeyEncryption.encrypt("x", SECRET);
    assertThatThrownBy(() -> KeyEncryption.decrypt(stored, "z".repeat(40)))
        .isInstanceOf(IllegalStateException.class);
  }
}
