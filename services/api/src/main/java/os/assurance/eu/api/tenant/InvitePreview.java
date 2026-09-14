package os.assurance.eu.api.tenant;

import java.time.Instant;

public record InvitePreview(
    String email,
    UserRole role,
    String tenantName,
    Instant expiresAt,
    boolean expired,
    boolean accepted) {
}
