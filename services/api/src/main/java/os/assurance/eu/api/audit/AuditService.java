package os.assurance.eu.api.audit;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import os.assurance.eu.api.observability.AssuranceMetrics;
import os.assurance.eu.api.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {
  private final AuditEventJpaRepository repository;
  private final TenantContext tenantContext;
  private final AuditChainHasher chainHasher;
  private final AssuranceMetrics assuranceMetrics;
  private final int retentionYears;

  public AuditService(
      AuditEventJpaRepository repository,
      TenantContext tenantContext,
      AuditChainHasher chainHasher,
      AssuranceMetrics assuranceMetrics,
      @Value("${assurance.audit.retention-years:7}") int retentionYears) {
    this.repository = repository;
    this.tenantContext = tenantContext;
    this.chainHasher = chainHasher;
    this.assuranceMetrics = assuranceMetrics;
    this.retentionYears = Math.max(1, retentionYears);
  }

  @Transactional
  public AuditEvent append(
      UUID systemId,
      String eventType,
      String resourceType,
      String resourceId,
      Map<String, Object> payload) {
    UUID tenantId = tenantContext.tenantId();
    UUID id = UUID.randomUUID();
    Instant createdAt = Instant.now();
    Instant retainUntil = createdAt.atZone(ZoneOffset.UTC).plusYears(retentionYears).toInstant();
    String prevHash = repository.findLatestByTenantId(tenantId)
        .map(AuditEventEntity::eventHash)
        .orElse(null);
    String eventHash = chainHasher.hash(
        tenantId,
        id,
        prevHash,
        tenantContext.actorId(),
        eventType,
        resourceType,
        resourceId,
        payload,
        createdAt);
    AuditEventEntity event = new AuditEventEntity(
        id,
        tenantId,
        systemId,
        tenantContext.actorId(),
        eventType,
        resourceType,
        resourceId,
        payload,
        createdAt,
        prevHash,
        eventHash,
        retainUntil);
    AuditEvent saved = repository.save(event).toDomain();
    assuranceMetrics.auditAppend();
    return saved;
  }

  @Transactional(readOnly = true)
  public List<AuditEvent> findAll() {
    return repository.findAllByTenantIdOrderByCreatedAtDesc(tenantContext.tenantId()).stream()
        .map(AuditEventEntity::toDomain)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<AuditEvent> findBySystemId(UUID systemId) {
    return repository.findAllByTenantIdAndSystemIdOrderByCreatedAtDesc(
            tenantContext.tenantId(), systemId).stream()
        .map(AuditEventEntity::toDomain)
        .toList();
  }

  /**
   * Tip of the tenant audit hash chain (latest eventHash), if any chained event exists.
   */
  @Transactional(readOnly = true)
  public java.util.Optional<String> chainHeadHash() {
    return repository.findLatestByTenantId(tenantContext.tenantId())
        .map(AuditEventEntity::eventHash)
        .filter(hash -> hash != null && !hash.isBlank());
  }

  @Transactional(readOnly = true)
  public AuditChainVerifyResponse verifyChain() {
    List<AuditEventEntity> events =
        repository.findAllByTenantIdOrderByCreatedAtAsc(tenantContext.tenantId());
    String expectedPrev = null;
    int checked = 0;
    for (AuditEventEntity event : events) {
      if (event.eventHash() == null || event.eventHash().isBlank()) {
        // Pre-chain rows are skipped for verification count but break continuity
        expectedPrev = event.eventHash();
        continue;
      }
      boolean ok = chainHasher.matches(
          event.tenantId(),
          event.id(),
          event.prevEventHash(),
          event.actorId(),
          event.eventType(),
          event.resourceType(),
          event.resourceId(),
          event.payload(),
          event.createdAt(),
          event.eventHash());
      boolean prevLinks = (expectedPrev == null && event.prevEventHash() == null)
          || (expectedPrev != null && expectedPrev.equals(event.prevEventHash()));
      checked++;
      if (!ok || !prevLinks) {
        return new AuditChainVerifyResponse(false, checked, event.id());
      }
      expectedPrev = event.eventHash();
    }
    return new AuditChainVerifyResponse(true, checked, null);
  }
}
