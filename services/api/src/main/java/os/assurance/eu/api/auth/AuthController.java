package os.assurance.eu.api.auth;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import os.assurance.eu.api.observability.AssuranceMetrics;
import os.assurance.eu.api.tenant.UserEntity;
import os.assurance.eu.api.tenant.UserJpaRepository;

@RestController
public class AuthController {
    private static final long ACCESS_TOKEN_TTL_SECONDS = 15 * 60;
    private final UserJpaRepository users;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AssuranceMetrics assuranceMetrics;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    // Constant-time defense against email-enumeration via login latency: bcrypt verification
    // always runs against a real hash (this dummy one when no user/password exists), so a
    // nonexistent email and a wrong password take statistically indistinguishable time.
    private final String dummyHashForTimingParity = new BCryptPasswordEncoder(12)
        .encode("dummy-password-never-matches-anything");

    public AuthController(
            UserJpaRepository users,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            AssuranceMetrics assuranceMetrics) {
        this.users = users;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.assuranceMetrics = assuranceMetrics;
    }

    @PostMapping("/auth/login")
    public TokenResponse login(@RequestBody LoginRequest request) {
        UserEntity user = users.findByEmail(request.email()).orElse(null);
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

    private TokenResponse issueTokenPair(java.util.UUID userId, java.util.UUID tenantId, os.assurance.eu.api.tenant.UserRole role) {
        String accessToken = jwtService.issueAccessToken(userId, tenantId, role);
        var refreshToken = refreshTokenService.issue(userId, tenantId);
        return new TokenResponse(accessToken, refreshToken.rawToken(), ACCESS_TOKEN_TTL_SECONDS);
    }
}