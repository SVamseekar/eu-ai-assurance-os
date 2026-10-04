package os.assurance.eu.api.account;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;
import os.assurance.eu.api.audit.AuditService;
import os.assurance.eu.api.auth.RefreshTokenService;
import os.assurance.eu.api.email.EmailSender;
import os.assurance.eu.api.email.EmailTemplates;
import os.assurance.eu.api.tenant.ApiKeyJpaRepository;
import os.assurance.eu.api.tenant.TenantContext;
import os.assurance.eu.api.tenant.TenantEntity;
import os.assurance.eu.api.tenant.TenantJpaRepository;
import os.assurance.eu.api.tenant.TenantStatusCache;
import os.assurance.eu.api.tenant.UserJpaRepository;
import os.assurance.eu.api.tenant.UserRole;

@Service
public class WorkspaceDeletionService {
  static final Duration RETENTION = Duration.ofDays(30);

  private final TenantJpaRepository tenants;
  private final UserJpaRepository users;
  private final ApiKeyJpaRepository apiKeys;
  private final RefreshTokenService refreshTokens;
  private final TenantStatusCache tenantStatus;
  private final TenantContext tenantContext;
  private final AuditService audit;
  private final EmailSender email;
  private final Clock clock;
  private final os.assurance.eu.api.billing.EntitlementService entitlements;

  public WorkspaceDeletionService(TenantJpaRepository tenants, UserJpaRepository users, ApiKeyJpaRepository apiKeys,
      RefreshTokenService refreshTokens, TenantStatusCache tenantStatus, TenantContext tenantContext,
      AuditService audit, EmailSender email, Clock clock,
      os.assurance.eu.api.billing.EntitlementService entitlements) {
    this.tenants = tenants;
    this.users = users;
    this.apiKeys = apiKeys;
    this.refreshTokens = refreshTokens;
    this.tenantStatus = tenantStatus;
    this.tenantContext = tenantContext;
    this.audit = audit;
    this.email = email;
    this.clock = clock;
    this.entitlements = entitlements;
  }

  @Transactional
  public void delete(String confirmOrganisationName) {
    UUID tenantId = tenantContext.tenantId();
    TenantEntity tenant = tenants.findById(tenantId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workspace not found"));
    if (confirmOrganisationName == null || !confirmOrganisationName.equals(tenant.name())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Type the exact organisation name to confirm deletion");
    }
    if (entitlements.hasBillingSubscription(tenantId)) {
      throw new os.assurance.eu.api.billing.BillingConflictException(
          "Cancel your subscription in Plan and billing first, so you are not charged after deletion.");
    }
    Instant now = clock.instant();
    Instant purgeOn = now.plus(RETENTION);
    tenant.scheduleDeletion(now, purgeOn);
    tenants.save(tenant);
    refreshTokens.revokeAllForTenant(tenantId);
    for (var key : apiKeys.findAllByTenantIdAndRevokedAtIsNullOrderByCreatedAtDesc(tenantId)) {
      key.revoke(now);
      apiKeys.save(key);
    }
    audit.append(null, "account.deletion_scheduled", "tenant", tenantId.toString(),
        Map.of("purgeAfter", purgeOn.toString()));
    LocalDate purgeDate = purgeOn.atZone(ZoneOffset.UTC).toLocalDate();
    users.findAllByTenantIdOrderByCreatedAtAsc(tenantId).stream()
        .filter(u -> u.role() == UserRole.ADMIN)
        .forEach(admin -> email.send(
            EmailTemplates.workspaceDeletionScheduled(tenant.name(), purgeDate).withTo(admin.email())));
    // Evict once the new status is committed, so no request can re-cache the old "active" answer.
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override
      public void afterCommit() {
        tenantStatus.evict(tenantId);
      }
    });
  }
}
