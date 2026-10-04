package os.assurance.eu.api.auth;

import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import os.assurance.eu.api.audit.AuditService;
import os.assurance.eu.api.email.AfterCommit;
import os.assurance.eu.api.email.EmailMessage;
import os.assurance.eu.api.email.EmailSender;
import os.assurance.eu.api.email.EmailTemplates;
import os.assurance.eu.api.tenant.TenantContext;
import os.assurance.eu.api.tenant.UserEntity;
import os.assurance.eu.api.tenant.UserJpaRepository;

@Service
public class PasswordResetService {
  static final Duration RESET_TTL = Duration.ofHours(1);
  private final UserJpaRepository users;
  private final AuthTokenService tokens;
  private final RefreshTokenService refreshTokens;
  private final EmailSender email;
  private final AuditService audit;
  private final TenantContext tenantContext;
  private final Clock clock;
  private final String baseUrl;
  private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

  public PasswordResetService(UserJpaRepository users, AuthTokenService tokens, RefreshTokenService refreshTokens,
      EmailSender email, AuditService audit, TenantContext tenantContext, Clock clock,
      @Value("${assurance.app.base-url}") String baseUrl) {
    this.users = users;
    this.tokens = tokens;
    this.refreshTokens = refreshTokens;
    this.email = email;
    this.audit = audit;
    this.tenantContext = tenantContext;
    this.clock = clock;
    this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
  }

  @Transactional
  public void forgot(String rawEmail) {
    if (rawEmail == null || rawEmail.isBlank()) {
      return;
    }
    users.findByEmailIgnoreCase(rawEmail.trim())
        .filter(u -> tokens.canIssue(u.id(), AuthTokenPurpose.RESET_PASSWORD))
        .ifPresent(user -> {
      String raw = tokens.issue(user.id(), AuthTokenPurpose.RESET_PASSWORD, RESET_TTL);
      EmailMessage message = EmailTemplates.resetPassword(baseUrl + "/reset-password?token=" + raw).withTo(user.email());
      AfterCommit.run(() -> email.send(message));
    });
  }

  @Transactional
  public void reset(String rawToken, String newPassword) {
    if (newPassword == null || newPassword.length() < 12 || newPassword.length() > 128) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords must be 12–128 characters");
    }
    UUID userId = tokens.consume(rawToken, AuthTokenPurpose.RESET_PASSWORD);
    UserEntity user = users.findById(userId).orElseThrow(
        () -> new ResponseStatusException(HttpStatus.GONE, "This link is invalid, already used, or expired"));
    user.setPasswordHash(encoder.encode(newPassword));
    if (user.emailVerifiedAt() == null) {
      user.markEmailVerified(clock.instant());
    }
    users.save(user);
    tokens.invalidateOutstanding(user.id(), AuthTokenPurpose.RESET_PASSWORD);
    tokens.invalidateOutstanding(user.id(), AuthTokenPurpose.VERIFY_EMAIL);
    refreshTokens.revokeAllForUser(user.id());
    tenantContext.setOverrides(user.tenantId(), user.id());
    try {
      audit.append(null, "user.password_reset", "user", user.id().toString(), Map.of("email", user.email()));
    } finally {
      tenantContext.clearOverrides();
    }
    EmailMessage changed = EmailTemplates.passwordChanged().withTo(user.email());
    AfterCommit.run(() -> email.send(changed));
  }
}
