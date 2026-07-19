package os.assurance.eu.api.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import org.junit.jupiter.api.Test;

class OAuthProviderProfileTest {

  @Test
  void parsesGoogleProfile() {
    OAuthProviderProfile profile = OAuthProviderProfile.fromUserInfo(
        "google",
        Map.of("sub", "google-123", "email", "a@example.com", "name", "Ada Lovelace"));

    assertThat(profile.provider()).isEqualTo("google");
    assertThat(profile.subject()).isEqualTo("google-123");
    assertThat(profile.email()).isEqualTo("a@example.com");
    assertThat(profile.displayName()).isEqualTo("Ada Lovelace");
  }

  @Test
  void microsoftFallsBackToPreferredUsername() {
    OAuthProviderProfile profile = OAuthProviderProfile.fromUserInfo(
        "microsoft",
        Map.of("sub", "ms-789", "preferred_username", "c@contoso.com", "name", "Contoso User"));

    assertThat(profile.email()).isEqualTo("c@contoso.com");
    assertThat(profile.displayName()).isEqualTo("Contoso User");
  }

  @Test
  void microsoftFallsBackToUpn() {
    OAuthProviderProfile profile = OAuthProviderProfile.fromUserInfo(
        "microsoft",
        Map.of("sub", "ms-101", "upn", "d@fabrikam.com"));

    assertThat(profile.email()).isEqualTo("d@fabrikam.com");
    assertThat(profile.displayName()).isEqualTo("d@fabrikam.com");
  }

  @Test
  void missingEmailRaises() {
    assertThatThrownBy(() -> OAuthProviderProfile.fromUserInfo("google", Map.of("sub", "x")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("email");
  }
}
