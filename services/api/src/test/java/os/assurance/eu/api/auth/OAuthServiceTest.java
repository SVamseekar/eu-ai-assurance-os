package os.assurance.eu.api.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import os.assurance.eu.api.tenant.TenantEntity;
import os.assurance.eu.api.tenant.TenantJpaRepository;
import os.assurance.eu.api.tenant.UserEntity;
import os.assurance.eu.api.tenant.UserJpaRepository;
import os.assurance.eu.api.tenant.UserRole;

@SpringBootTest(properties = {
    "assurance.eval.worker.enabled=false",
    "assurance.eval.callback.secret=test-eval-callback-secret",
    "assurance.oauth.auto-provision=false",
    "assurance.oauth.redirect-base-url=http://localhost:3000",
    "assurance.oauth.state-secret=test-oauth-state-secret",
    "assurance.oauth.google.client-id=test-google-client",
    "assurance.oauth.google.client-secret=test-google-secret",
    "assurance.oauth.microsoft.client-id=test-ms-client",
    "assurance.oauth.microsoft.client-secret=test-ms-secret"
})
class OAuthServiceTest {
  private static final String NONCE = "test-browser-nonce-0123456789";

  @Autowired
  private OAuthService oauthService;

  @Autowired
  private OAuthStateService stateService;

  @Autowired
  private UserJpaRepository users;

  @Autowired
  private TenantJpaRepository tenants;

  @Autowired
  private JwtService jwtService;

  @Autowired
  private RefreshTokenService refreshTokenService;

  @Autowired
  private AuthTokenService authTokens;

  @MockitoBean
  private OAuthTokenClient tokenClient;

  private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

  @BeforeEach
  void stubTokenClientBasics() {
    when(tokenClient.callbackRedirectUri(anyString()))
        .thenAnswer(inv -> "http://localhost:3000/api/auth/oauth/" + inv.getArgument(0) + "/callback");
    when(tokenClient.buildAuthorizationUrl(anyString(), anyString(), anyString()))
        .thenAnswer(inv -> "https://accounts.google.com/o/oauth2/v2/auth?state=" + inv.getArgument(1)
            + "&client_id=test");
  }

  @Test
  void happyPathLinksExistingUserByEmailAndIssuesTokens() {
    UUID tenantId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    tenants.save(new TenantEntity(tenantId, "OAuth Happy", "starter", "EU", Instant.now()));
    users.save(new UserEntity(
        userId, tenantId, "oauth-happy@example.com", UserRole.ADMIN,
        encoder.encode("dev-local-password-only"), Instant.now()));

    when(tokenClient.exchangeCode(eq("google"), eq("good-code"), anyString()))
        .thenReturn(Map.of("access_token", "at", "id_token", "ignored"));
    when(tokenClient.fetchUserInfo(eq("google"), any()))
        .thenReturn(Map.of(
            "sub", "google-subject-happy",
            "email", "oauth-happy@example.com",
            "name", "OAuth Happy",
            "email_verified", true));

    String state = stateService.issue("google", NONCE);
    TokenResponse tokens = oauthService.completeAuthorization("google", "good-code", state, NONCE);

    assertThat(tokens.accessToken()).isNotBlank();
    assertThat(tokens.refreshToken()).isNotBlank();
    assertThat(jwtService.verifyAccessToken(tokens.accessToken())).isPresent();

    UserEntity linked = users.findById(userId).orElseThrow();
    assertThat(linked.oauthProvider()).isEqualTo("google");
    assertThat(linked.oauthSubject()).isEqualTo("google-subject-happy");
  }

  @Test
  void happyPathFindsUserByExistingOauthBinding() {
    UUID tenantId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    tenants.save(new TenantEntity(tenantId, "OAuth Bound", "starter", "EU", Instant.now()));
    users.save(new UserEntity(
        userId, tenantId, "oauth-bound@example.com", UserRole.COMPLIANCE_OFFICER,
        null, "google", "google-subject-bound", Instant.now()));

    when(tokenClient.exchangeCode(eq("google"), eq("bound-code"), anyString()))
        .thenReturn(Map.of("access_token", "at"));
    when(tokenClient.fetchUserInfo(eq("google"), any()))
        .thenReturn(Map.of(
            "sub", "google-subject-bound",
            "email", "oauth-bound@example.com",
            "name", "Bound User"));

    String state = stateService.issue("google", NONCE);
    TokenResponse tokens = oauthService.completeAuthorization("google", "bound-code", state, NONCE);

    assertThat(tokens.accessToken()).isNotBlank();
    var claims = jwtService.verifyAccessToken(tokens.accessToken()).orElseThrow();
    assertThat(claims.userId()).isEqualTo(userId);
    assertThat(claims.tenantId()).isEqualTo(tenantId);
  }

  @Test
  void notProvisionedWhenAutoProvisionDisabledAndUserUnknown() {
    when(tokenClient.exchangeCode(eq("google"), eq("new-code"), anyString()))
        .thenReturn(Map.of("access_token", "at"));
    when(tokenClient.fetchUserInfo(eq("google"), any()))
        .thenReturn(Map.of(
            "sub", "google-unknown-subject",
            "email", "brand-new-oauth@example.com",
            "name", "New User",
            "email_verified", true));

    String state = stateService.issue("google", NONCE);

    assertThatThrownBy(() -> oauthService.completeAuthorization("google", "new-code", state, NONCE))
        .isInstanceOf(OAuthService.OAuthLoginException.class)
        .extracting(ex -> ((OAuthService.OAuthLoginException) ex).code())
        .isEqualTo("not_provisioned");
  }

  @Test
  void badStateIsRejectedWithoutCallingProvider() {
    assertThatThrownBy(() -> oauthService.completeAuthorization("google", "any-code", "not-a-valid-state", NONCE))
        .isInstanceOf(OAuthService.OAuthLoginException.class)
        .extracting(ex -> ((OAuthService.OAuthLoginException) ex).code())
        .isEqualTo("state");
  }

  @Test
  void beginAuthorizationReturnsProviderUrl() {
    String url = oauthService.beginAuthorization("google", NONCE);
    assertThat(url).contains("accounts.google.com");
  }

  @Test
  void unverifiedEmailNeverLinksToExistingAccount() {
    UUID tenantId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    tenants.save(new TenantEntity(tenantId, "Victim Co", "starter", "EU", Instant.now()));
    users.save(new UserEntity(userId, tenantId, "victim@victim.example", UserRole.ADMIN,
        encoder.encode("victim-password"), Instant.now()));

    when(tokenClient.exchangeCode(eq("microsoft"), eq("attacker-code"), anyString()))
        .thenReturn(Map.of("access_token", "at"));
    when(tokenClient.fetchUserInfo(eq("microsoft"), any()))
        .thenReturn(Map.of("sub", "attacker-subject", "preferred_username", "victim@victim.example"));

    String state = stateService.issue("microsoft", NONCE);
    assertThatThrownBy(() -> oauthService.completeAuthorization("microsoft", "attacker-code", state, NONCE))
        .isInstanceOf(OAuthService.OAuthLoginException.class)
        .extracting(ex -> ((OAuthService.OAuthLoginException) ex).code())
        .isEqualTo("email_unverified");

    UserEntity victim = users.findById(userId).orElseThrow();
    assertThat(victim.oauthProvider()).isNull();
    assertThat(victim.oauthSubject()).isNull();
  }

  @Test
  void verifiedLinkingMatchesEmailCaseInsensitively() {
    UUID tenantId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    tenants.save(new TenantEntity(tenantId, "Case Co", "starter", "EU", Instant.now()));
    users.save(new UserEntity(userId, tenantId, "Mixed.Case@Case.Example", UserRole.ADMIN,
        encoder.encode("pw"), Instant.now()));

    when(tokenClient.exchangeCode(eq("google"), eq("case-code"), anyString()))
        .thenReturn(Map.of("access_token", "at"));
    when(tokenClient.fetchUserInfo(eq("google"), any()))
        .thenReturn(Map.of("sub", "case-subject", "email", "mixed.case@case.example", "email_verified", true));

    String state = stateService.issue("google", NONCE);
    oauthService.completeAuthorization("google", "case-code", state, NONCE);
    assertThat(users.findById(userId).orElseThrow().oauthSubject()).isEqualTo("case-subject");
  }

  @Test
  void linkingAnUnverifiedPasswordAccountWipesThePasswordAndItsSessions() {
    UUID tenantId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    tenants.save(new TenantEntity(tenantId, "Pre-hijack Co", "trial", "EU", Instant.now()));
    users.save(new UserEntity(userId, tenantId, "prehijack@victim.example", UserRole.ADMIN,
        encoder.encode("attacker-chosen-password"), Instant.now()));
    String attackerRefresh = refreshTokenService.issue(userId, tenantId).rawToken();
    String pendingLink = authTokens.issue(userId, AuthTokenPurpose.VERIFY_EMAIL, java.time.Duration.ofHours(1));

    when(tokenClient.exchangeCode(eq("google"), eq("victim-code"), anyString()))
        .thenReturn(Map.of("access_token", "at"));
    when(tokenClient.fetchUserInfo(eq("google"), any()))
        .thenReturn(Map.of("sub", "victim-subject", "email", "prehijack@victim.example", "email_verified", true));

    String state = stateService.issue("google", NONCE);
    oauthService.completeAuthorization("google", "victim-code", state, NONCE);

    UserEntity linked = users.findById(userId).orElseThrow();
    assertThat(linked.passwordHash()).isNull();
    assertThat(linked.emailVerifiedAt()).isNotNull();
    assertThat(refreshTokenService.rotate(attackerRefresh))
        .isInstanceOf(RefreshTokenService.RefreshResult.Rejected.class);
    assertThatThrownBy(() -> authTokens.consume(pendingLink, AuthTokenPurpose.VERIFY_EMAIL))
        .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
  }

  @Test
  void linkingAVerifiedAccountKeepsItsPassword() {
    UUID tenantId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    tenants.save(new TenantEntity(tenantId, "Verified Co", "trial", "EU", Instant.now()));
    UserEntity verified = new UserEntity(userId, tenantId, "verified@owner.example", UserRole.ADMIN,
        encoder.encode("owner-password"), Instant.now());
    verified.markEmailVerified(Instant.now());
    users.save(verified);

    when(tokenClient.exchangeCode(eq("google"), eq("owner-code"), anyString()))
        .thenReturn(Map.of("access_token", "at"));
    when(tokenClient.fetchUserInfo(eq("google"), any()))
        .thenReturn(Map.of("sub", "owner-subject", "email", "verified@owner.example", "email_verified", true));

    String state = stateService.issue("google", NONCE);
    oauthService.completeAuthorization("google", "owner-code", state, NONCE);

    assertThat(users.findById(userId).orElseThrow().passwordHash()).isNotNull();
  }
}
