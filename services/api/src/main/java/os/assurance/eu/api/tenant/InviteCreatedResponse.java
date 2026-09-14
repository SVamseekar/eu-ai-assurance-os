package os.assurance.eu.api.tenant;

import java.time.Instant;
import java.util.UUID;

/**
 * {@code inviteToken} is shown once. Store it out of band (email). It is not persisted in cleartext.
 */
public record InviteCreatedResponse(
    UUID inviteId,
    UUID tenantId,
    String email,
    UserRole role,
    Instant expiresAt,
    String inviteToken,
    String acceptPath) {
}
