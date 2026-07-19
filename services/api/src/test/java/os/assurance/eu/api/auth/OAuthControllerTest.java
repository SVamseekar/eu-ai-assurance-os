package os.assurance.eu.api.auth;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.DefaultResponseErrorHandler;
import os.assurance.eu.api.tenant.TenantEntity;
import os.assurance.eu.api.tenant.TenantJpaRepository;
import os.assurance.eu.api.tenant.UserEntity;
import os.assurance.eu.api.tenant.UserJpaRepository;
import os.assurance.eu.api.tenant.UserRole;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "assurance.eval.worker.enabled=false",
        "assurance.eval.callback.secret=test-eval-callback-secret",
        "assurance.oauth.auto-provision=false",
        "assurance.oauth.redirect-base-url=http://localhost:3000",
        "assurance.oauth.state-secret=test-oauth-state-secret",
        "assurance.oauth.google.client-id=test-google-client",
        "assurance.oauth.google.client-secret=test-google-secret"
    })
class OAuthControllerTest {

  @Autowired
  private TestRestTemplate rest;

  @Autowired
  private OAuthStateService stateService;

  @Autowired
  private TenantJpaRepository tenants;

  @Autowired
  private UserJpaRepository users;

  @MockBean
  private OAuthTokenClient tokenClient;

  private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

  @BeforeEach
  void configureRestAndStubs() {
    var restTemplate = rest.getRestTemplate();
    restTemplate.setRequestFactory(new JdkClientHttpRequestFactory());
    restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
      @Override
      public boolean hasError(org.springframework.http.client.ClientHttpResponse response) {
        return false;
      }
    });

    when(tokenClient.callbackRedirectUri(anyString()))
        .thenAnswer(inv -> "http://localhost:3000/api/auth/oauth/" + inv.getArgument(0) + "/callback");
    when(tokenClient.buildAuthorizationUrl(anyString(), anyString(), anyString()))
        .thenReturn("https://accounts.google.com/o/oauth2/v2/auth?client_id=test");
  }

  @Test
  void startRedirectsToProvider() {
    var response = rest.exchange(
        "/auth/oauth/google/start",
        HttpMethod.GET,
        HttpEntity.EMPTY,
        Void.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
    assertThat(response.getHeaders().getLocation()).isNotNull();
    assertThat(response.getHeaders().getLocation().toString()).contains("accounts.google.com");
  }

  @Test
  void callbackHappyPathReturnsTokensWithoutAuthHeaders() {
    UUID tenantId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    tenants.save(new TenantEntity(tenantId, "Ctrl Happy", "starter", "EU", Instant.now()));
    users.save(new UserEntity(
        userId, tenantId, "ctrl-happy@example.com", UserRole.ADMIN,
        encoder.encode("x"), Instant.now()));

    when(tokenClient.exchangeCode(eq("google"), eq("ctrl-code"), anyString()))
        .thenReturn(Map.of("access_token", "at"));
    when(tokenClient.fetchUserInfo(eq("google"), any()))
        .thenReturn(Map.of(
            "sub", "google-ctrl-happy",
            "email", "ctrl-happy@example.com",
            "name", "Ctrl Happy"));

    String state = stateService.issue("google");
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    var response = rest.exchange(
        "/auth/oauth/google/callback",
        HttpMethod.POST,
        new HttpEntity<>(new OAuthController.OAuthCallbackRequest("ctrl-code", state), headers),
        TokenResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().accessToken()).isNotBlank();
    assertThat(response.getBody().refreshToken()).isNotBlank();
  }

  @Test
  void callbackNotProvisionedReturnsForbidden() {
    when(tokenClient.exchangeCode(eq("google"), eq("np-code"), anyString()))
        .thenReturn(Map.of("access_token", "at"));
    when(tokenClient.fetchUserInfo(eq("google"), any()))
        .thenReturn(Map.of(
            "sub", "google-not-provisioned",
            "email", "never-seen@example.com",
            "name", "Nobody"));

    String state = stateService.issue("google");
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    var response = rest.exchange(
        "/auth/oauth/google/callback",
        HttpMethod.POST,
        new HttpEntity<>(new OAuthController.OAuthCallbackRequest("np-code", state), headers),
        String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void callbackBadStateReturnsBadRequest() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    var response = rest.exchange(
        "/auth/oauth/google/callback",
        HttpMethod.POST,
        new HttpEntity<>(new OAuthController.OAuthCallbackRequest("code", "garbage-state"), headers),
        String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void oauthStartIsReachableWithoutCredentials() {
    var response = rest.exchange(
        "/auth/oauth/google/start",
        HttpMethod.GET,
        HttpEntity.EMPTY,
        Void.class);
    // Not 401 from TenantContextFilter
    assertThat(response.getStatusCode().value()).isNotEqualTo(401);
  }
}
