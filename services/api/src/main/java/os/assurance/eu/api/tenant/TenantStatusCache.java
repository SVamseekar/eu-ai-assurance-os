package os.assurance.eu.api.tenant;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Remembers for 60 seconds that a workspace is active, so the auth filter does not hit the database on every
 * request. Only positive answers are cached: a deleted or unknown workspace is re-checked every time.
 */
@Component
public class TenantStatusCache {
  static final Duration TTL = Duration.ofSeconds(60);

  private final ConcurrentHashMap<UUID, Instant> activeUntil = new ConcurrentHashMap<>();
  private final TenantJpaRepository tenants;
  private final Clock clock;

  public TenantStatusCache(TenantJpaRepository tenants, Clock clock) {
    this.tenants = tenants;
    this.clock = clock;
  }

  public boolean isActive(UUID tenantId) {
    Instant now = clock.instant();
    Instant until = activeUntil.get(tenantId);
    if (until != null && until.isAfter(now)) {
      return true;
    }
    boolean active = tenants.findById(tenantId).map(TenantEntity::active).orElse(false);
    if (active) {
      activeUntil.put(tenantId, now.plus(TTL));
    } else {
      activeUntil.remove(tenantId);
    }
    return active;
  }

  public void evict(UUID tenantId) {
    activeUntil.remove(tenantId);
  }
}
