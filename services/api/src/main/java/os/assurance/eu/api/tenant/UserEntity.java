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
@Table(name = "users")
public class UserEntity {
  @Id
  private UUID id;

  @Column(nullable = false)
  private UUID tenantId;

  @Column(nullable = false)
  private String email;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserRole role;

  @Column(name = "password_hash")
  private String passwordHash;

  @Column(name = "oauth_provider")
  private String oauthProvider;

  @Column(name = "oauth_subject")
  private String oauthSubject;

  @Column(nullable = false)
  private Instant createdAt;

  protected UserEntity() {
  }

  public UserEntity(UUID id, UUID tenantId, String email, UserRole role, Instant createdAt) {
    this(id, tenantId, email, role, null, null, null, createdAt);
  }

  public UserEntity(UUID id, UUID tenantId, String email, UserRole role, String passwordHash, Instant createdAt) {
    this(id, tenantId, email, role, passwordHash, null, null, createdAt);
  }

  public UserEntity(
      UUID id,
      UUID tenantId,
      String email,
      UserRole role,
      String passwordHash,
      String oauthProvider,
      String oauthSubject,
      Instant createdAt) {
    this.id = id;
    this.tenantId = tenantId;
    this.email = email;
    this.role = role;
    this.passwordHash = passwordHash;
    this.oauthProvider = oauthProvider;
    this.oauthSubject = oauthSubject;
    this.createdAt = createdAt;
  }

  public UserRole role() {
    return role;
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

  public String passwordHash() {
    return passwordHash;
  }

  public String oauthProvider() {
    return oauthProvider;
  }

  public String oauthSubject() {
    return oauthSubject;
  }

  public void linkOAuth(String provider, String subject) {
    this.oauthProvider = provider;
    this.oauthSubject = subject;
  }
}
