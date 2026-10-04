package os.assurance.eu.api.auth;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import os.assurance.eu.api.tenant.ApiKeyHasher;

@Service
public class AuthTokenService {
  private final SecureRandom random = new SecureRandom();
  private final AuthTokenJpaRepository tokens;
  private final Clock clock;

  public AuthTokenService(AuthTokenJpaRepository tokens, Clock clock) {
    this.tokens = tokens;
    this.clock = clock;
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
