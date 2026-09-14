package os.assurance.eu.api.tenant;

import java.time.Instant;
import java.util.UUID;

public record UserView(
    UUID id,
    UUID tenantId,
    String email,
    UserRole role,
    boolean hasPassword,
    String oauthProvider,
    Instant createdAt) {

  public static UserView from(UserEntity user) {
    return new UserView(
        user.id(),
        user.tenantId(),
        user.email(),
        user.role(),
        user.passwordHash() != null && !user.passwordHash().isBlank(),
        user.oauthProvider(),
        user.createdAt());
  }
}
