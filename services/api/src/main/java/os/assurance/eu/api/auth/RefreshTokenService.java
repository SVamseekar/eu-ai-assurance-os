package os.assurance.eu.api.auth;

import java.security.SecureRandom;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService {
    private static final long REFRESH_TOKEN_TTL_DAYS = 30;
    private final SecureRandom random = new SecureRandom();
    private final RefreshTokenJpaRepository repository;
    private final java.time.Duration reuseGrace;

    public RefreshTokenService(
            RefreshTokenJpaRepository repository,
            @org.springframework.beans.factory.annotation.Value("${assurance.auth.refresh-reuse-grace-seconds:30}")
            long reuseGraceSeconds) {
        this.repository = repository;
        this.reuseGrace = java.time.Duration.ofSeconds(reuseGraceSeconds);
    }

    public record IssuedRefreshToken(String rawToken, UUID id) {}

    public sealed interface RefreshResult permits RefreshResult.Rotated, RefreshResult.Rejected {
        record Rotated(IssuedRefreshToken newToken, UUID userId, UUID tenantId) implements RefreshResult {}
        record Rejected(String reason) implements RefreshResult {}
    }

    public IssuedRefreshToken issue(UUID userId, UUID tenantId) {
        String rawToken = generateRawToken();
        UUID id = UUID.randomUUID();
        repository.save(new RefreshTokenEntity(
            id, tenantId, userId, hash(rawToken),
            Instant.now().plus(REFRESH_TOKEN_TTL_DAYS, ChronoUnit.DAYS), Instant.now()));
        return new IssuedRefreshToken(rawToken, id);
    }

    @org.springframework.transaction.annotation.Transactional
    public RefreshResult rotate(String rawToken) {
        String presentedHash = hash(rawToken);
        RefreshTokenEntity entity = repository.findForUpdateByTokenHash(presentedHash).orElse(null);
        if (entity == null) {
            return new RefreshResult.Rejected("Unknown refresh token");
        }
        if (entity.isRevoked()) {
            if (withinConcurrentRefreshGrace(entity)) {
                IssuedRefreshToken sibling = issue(entity.userId(), entity.tenantId());
                return new RefreshResult.Rotated(sibling, entity.userId(), entity.tenantId());
            }
            revokeChainFrom(entity);
            return new RefreshResult.Rejected("Refresh token reuse detected — chain revoked");
        }
        if (entity.isExpired()) {
            return new RefreshResult.Rejected("Refresh token expired");
        }
        IssuedRefreshToken newToken = issue(entity.userId(), entity.tenantId());
        entity.revoke(hash(newToken.rawToken()));
        repository.save(entity);
        return new RefreshResult.Rotated(newToken, entity.userId(), entity.tenantId());
    }

    /** A parallel request raced a rotation that just happened; the successor is still live. Not theft. */
    private boolean withinConcurrentRefreshGrace(RefreshTokenEntity entity) {
        if (reuseGrace.isZero() || entity.replacedByTokenHash() == null || entity.isExpired()) {
            return false;
        }
        if (entity.revokedAt().isBefore(Instant.now().minus(reuseGrace))) {
            return false;
        }
        return repository.findByTokenHash(entity.replacedByTokenHash())
            .filter(successor -> !successor.isRevoked())
            .isPresent();
    }

    @org.springframework.transaction.annotation.Transactional
    public void revoke(String rawToken) {
        repository.findByTokenHash(hash(rawToken)).ifPresent(entity -> {
            if (!entity.isRevoked()) {
                entity.revoke(null);
                repository.save(entity);
            }
        });
    }

    private void revokeChainFrom(RefreshTokenEntity entity) {
        String nextHash = entity.replacedByTokenHash();
        while (nextHash != null) {
            RefreshTokenEntity next = repository.findByTokenHash(nextHash).orElse(null);
            if (next == null || next.isRevoked()) {
                break;
            }
            String afterNext = next.replacedByTokenHash();
            next.revoke(null);
            repository.save(next);
            nextHash = afterNext;
        }
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hashed.length * 2);
            for (byte b : hashed) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
