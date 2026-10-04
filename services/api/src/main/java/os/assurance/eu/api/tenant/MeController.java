package os.assurance.eu.api.tenant;

import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import os.assurance.eu.api.demo.DemoProperties;

/** Who the caller is, so the dashboard can tell a real workspace from the read-only demo. */
@RestController
public class MeController {
  public record Me(UUID tenantId, UserRole role, String email, boolean demo) {}

  private final TenantContext tenantContext;
  private final TenantAuthorizationService authorization;
  private final UserJpaRepository users;

  public MeController(TenantContext tenantContext, TenantAuthorizationService authorization, UserJpaRepository users) {
    this.tenantContext = tenantContext;
    this.authorization = authorization;
    this.users = users;
  }

  @GetMapping("/api/v1/me")
  public Me me() {
    UUID tenantId = tenantContext.tenantId();
    String email = users.findByIdAndTenantId(tenantContext.actorId(), tenantId).map(UserEntity::email).orElse("");
    return new Me(tenantId, authorization.currentRole(), email, DemoProperties.DEMO_TENANT_ID.equals(tenantId));
  }
}
