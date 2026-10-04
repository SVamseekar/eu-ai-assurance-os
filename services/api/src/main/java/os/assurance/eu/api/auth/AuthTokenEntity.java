package os.assurance.eu.api.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "auth_tokens")
public class AuthTokenEntity {
  @Id private UUID id;
  @Column(name = "user_id", nullable = false) private UUID userId;
  @Enumerated(EnumType.STRING) @Column(nullable = false) private AuthTokenPurpose purpose;
  @Column(name = "token_hash", nullable = false, unique = true) private String tokenHash;
  @Column(name = "expires_at", nullable = false) private Instant expiresAt;
  @Column(name = "used_at") private Instant usedAt;
  @Column(name = "created_at", nullable = false) private Instant createdAt;

  protected AuthTokenEntity() {
  }

  public AuthTokenEntity(UUID id, UUID userId, AuthTokenPurpose purpose, String tokenHash, Instant expiresAt, Instant createdAt) {
    this.id = id;
    this.userId = userId;
    this.purpose = purpose;
    this.tokenHash = tokenHash;
    this.expiresAt = expiresAt;
    this.createdAt = createdAt;
  }

  public UUID userId() { return userId; }
  public AuthTokenPurpose purpose() { return purpose; }
  public Instant expiresAt() { return expiresAt; }
  public Instant usedAt() { return usedAt; }
  public void markUsed(Instant at) { this.usedAt = at; }
}
