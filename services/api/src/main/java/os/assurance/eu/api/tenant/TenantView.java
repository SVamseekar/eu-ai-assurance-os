package os.assurance.eu.api.tenant;

import java.time.Instant;
import java.util.UUID;

public record TenantView(
    UUID id,
    String name,
    String plan,
    String dataRegion,
    Instant createdAt) {

  public static TenantView from(TenantEntity tenant) {
    return new TenantView(
        tenant.id(),
        tenant.name(),
        tenant.plan(),
        tenant.dataRegion(),
        tenant.createdAt());
  }
}
