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
    void unknownEmailAndWrongPasswordTakeStatisticallyIndistinguishableTime() {
        seedUser("timing-control@example.com", "correct-password");

        // Warm up the JIT/connection pool before measuring, then take several samples of each
        // path and compare medians — a bcrypt-skipping timing oracle would show as a
        // consistent multi-millisecond gap between the two, not noise-level variance.
        for (int i = 0; i < 3; i++) {
            rest.postForEntity("/auth/login", new LoginRequest("timing-control@example.com", "wrong"), TokenResponse.class);
            rest.postForEntity("/auth/login", new LoginRequest("nobody-" + i + "@example.com", "wrong"), TokenResponse.class);
        }

        long[] wrongPasswordTimes = new long[7];
        long[] unknownEmailTimes = new long[7];
        for (int i = 0; i < 7; i++) {
            long start = System.nanoTime();
            rest.postForEntity("/auth/login", new LoginRequest("timing-control@example.com", "wrong"), TokenResponse.class);
            wrongPasswordTimes[i] = System.nanoTime() - start;

            start = System.nanoTime();
            rest.postForEntity("/auth/login", new LoginRequest("nobody-sample-" + i + "@example.com", "wrong"), TokenResponse.class);
            unknownEmailTimes[i] = System.nanoTime() - start;
        }

        long wrongPasswordMedian = median(wrongPasswordTimes);
        long unknownEmailMedian = median(unknownEmailTimes);
        double ratio = (double) Math.max(wrongPasswordMedian, unknownEmailMedian)
            / Math.min(wrongPasswordMedian, unknownEmailMedian);

        // A bcrypt-skipping oracle produces an order-of-magnitude gap (no hash vs. a ~60-100ms
        // bcrypt-12 hash); same-cost paths stay within normal HTTP/JVM noise. 3x is a generous
        // ceiling that would already fail on a real timing leak while tolerating test-env jitter.
        assertThat(ratio).isLessThan(3.0);
    }

    private long median(long[] values) {
        long[] sorted = values.clone();
        java.util.Arrays.sort(sorted);
        return sorted[sorted.length / 2];
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