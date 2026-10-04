package os.assurance.eu.api.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * The per-address email cap counts links issued inside {@code CAP_WINDOW}. If that window were longer than a link's
 * lifetime, an attacker could burn the cap with requests whose links had already expired and lock the owner out
 * of getting a usable one. Both values are constants, so this pins them against each other.
 */
class EmailThrottleInvariantTest {
  @Test
  void capWindowIsNeverLongerThanAResetLinkLifetime() {
    assertThat(AuthTokenService.CAP_WINDOW).isLessThanOrEqualTo(PasswordResetService.RESET_TTL);
    assertThat(AuthTokenService.CAP_WINDOW).isLessThanOrEqualTo(SignupService.VERIFY_TTL);
  }
}
