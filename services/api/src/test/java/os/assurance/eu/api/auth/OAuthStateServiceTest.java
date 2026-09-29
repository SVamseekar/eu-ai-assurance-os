package os.assurance.eu.api.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OAuthStateServiceTest {
  private static final String NONCE = "browser-nonce-0123456789abcdef";

  private OAuthProperties props;
  private Clock clock;
  private OAuthStateService service;

  @BeforeEach
  void setUp() {
    props = new OAuthProperties();
    props.setStateSecret("test-state-secret");
    clock = Clock.fixed(Instant.parse("2026-07-20T12:00:00Z"), ZoneOffset.UTC);
    service = new OAuthStateService(props, clock);
  }

  @Test
  void issuedStateValidatesForMatchingProvider() {
    String state = service.issue("google", NONCE);
    OAuthStateService.ValidationResult result = service.validate(state, "google", NONCE);

    assertThat(result.isValid()).isTrue();
  }

  @Test
  void providerMismatchIsRejected() {
    String state = service.issue("google", NONCE);
    OAuthStateService.ValidationResult result = service.validate(state, "microsoft", NONCE);

    assertThat(result.isValid()).isFalse();
    assertThat(((OAuthStateService.ValidationResult.Invalid) result).reason())
        .isEqualTo("provider_mismatch");
  }

  @Test
  void tamperedStateIsRejected() {
    String state = service.issue("google", NONCE) + "x";
    OAuthStateService.ValidationResult result = service.validate(state, "google", NONCE);

    assertThat(result.isValid()).isFalse();
  }

  @Test
  void expiredStateIsRejected() {
    String state = service.issue("google", NONCE);

    Clock later = Clock.fixed(Instant.parse("2026-07-20T12:20:00Z"), ZoneOffset.UTC);
    OAuthStateService validator = new OAuthStateService(props, later);
    OAuthStateService.ValidationResult result = validator.validate(state, "google", NONCE);

    assertThat(result.isValid()).isFalse();
    assertThat(((OAuthStateService.ValidationResult.Invalid) result).reason()).isEqualTo("expired");
  }

  @Test
  void stateIssuedForOneBrowserFailsForAnother() {
    String state = service.issue("google", NONCE);
    assertThat(service.validate(state, "google", "different-browser-nonce-000000").isValid()).isFalse();
  }

  @Test
  void stateWithoutBrowserNonceIsRejected() {
    String state = service.issue("google", NONCE);
    assertThat(service.validate(state, "google", null).isValid()).isFalse();
  }

  @Test
  void issueRejectsShortOrUnsafeNonce() {
    assertThatThrownBy(() -> service.issue("google", "short"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> service.issue("google", "has|pipe|chars-000000000"))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
