package os.assurance.eu.api.tenant;

import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

@Component
public class TenantContext {
  public static final UUID DEFAULT_TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  public static final UUID DEFAULT_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
  private final ThreadLocal<UUID> tenantOverride = new ThreadLocal<>();
  private final ThreadLocal<UUID> actorOverride = new ThreadLocal<>();

  public void setOverrides(UUID tenantId, UUID actorId) {
    tenantOverride.set(tenantId);
    actorOverride.set(actorId);
  }

  public void clearOverrides() {
    tenantOverride.remove();
    actorOverride.remove();
  }

  public UUID tenantId() {
    UUID override = tenantOverride.get();
    if (override == null) {
      throw new IllegalStateException("tenantId() called outside an authenticated request context");
    }
    return override;
  }

  public UUID actorId() {
    UUID override = actorOverride.get();
    if (override == null) {
      throw new IllegalStateException("actorId() called outside an authenticated request context");
    }
    return override;
  }

  public <T> T withTenant(UUID tenantId, Supplier<T> work) {
    UUID previous = tenantOverride.get();
    tenantOverride.set(tenantId);
    try {
      return work.get();
    } finally {
      if (previous == null) {
        tenantOverride.remove();
      } else {
        tenantOverride.set(previous);
      }
    }
  }
}