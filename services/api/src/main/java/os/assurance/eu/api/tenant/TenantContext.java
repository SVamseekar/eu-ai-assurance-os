package os.assurance.eu.api.tenant;

import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class TenantContext {
  public static final UUID DEFAULT_TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  public static final UUID DEFAULT_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
  public static final String TENANT_HEADER = "X-Tenant-Id";
  public static final String ACTOR_HEADER = "X-Actor-Id";
  private final ThreadLocal<UUID> tenantOverride = new ThreadLocal<>();

  public UUID tenantId() {
    UUID override = tenantOverride.get();
    if (override != null) {
      return override;
    }
    return headerUuid(TENANT_HEADER, DEFAULT_TENANT_ID);
  }

  public UUID actorId() {
    return headerUuid(ACTOR_HEADER, DEFAULT_USER_ID);
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

  private UUID headerUuid(String headerName, UUID fallback) {
    RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
    if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
      return fallback;
    }
    String value = servletAttributes.getRequest().getHeader(headerName);
    if (value == null || value.isBlank()) {
      return fallback;
    }
    return UUID.fromString(value);
  }
}
