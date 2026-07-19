package os.assurance.eu.api.tenant;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.DefaultResponseErrorHandler;
import os.assurance.eu.api.auth.JwtService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TenantContextFilterTest {

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private TenantJpaRepository tenants;

    @Autowired
    private UserJpaRepository users;

    @Autowired
    private JwtService jwtService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @BeforeEach
    void configureRestTemplate() {
        var restTemplate = rest.getRestTemplate();
        restTemplate.setRequestFactory(new JdkClientHttpRequestFactory());
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(org.springframework.http.client.ClientHttpResponse response) {
                return false;
            }
        });
    }

    @Test
    void requestWithNoCredentialsIsRejected() {
        var response = rest.getForEntity("/api/v1/systems", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void requestWithOnlyLegacyTenantAndActorHeadersIsRejected() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-Id", TenantContext.DEFAULT_TENANT_ID.toString());
        headers.set("X-Actor-Id", TenantContext.DEFAULT_USER_ID.toString());

        var response = rest.exchange("/api/v1/systems", org.springframework.http.HttpMethod.GET,
            new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void requestWithAValidBearerTokenIsAccepted() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        tenants.save(new TenantEntity(tenantId, "Bearer Test Tenant", "starter", "EU", Instant.now()));
        users.save(new UserEntity(userId, tenantId, "bearer-test@example.com", UserRole.ADMIN,
            encoder.encode("irrelevant"), Instant.now()));
        String token = jwtService.issueAccessToken(userId, tenantId, UserRole.ADMIN);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        var response = rest.exchange("/api/v1/systems", org.springframework.http.HttpMethod.GET,
            new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void requestWithAnExpiredOrGarbageBearerTokenIsRejected() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer not-a-real-jwt");

        var response = rest.exchange("/api/v1/systems", org.springframework.http.HttpMethod.GET,
            new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void jwksEndpointRemainsReachableWithoutCredentials() {
        var response = rest.getForEntity("/.well-known/jwks.json", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void loginEndpointRemainsReachableWithoutCredentials() {
        var response = rest.postForEntity("/auth/login",
            new os.assurance.eu.api.auth.LoginRequest("nobody@example.com", "x"), String.class);

        // 401 (bad credentials) is fine here — the point is it's not blocked by the tenant filter before reaching the controller.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void unauthenticatedAllowlistIsNarrow() {
        assertThat(TenantContextFilter.isUnauthenticatedPath("/.well-known/jwks.json")).isTrue();
        assertThat(TenantContextFilter.isUnauthenticatedPath("/auth/login")).isTrue();
        assertThat(TenantContextFilter.isUnauthenticatedPath("/auth/refresh")).isTrue();
        assertThat(TenantContextFilter.isUnauthenticatedPath("/auth/logout")).isTrue();
        assertThat(TenantContextFilter.isUnauthenticatedPath("/actuator/health")).isTrue();
        assertThat(TenantContextFilter.isUnauthenticatedPath("/actuator/health/liveness")).isTrue();
        assertThat(TenantContextFilter.isUnauthenticatedPath("/actuator/health/readiness")).isTrue();
        // Part 4 OAuth start + callback
        assertThat(TenantContextFilter.isUnauthenticatedPath("/auth/oauth/google/start")).isTrue();
        assertThat(TenantContextFilter.isUnauthenticatedPath("/auth/oauth/microsoft/callback")).isTrue();

        // Everything else requires credentials — including metrics, systems, and eval callbacks.
        assertThat(TenantContextFilter.isUnauthenticatedPath("/actuator/metrics")).isFalse();
        assertThat(TenantContextFilter.isUnauthenticatedPath("/api/v1/systems")).isFalse();
        assertThat(TenantContextFilter.isUnauthenticatedPath("/api/v1/eval-runs/x/result")).isFalse();
        assertThat(TenantContextFilter.isUnauthenticatedPath("/auth/register")).isFalse();
    }

    @Test
    void actuatorMetricsRequiresAuthentication() {
        var response = rest.getForEntity("/actuator/metrics", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}