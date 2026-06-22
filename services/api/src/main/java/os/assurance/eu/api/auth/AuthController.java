package os.assurance.eu.api.auth;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import os.assurance.eu.api.tenant.UserEntity;
import os.assurance.eu.api.tenant.UserJpaRepository;

@RestController
public class AuthController {
    private static final long ACCESS_TOKEN_TTL_SECONDS = 15 * 60;
    private final UserJpaRepository users;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    public AuthController(UserJpaRepository users, JwtService jwtService, RefreshTokenService refreshTokenService) {
        this.users = users;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/auth/login")
    public TokenResponse login(@RequestBody LoginRequest request) {
        UserEntity user = users.findByEmail(request.email()).orElse(null);
        if (user == null || user.passwordHash() == null
                || !passwordEncoder.matches(request.password(), user.passwordHash())) {
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