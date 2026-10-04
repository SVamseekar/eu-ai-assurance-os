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

  /**
   * Always answers the same way. A new address gets a passwordless account and a link; an unverified
   * one gets a fresh link (and the latest organisation name); a verified one is left untouched and its
   * owner gets a notice. The password is only ever chosen on the emailed link.
   */
  @Transactional
  public void signup(SignupRequest request) {
    String normalized = request.email().trim().toLowerCase(Locale.ROOT);
    String orgName = request.organisationName().trim();
    UserEntity existing = users.findByEmailIgnoreCase(normalized).orElse(null);
    if (existing == null) {
      createAccount(normalized, orgName);
    } else if (existing.emailVerifiedAt() == null) {
      if (tokens.canIssue(existing.id(), AuthTokenPurpose.VERIFY_EMAIL)) {
        tenants.findById(existing.tenantId()).ifPresent(t -> {
          t.rename(orgName);
          tenants.save(t);
        });
        sendVerification(existing);
      }
    } else if (tokens.canIssue(existing.id(), AuthTokenPurpose.RESET_PASSWORD)) {
      String raw = tokens.issue(existing.id(), AuthTokenPurpose.RESET_PASSWORD, PasswordResetService.RESET_TTL);
      email.send(EmailTemplates.accountExists(baseUrl + "/reset-password?token=" + raw).withTo(existing.email()));
    }
  }

  private void createAccount(String normalized, String orgName) {
    Instant now = clock.instant();
    UUID tenantId = UUID.randomUUID();
    tenants.save(new TenantEntity(tenantId, orgName, "trial", "EU", now));
    chainHeads.attachToNewTenant(tenantId);
    UserEntity user = users.save(new UserEntity(UUID.randomUUID(), tenantId, normalized, UserRole.ADMIN, now));
    tenantContext.setOverrides(tenantId, user.id());
    try {
      audit.append(null, "tenant.self_signup", "tenant", tenantId.toString(),
          Map.of("organisationName", orgName, "email", normalized));
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
        .filter(u -> tokens.canIssue(u.id(), AuthTokenPurpose.VERIFY_EMAIL))
        .ifPresent(this::sendVerification);
  }

  @Transactional
  public TokenResponse verify(String rawToken, String password) {
    if (password == null || password.length() < 12 || password.length() > 128) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords must be 12–128 characters");
    }
    UUID userId = tokens.consume(rawToken, AuthTokenPurpose.VERIFY_EMAIL);
    UserEntity user = users.findById(userId)
        .filter(u -> u.emailVerifiedAt() == null)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.GONE, "This link is invalid, already used, or expired"));
    user.setPasswordHash(encoder.encode(password));
    user.markEmailVerified(clock.instant());
    users.save(user);
    tokens.invalidateOutstanding(user.id(), AuthTokenPurpose.VERIFY_EMAIL);
    String access = jwtService.issueAccessToken(user.id(), user.tenantId(), user.role());
    var refresh = refreshTokens.issue(user.id(), user.tenantId());
    return new TokenResponse(access, refresh.rawToken(), ACCESS_TOKEN_TTL_SECONDS);
  }

  private void sendVerification(UserEntity user) {
    String raw = tokens.issue(user.id(), AuthTokenPurpose.VERIFY_EMAIL, VERIFY_TTL);
    email.send(EmailTemplates.verifyEmail(baseUrl + "/verify-email?token=" + raw).withTo(user.email()));
  }
}
