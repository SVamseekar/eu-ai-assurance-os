package os.assurance.eu.api.auth;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import os.assurance.eu.api.audit.AuditChainHeads;
import os.assurance.eu.api.audit.AuditService;
import os.assurance.eu.api.email.EmailSender;
import os.assurance.eu.api.email.EmailTemplates;
import os.assurance.eu.api.tenant.TenantContext;
import os.assurance.eu.api.tenant.TenantEntity;
import os.assurance.eu.api.tenant.TenantJpaRepository;
import os.assurance.eu.api.tenant.UserEntity;
import os.assurance.eu.api.tenant.UserJpaRepository;
import os.assurance.eu.api.tenant.UserRole;

@Service
public class SignupService {
  static final Duration VERIFY_TTL = Duration.ofHours(24);
  private static final long ACCESS_TOKEN_TTL_SECONDS = 15 * 60;
  private final UserJpaRepository users;
  private final TenantJpaRepository tenants;
  private final AuthTokenService tokens;
  private final EmailSender email;
  private final AuditService audit;
  private final AuditChainHeads chainHeads;
  private final TenantContext tenantContext;
  private final JwtService jwtService;
  private final RefreshTokenService refreshTokens;
  private final Clock clock;
  private final String baseUrl;
  private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

  public SignupService(UserJpaRepository users, TenantJpaRepository tenants, AuthTokenService tokens,
      EmailSender email, AuditService audit, AuditChainHeads chainHeads, TenantContext tenantContext,
      JwtService jwtService, RefreshTokenService refreshTokens, Clock clock,
      @Value("${assurance.app.base-url}") String baseUrl) {
    this.users = users;
    this.tenants = tenants;
    this.tokens = tokens;
    this.email = email;
    this.audit = audit;
    this.chainHeads = chainHeads;
    this.tenantContext = tenantContext;
    this.jwtService = jwtService;
    this.refreshTokens = refreshTokens;
    this.clock = clock;
    this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
  }

  @Transactional
  public void signup(SignupRequest request) {
    String normalized = request.email().trim().toLowerCase(Locale.ROOT);
    if (users.existsByEmailIgnoreCase(normalized)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT,
          "An account with this email already exists — sign in or reset your password.");
    }
    Instant now = clock.instant();
    UUID tenantId = UUID.randomUUID();
    tenants.save(new TenantEntity(tenantId, request.organisationName().trim(), "trial", "EU", now));
    chainHeads.attachToNewTenant(tenantId);
    UserEntity user = users.save(new UserEntity(
        UUID.randomUUID(), tenantId, normalized, UserRole.ADMIN, encoder.encode(request.password()), now));
    tenantContext.setOverrides(tenantId, user.id());
    try {
      audit.append(null, "tenant.self_signup", "tenant", tenantId.toString(),
          Map.of("organisationName", request.organisationName().trim(), "email", normalized));
    } finally {
      tenantContext.clearOverrides();
    }
    sendVerification(user);
  }

  @Transactional
  public void resend(String rawEmail) {
    if (rawEmail == null) {
      return;
    }
    users.findByEmailIgnoreCase(rawEmail.trim())
        .filter(u -> u.emailVerifiedAt() == null)
        .ifPresent(this::sendVerification);
  }

  @Transactional
  public TokenResponse verify(String rawToken) {
    UUID userId = tokens.consume(rawToken, AuthTokenPurpose.VERIFY_EMAIL);
    UserEntity user = users.findById(userId).orElseThrow(
        () -> new ResponseStatusException(HttpStatus.GONE, "This link is invalid, already used, or expired"));
    user.markEmailVerified(clock.instant());
    users.save(user);
    String access = jwtService.issueAccessToken(user.id(), user.tenantId(), user.role());
    var refresh = refreshTokens.issue(user.id(), user.tenantId());
    return new TokenResponse(access, refresh.rawToken(), ACCESS_TOKEN_TTL_SECONDS);
  }

  private void sendVerification(UserEntity user) {
    String raw = tokens.issue(user.id(), AuthTokenPurpose.VERIFY_EMAIL, VERIFY_TTL);
    email.send(EmailTemplates.verifyEmail(baseUrl + "/verify-email?token=" + raw).withTo(user.email()));
  }
}
