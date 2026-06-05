package os.assurance.eu.api.tenant;

import java.util.Arrays;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TenantAuthorizationService {
  private final TenantContext tenantContext;
  private final UserJpaRepository users;

  public TenantAuthorizationService(TenantContext tenantContext, UserJpaRepository users) {
    this.tenantContext = tenantContext;
    this.users = users;
  }

  public void requireAnyRole(UserRole... allowedRoles) {
    UserRole actorRole = users.findByIdAndTenantId(tenantContext.actorId(), tenantContext.tenantId())
        .map(UserEntity::role)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unknown actor for tenant"));
    boolean allowed = Arrays.stream(allowedRoles).anyMatch(actorRole::equals);
    if (!allowed) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Actor is not authorized for this operation");
    }
  }
}
