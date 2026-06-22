package os.assurance.eu.api.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import os.assurance.eu.api.tenant.TenantEntity;
import os.assurance.eu.api.tenant.TenantJpaRepository;
import os.assurance.eu.api.tenant.UserEntity;
import os.assurance.eu.api.tenant.UserJpaRepository;
import os.assurance.eu.api.tenant.UserRole;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private TenantJpaRepository tenants;

    @Autowired
    private UserJpaRepository users;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @BeforeEach
    void acceptErrorResponsesWithoutRetrying() {
        var restTemplate = rest.getRestTemplate();
        restTemplate.setRequestFactory(new JdkClientHttpRequestFactory());
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(org.springframework.http.client.ClientHttpResponse response) {
                return false;
            }
        });
    }

    private UUID seedUser(String email, String rawPassword) {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        tenants.save(new TenantEntity(tenantId, "Test Tenant", "starter", "EU", Instant.now()));
        users.save(new UserEntity(userId, tenantId, email, UserRole.ADMIN, encoder.encode(rawPassword), Instant.now()));
        return userId;
    }

    @Test
    void loginWithValidCredentialsReturnsTokens() {
        seedUser("login-valid@example.com", "correct-password");

        var response = rest.postForEntity(
            "/auth/login", new LoginRequest("login-valid@example.com", "correct-password"), TokenResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().accessToken()).isNotBlank();
        assertThat(response.getBody().refreshToken()).isNotBlank();
    }

    @Test
    void loginWithWrongPasswordIsRejected() {
        seedUser("login-wrong@example.com", "correct-password");

        var response = rest.postForEntity(
            "/auth/login", new LoginRequest("login-wrong@example.com", "wrong-password"), TokenResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void loginWithUnknownEmailIsRejectedWithTheSameStatusAsWrongPassword() {
        var response = rest.postForEntity(
            "/auth/login", new LoginRequest("no-such-user@example.com", "anything"), TokenResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void refreshIssuesANewTokenPair() {
        seedUser("refresh-flow@example.com", "correct-password");
        var login = rest.postForEntity(
            "/auth/login", new LoginRequest("refresh-flow@example.com", "correct-password"), TokenResponse.class);

        var refreshed = rest.postForEntity(
            "/auth/refresh", new RefreshRequest(login.getBody().refreshToken()), TokenResponse.class);

        assertThat(refreshed.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(refreshed.getBody().refreshToken()).isNotEqualTo(login.getBody().refreshToken());
    }

    @Test
    void logoutThenRefreshIsRejected() {
        seedUser("logout-flow@example.com", "correct-password");
        var login = rest.postForEntity(
            "/auth/login", new LoginRequest("logout-flow@example.com", "correct-password"), TokenResponse.class);

        rest.postForEntity("/auth/logout", new RefreshRequest(login.getBody().refreshToken()), Void.class);
        var refreshAttempt = rest.postForEntity(
            "/auth/refresh", new RefreshRequest(login.getBody().refreshToken()), TokenResponse.class);

        assertThat(refreshAttempt.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}