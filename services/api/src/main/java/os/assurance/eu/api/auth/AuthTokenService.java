package os.assurance.eu.api.auth;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import os.assurance.eu.api.tenant.ApiKeyHasher;
import os.assurance.eu.api.tenant.UserEntity;

@Service
public class AuthTokenService {
  private final SecureRandom random = new SecureRandom();
  private final AuthTokenJpaRepository tokens;
  private final Clock clock;
  private final EntityManager entityManager;
  private final Duration cooldown;
  private final int maxPerHour;

  public AuthTokenService(AuthTokenJpaRepository tokens, Clock clock, EntityManager entityManager,
      @Value("${assurance.auth.email.cooldown-seconds:60}") long cooldownSeconds,
      @Value("${assurance.auth.email.max-per-hour:5}") int maxPerHour) {
    this.tokens = tokens;
    this.clock = clock;
    this.entityManager = entityManager;
    this.cooldown = Duration.ofSeconds(cooldownSeconds);
    this.maxPerHour = maxPerHour;
  }

  /**
   * Per-user, per-purpose send limit: one link per cooldown and a cap per hour. Callers skip the
   * send silently when false, so the response never reveals the limit and nobody can be locked out.
   * Invariant: the one-hour cap window is never longer than a reset link's lifetime, so whenever the
   * cap suppresses a send, a still-valid link from an earlier send is already in the owner's inbox.
   * Must run inside the transaction that issues the token.
   */
  @Transactional(propagation = Propagation.MANDATORY)
  public boolean canIssue(UUID userId, AuthTokenPurpose purpose) {
    // Lock the user row until the caller's transaction ends, so parallel requests for one address
    // queue up and the later ones see the earlier one's token instead of all passing the check.
    entityManager.find(UserEntity.class, userId, LockModeType.PESSIMISTIC_WRITE);
    Instant now = clock.instant();
    if (!cooldown.isZero() && tokens.countIssuedSince(userId, purpose, now.minus(cooldown)) > 0) {
      return false;
    }
    return tokens.countIssuedSince(userId, purpose, now.minus(Duration.ofHours(1))) < maxPerHour;
  }

  /** Retires every unused link of this purpose for the user. */
  @Transactional
  public void invalidateOutstanding(UUID userId, AuthTokenPurpose purpose) {
    tokens.markOutstandingUsed(userId, purpose, clock.instant());
  }

  @Transactional
  public String issue(UUID userId, AuthTokenPurpose purpose, Duration ttl) {
    byte[] bytes = new byte[32];
    random.nextBytes(bytes);
    String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    Instant now = clock.instant();
    tokens.save(new AuthTokenEntity(UUID.randomUUID(), userId, purpose, ApiKeyHasher.sha256Hex(raw), now.plus(ttl), now));
    return raw;
  }

  /** Marks the token used and returns its user; 410 when unknown, wrong purpose, used, or expired. */
  @Transactional
  public UUID consume(String raw, AuthTokenPurpose purpose) {
    if (raw == null || raw.isBlank()) {
      throw gone();
    }
    AuthTokenEntity token = tokens.findForUpdateByHash(ApiKeyHasher.sha256Hex(raw)).orElseThrow(AuthTokenService::gone);
    Instant now = clock.instant();
    if (token.purpose() != purpose || token.usedAt() != null || token.expiresAt().isBefore(now)) {
      throw gone();
    }
    token.markUsed(now);
    tokens.save(token);
    return token.userId();
  }

  private static ResponseStatusException gone() {
    return new ResponseStatusException(HttpStatus.GONE, "This link is invalid, already used, or expired");
  }
}
