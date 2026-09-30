package os.assurance.eu.api.auth;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import os.assurance.eu.api.observability.AssuranceMetrics;
import os.assurance.eu.api.tenant.AcceptInviteRequest;
import os.assurance.eu.api.tenant.InvitePreview;
import os.assurance.eu.api.tenant.TenantAdminService;
import os.assurance.eu.api.tenant.UserEntity;
import os.assurance.eu.api.tenant.UserJpaRepository;

@RestController
public class AuthController {
    private static final long ACCESS_TOKEN_TTL_SECONDS = 15 * 60;
    private final UserJpaRepository users;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AssuranceMetrics assuranceMetrics;
    private final TenantAdminService tenantAdminService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    private final SlidingWindowRateLimiter perEmail;

    // Constant-time defense against email-enumeration via login latency: bcrypt verification
    // always runs against a real hash (this dummy one when no user/password exists), so a
    // nonexistent email and a wrong password take statistically indistinguishable time.
    private final String dummyHashForTimingParity = new BCryptPasswordEncoder(12)
        .encode("dummy-password-never-matches-anything");

    public AuthController(
            UserJpaRepository users,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            AssuranceMetrics assuranceMetrics,
            TenantAdminService tenantAdminService,
            @org.springframework.beans.factory.annotation.Value("${assurance.security.auth-rate.per-email-per-15m:10}") int perEmailLimit,
            java.time.Clock clock) {
        this.users = users;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.assuranceMetrics = assuranceMetrics;
        this.tenantAdminService = tenantAdminService;
        this.perEmail = new SlidingWindowRateLimiter(perEmailLimit, java.time.Duration.ofMinutes(15), clock);
    }

    @PostMapping("/auth/login")
    public TokenResponse login(@RequestBody LoginRequest request) {
        String emailKey = request.email() == null ? "" : request.email().trim().toLowerCase(java.util.Locale.ROOT);
        if (!perEmail.tryAcquire(emailKey)) {
            throw new TooManyAttemptsException();
        }
        UserEntity user = users.findByEmailIgnoreCase(emailKey).orElse(null);
        String hashToVerifyAgainst = (user != null && user.passwordHash() != null)
            ? user.passwordHash()
            : dummyHashForTimingParity;
        boolean passwordMatches = passwordEncoder.matches(request.password(), hashToVerifyAgainst);
        if (user == null || user.passwordHash() == null || !passwordMatches) {
            // Single reason tag — do not distinguish unknown user vs bad password (enumeration).
            assuranceMetrics.authLoginFailure("invalid_credentials");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        return issueTokenPair(user.id(), user.tenantId(), user.role());
    }

    @PostMapping("/auth/refresh")
    public TokenResponse refresh(@RequestBody RefreshRequest request) {
        var result = refreshTokenService.rotate(request.refreshToken());
        if (result instanceof RefreshTokenService.RefreshResult.Rejected rejected) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, rejected.reason());
        }
        var rotated = (RefreshTokenService.RefreshResult.Rotated) result;
        UserEntity user = users.findByIdAndTenantId(rotated.userId(), rotated.tenantId()).orElse(null);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User no longer exists");
        }
        String accessToken = jwtService.issueAccessToken(user.id(), user.tenantId(), user.role());
        return new TokenResponse(accessToken, rotated.newToken().rawToken(), ACCESS_TOKEN_TTL_SECONDS);
    }

    @PostMapping("/auth/logout")
    public void logout(@RequestBody RefreshRequest request) {
        refreshTokenService.revoke(request.refreshToken());
    }

    @GetMapping("/auth/invites/{token}")
    public InvitePreview previewInvite(@PathVariable String token) {
        return tenantAdminService.previewInvite(token);
    }

    @PostMapping("/auth/accept-invite")
    public TokenResponse acceptInvite(@Valid @RequestBody AcceptInviteRequest request) {
        return tenantAdminService.acceptInvite(request);
    }

    private TokenResponse issueTokenPair(java.util.UUID userId, java.util.UUID tenantId, os.assurance.eu.api.tenant.UserRole role) {
        String accessToken = jwtService.issueAccessToken(userId, tenantId, role);
        var refreshToken = refreshTokenService.issue(userId, tenantId);
        return new TokenResponse(accessToken, refreshToken.rawToken(), ACCESS_TOKEN_TTL_SECONDS);
    }
}
