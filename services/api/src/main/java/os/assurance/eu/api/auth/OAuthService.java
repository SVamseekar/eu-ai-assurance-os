package os.assurance.eu.api.auth;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import os.assurance.eu.api.audit.AuditChainHeads;
import os.assurance.eu.api.tenant.TenantEntity;
import os.assurance.eu.api.tenant.TenantJpaRepository;
import os.assurance.eu.api.tenant.UserEntity;
import os.assurance.eu.api.tenant.UserJpaRepository;
import os.assurance.eu.api.tenant.UserRole;

/**
 * Resolves or (optionally) provisions a user from an OIDC profile and issues the same
 * JWT + refresh token pair as password login.
 */
@Service
public class OAuthService {
  private static final long ACCESS_TOKEN_TTL_SECONDS = 15 * 60;

  private final OAuthProperties properties;
  private final OAuthStateService stateService;
  private final OAuthTokenClient tokenClient;
  private final UserJpaRepository users;
  private final TenantJpaRepository tenants;
  private final AuditChainHeads auditChainHeads;
  private final JwtService jwtService;
  private final RefreshTokenService refreshTokenService;

  public OAuthService(
      OAuthProperties properties,
      OAuthStateService stateService,
      OAuthTokenClient tokenClient,
      UserJpaRepository users,
      TenantJpaRepository tenants,
      AuditChainHeads auditChainHeads,
      JwtService jwtService,
      RefreshTokenService refreshTokenService) {
    this.properties = properties;
    this.stateService = stateService;
    this.tokenClient = tokenClient;
    this.users = users;
    this.tenants = tenants;
    this.auditChainHeads = auditChainHeads;
    this.jwtService = jwtService;
    this.refreshTokenService = refreshTokenService;
  }

  public String beginAuthorization(String provider, String browserNonce) {
    String normalized = normalizeProvider(provider);
    String state;
    try {
      state = stateService.issue(normalized, browserNonce);
    } catch (IllegalArgumentException e) {
      throw new OAuthLoginException("state", e.getMessage());
    }
    String redirectUri = tokenClient.callbackRedirectUri(normalized);
    return tokenClient.buildAuthorizationUrl(normalized, state, redirectUri);
  }

  @Transactional
  public TokenResponse completeAuthorization(String provider, String code, String state, String browserNonce) {
    String normalized = normalizeProvider(provider);
    OAuthStateService.ValidationResult stateResult = stateService.validate(state, normalized, browserNonce);
    if (!stateResult.isValid()) {
      throw new OAuthLoginException("state", "Invalid or expired OAuth state");
    }
    if (code == null || code.isBlank()) {
      throw new OAuthLoginException("denied", "Missing authorization code");
    }

    String redirectUri = tokenClient.callbackRedirectUri(normalized);
    Map<String, Object> tokenResponse = tokenClient.exchangeCode(normalized, code, redirectUri);
    Map<String, Object> userInfo = tokenClient.fetchUserInfo(normalized, tokenResponse);
    OAuthProviderProfile profile = OAuthProviderProfile.fromUserInfo(normalized, userInfo);

    UserEntity user = resolveUser(profile);
    return issueTokenPair(user);
  }

  UserEntity resolveUser(OAuthProviderProfile profile) {
    UserEntity byOauth = users
        .findByOauthProviderAndOauthSubject(profile.provider(), profile.subject())
        .orElse(null);
    if (byOauth != null) {
      return byOauth;
    }

    if (!profile.emailVerified()) {
      throw new OAuthLoginException(
          "email_unverified",
          "Your identity provider did not confirm this email address. Sign in with your password, "
              + "then link this provider from Settings.");
    }

    UserEntity byEmail = users.findByEmailIgnoreCase(profile.email()).orElse(null);
    if (byEmail != null) {
      byEmail.linkOAuth(profile.provider(), profile.subject());
      return users.save(byEmail);
    }

    if (!properties.isAutoProvision()) {
      throw new OAuthLoginException(
          "not_provisioned",
          "No account is provisioned for this identity. Contact your administrator.");
    }

    return provisionNewTenantAdmin(profile);
  }

  private UserEntity provisionNewTenantAdmin(OAuthProviderProfile profile) {
    Instant now = Instant.now();
    UUID tenantId = UUID.randomUUID();
    String tenantName = profile.displayName() + "'s workspace";
    tenants.save(new TenantEntity(tenantId, tenantName, "trial", "EU", now));
    auditChainHeads.attachToNewTenant(tenantId);

    UserEntity user = new UserEntity(
        UUID.randomUUID(),
        tenantId,
        profile.email(),
        UserRole.ADMIN,
        null,
        profile.provider(),
        profile.subject(),
        now);
    user.markEmailVerified(now);
    return users.save(user);
  }

  private TokenResponse issueTokenPair(UserEntity user) {
    String accessToken = jwtService.issueAccessToken(user.id(), user.tenantId(), user.role());
    var refreshToken = refreshTokenService.issue(user.id(), user.tenantId());
    return new TokenResponse(accessToken, refreshToken.rawToken(), ACCESS_TOKEN_TTL_SECONDS);
  }

  private static String normalizeProvider(String provider) {
    if (provider == null) {
      throw new OAuthLoginException("unsupported_provider", "Provider is required");
    }
    String normalized = provider.trim().toLowerCase(Locale.ROOT);
    if (!"google".equals(normalized) && !"microsoft".equals(normalized)) {
      throw new OAuthLoginException("unsupported_provider", "Unsupported OAuth provider: " + provider);
    }
    return normalized;
  }

  public static class OAuthLoginException extends RuntimeException {
    private final String code;

    public OAuthLoginException(String code, String message) {
      super(message);
      this.code = code;
    }

    public String code() {
      return code;
    }
  }
}
