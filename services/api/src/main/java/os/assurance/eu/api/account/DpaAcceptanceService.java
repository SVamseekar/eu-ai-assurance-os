package os.assurance.eu.api.account;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import os.assurance.eu.api.audit.AuditService;
import os.assurance.eu.api.tenant.TenantEntity;
import os.assurance.eu.api.tenant.TenantJpaRepository;

/** Records a workspace admin's click-through acceptance of the published DPA Cover Page version. */
@Service
public class DpaAcceptanceService {
  static final int MAX_VERSION_LENGTH = 16;

  private final TenantJpaRepository tenants;
  private final AuditService audit;
  private final Clock clock;

  public DpaAcceptanceService(TenantJpaRepository tenants, AuditService audit, Clock clock) {
    this.tenants = tenants;
    this.audit = audit;
    this.clock = clock;
  }

  public record DpaAcceptance(String version, Instant acceptedAt) {}

  @Transactional(readOnly = true)
  public DpaAcceptance current(UUID tenantId) {
    TenantEntity tenant = tenant(tenantId);
    return new DpaAcceptance(tenant.dpaVersion(), tenant.dpaAcceptedAt());
  }

  @Transactional
  public DpaAcceptance accept(UUID tenantId, String version) {
    if (version == null || version.isBlank() || version.length() > MAX_VERSION_LENGTH
        || !version.matches("[A-Za-z0-9.\\-]+")) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A DPA version is required");
    }
    TenantEntity tenant = tenant(tenantId);
    Instant now = clock.instant();
    tenant.acceptDpa(version, now);
    tenants.save(tenant);
    audit.append(null, "account.dpa_accepted", "tenant", tenantId.toString(), Map.of("version", version));
    return new DpaAcceptance(version, now);
  }

  private TenantEntity tenant(UUID tenantId) {
    return tenants.findById(tenantId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workspace not found"));
  }
}
