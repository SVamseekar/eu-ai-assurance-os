package os.assurance.eu.api.tenant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_invites")
public class UserInviteEntity {
  @Id
  private UUID id;

  @Column(nullable = false)
  private UUID tenantId;

  @Column(nullable = false)
  private String email;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserRole role;

  @Column(nullable = false)
  private String tokenHash;

  @Column(nullable = false)
  private UUID invitedBy;

  @Column(nullable = false)
  private Instant expiresAt;

  private Instant acceptedAt;

  @Column(nullable = false)
  private Instant createdAt;

  protected UserInviteEntity() {
  }

  public UserInviteEntity(
      UUID id,
      UUID tenantId,
      String email,
      UserRole role,
      String tokenHash,
      UUID invitedBy,
      Instant expiresAt,
      Instant createdAt) {
    this.id = id;
    this.tenantId = tenantId;
    this.email = email;
    this.role = role;
    this.tokenHash = tokenHash;
    this.invitedBy = invitedBy;
    this.expiresAt = expiresAt;
    this.createdAt = createdAt;
  }

  public UUID id() {
    return id;
  }

  public UUID tenantId() {
    return tenantId;
  }

  public String email() {
    return email;
  }

  public UserRole role() {
    return role;
  }

  public String tokenHash() {
    return tokenHash;
  }

  public UUID invitedBy() {
    return invitedBy;
  }

  public Instant expiresAt() {
    return expiresAt;
  }

  public Instant acceptedAt() {
    return acceptedAt;
  }

  public Instant createdAt() {
    return createdAt;
  }

  public void markAccepted(Instant at) {
    this.acceptedAt = at;
  }
}
