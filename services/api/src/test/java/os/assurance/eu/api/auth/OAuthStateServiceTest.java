package os.assurance.eu.api.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class OAuthStateServiceTest {

  @Test
  void issuedStateValidatesForMatchingProvider() {
    OAuthProperties props = new OAuthProperties();
    props.setStateSecret("test-state-secret");
    Clock clock = Clock.fixed(Instant.parse("2026-07-20T12:00:00Z"), ZoneOffset.UTC);
    OAuthStateService service = new OAuthStateService(props, clock);

    String state = service.issue("google");
    OAuthStateService.ValidationResult result = service.validate(state, "google");

    assertThat(result.isValid()).isTrue();
  }

  @Test
  void providerMismatchIsRejected() {
    OAuthProperties props = new OAuthProperties();
    props.setStateSecret("test-state-secret");
    Clock clock = Clock.fixed(Instant.parse("2026-07-20T12:00:00Z"), ZoneOffset.UTC);
    OAuthStateService service = new OAuthStateService(props, clock);

    String state = service.issue("google");
    OAuthStateService.ValidationResult result = service.validate(state, "microsoft");

    assertThat(result.isValid()).isFalse();
    assertThat(((OAuthStateService.ValidationResult.Invalid) result).reason())
        .isEqualTo("provider_mismatch");
  }

  @Test
  void tamperedStateIsRejected() {
    OAuthProperties props = new OAuthProperties();
    props.setStateSecret("test-state-secret");
    Clock clock = Clock.fixed(Instant.parse("2026-07-20T12:00:00Z"), ZoneOffset.UTC);
    OAuthStateService service = new OAuthStateService(props, clock);

    String state = service.issue("google") + "x";
    OAuthStateService.ValidationResult result = service.validate(state, "google");

    assertThat(result.isValid()).isFalse();
  }

  @Test
  void expiredStateIsRejected() {
    OAuthProperties props = new OAuthProperties();
    props.setStateSecret("test-state-secret");
    Clock issueClock = Clock.fixed(Instant.parse("2026-07-20T12:00:00Z"), ZoneOffset.UTC);
    OAuthStateService issuer = new OAuthStateService(props, issueClock);
    String state = issuer.issue("google");

    Clock later = Clock.fixed(Instant.parse("2026-07-20T12:20:00Z"), ZoneOffset.UTC);
    OAuthStateService validator = new OAuthStateService(props, later);
    OAuthStateService.ValidationResult result = validator.validate(state, "google");

    assertThat(result.isValid()).isFalse();
    assertThat(((OAuthStateService.ValidationResult.Invalid) result).reason()).isEqualTo("expired");
  }
}
