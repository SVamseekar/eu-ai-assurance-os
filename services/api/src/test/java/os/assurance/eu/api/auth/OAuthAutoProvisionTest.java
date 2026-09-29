package os.assurance.eu.api.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import os.assurance.eu.api.tenant.UserJpaRepository;

@SpringBootTest(properties = {
    "assurance.eval.worker.enabled=false",
    "assurance.eval.callback.secret=test-eval-callback-secret",
    "assurance.oauth.auto-provision=true",
    "assurance.oauth.redirect-base-url=http://localhost:3000",
    "assurance.oauth.state-secret=test-oauth-state-secret",
    "assurance.oauth.google.client-id=test-google-client",
    "assurance.oauth.google.client-secret=test-google-secret"
})
class OAuthAutoProvisionTest {
  private static final String NONCE = "test-browser-nonce-0123456789";

  @Autowired
  private OAuthService oauthService;

  @Autowired
  private OAuthStateService stateService;

  @Autowired
  private UserJpaRepository users;

  @Autowired
  private JwtService jwtService;

  @MockitoBean
  private OAuthTokenClient tokenClient;

  @BeforeEach
  void stubs() {
    when(tokenClient.callbackRedirectUri(anyString()))
        .thenReturn("http://localhost:3000/api/auth/oauth/google/callback");
    when(tokenClient.exchangeCode(eq("google"), eq("auto-code"), anyString()))
        .thenReturn(Map.of("access_token", "at"));
    when(tokenClient.fetchUserInfo(eq("google"), any()))
        .thenReturn(Map.of(
            "sub", "google-auto-subject",
            "email", "auto-provisioned@newcorp.example",
            "name", "Auto User",
            "email_verified", true));
  }

  @Test
  void autoProvisionCreatesTenantAdminAndIssuesTokens() {
    String state = stateService.issue("google", NONCE);
    TokenResponse tokens = oauthService.completeAuthorization("google", "auto-code", state, NONCE);

    assertThat(tokens.accessToken()).isNotBlank();
    var claims = jwtService.verifyAccessToken(tokens.accessToken()).orElseThrow();
    var user = users.findById(claims.userId()).orElseThrow();
    assertThat(user.email()).isEqualTo("auto-provisioned@newcorp.example");
    assertThat(user.oauthProvider()).isEqualTo("google");
    assertThat(user.oauthSubject()).isEqualTo("google-auto-subject");
    assertThat(user.passwordHash()).isNull();
  }

  @Test
  void autoProvisionRefusesUnverifiedEmail() {
    when(tokenClient.exchangeCode(eq("google"), eq("unverified-code"), anyString()))
        .thenReturn(Map.of("access_token", "at"));
    when(tokenClient.fetchUserInfo(eq("google"), any()))
        .thenReturn(Map.of("sub", "google-unverified", "email", "someone@unverified.example"));
    String state = stateService.issue("google", NONCE);
    assertThatThrownBy(() -> oauthService.completeAuthorization("google", "unverified-code", state, NONCE))
        .isInstanceOf(OAuthService.OAuthLoginException.class)
        .extracting(ex -> ((OAuthService.OAuthLoginException) ex).code())
        .isEqualTo("email_unverified");
  }
}
