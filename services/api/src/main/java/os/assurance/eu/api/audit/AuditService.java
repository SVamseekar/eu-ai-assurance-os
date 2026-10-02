package os.assurance.eu.api.audit;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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
  private final AuditChainHeadJpaRepository heads;
  private final AuditChainHeads headInit;
  private final TenantContext tenantContext;
  private final AuditChainHasher chainHasher;
  private final AssuranceMetrics assuranceMetrics;
  private final int retentionYears;

  public AuditService(
      AuditEventJpaRepository repository,
      AuditChainHeadJpaRepository heads,
      AuditChainHeads headInit,
      TenantContext tenantContext,
      AuditChainHasher chainHasher,
      AssuranceMetrics assuranceMetrics,
      @Value("${assurance.audit.retention-years:7}") int retentionYears) {
    this.repository = repository;
    this.heads = heads;
    this.headInit = headInit;
    this.tenantContext = tenantContext;
    this.chainHasher = chainHasher;
    this.assuranceMetrics = assuranceMetrics;
    this.retentionYears = Math.max(1, retentionYears);
  }

  @Transactional
  public AuditEvent append(
      UUID systemId, String eventType, String resourceType, String resourceId, Map<String, Object> payload) {
    return append(systemId, eventType, resourceType, resourceId, payload, "system");
  }

  @Transactional
  public AuditEvent append(
      UUID systemId, String eventType, String resourceType, String resourceId,
      Map<String, Object> payload, String source) {
    UUID tenantId = tenantContext.tenantId();
    headInit.ensureExists(tenantId);
    AuditChainHeadEntity head = heads.findForUpdate(tenantId)
        .orElseThrow(() -> new IllegalStateException("Audit chain head missing for tenant " + tenantId));
    UUID id = UUID.randomUUID();
    // Truncate to millis so hash input matches JDBC/H2 timestamp round-trip precision.
    Instant createdAt = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    Instant retainUntil = createdAt.atZone(ZoneOffset.UTC).plusYears(retentionYears).toInstant();
    String prevHash = head.headHash() != null
        ? head.headHash()
        : repository.findLatestByTenantId(tenantId).map(AuditEventEntity::eventHash).orElse(null);
    String eventHash = chainHasher.hash(
        tenantId, id, prevHash, tenantContext.actorId(), eventType, resourceType, resourceId, payload, createdAt);
    AuditEventEntity event = new AuditEventEntity(
        id, tenantId, systemId, tenantContext.actorId(), eventType, resourceType, resourceId, payload,
        createdAt, prevHash, eventHash, retainUntil);
    event.markSource("manual".equals(source) ? "manual" : "system");
    AuditEvent saved = repository.save(event).toDomain();
    head.advance(eventHash, createdAt);
    heads.save(head);
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
    List<AuditEventEntity> chained = repository.findAllByTenantIdOrderByCreatedAtAsc(tenantContext.tenantId())
        .stream()
        .filter(e -> e.eventHash() != null && !e.eventHash().isBlank())
        .toList();
    if (chained.isEmpty()) {
      return new AuditChainVerifyResponse(true, 0, null);
    }
    Map<String, List<AuditEventEntity>> byPrev = new java.util.HashMap<>();
    for (AuditEventEntity e : chained) {
      byPrev.computeIfAbsent(e.prevEventHash() == null ? "" : e.prevEventHash(), k -> new ArrayList<>()).add(e);
    }
    for (List<AuditEventEntity> siblings : byPrev.values()) {
      if (siblings.size() > 1) {
        return new AuditChainVerifyResponse(false, 0, siblings.get(1).id());
      }
    }
    // genesis: the event whose predecessor is not itself a chained event
    java.util.Set<String> hashes = new java.util.HashSet<>();
    chained.forEach(e -> hashes.add(e.eventHash()));
    AuditEventEntity current = chained.stream()
        .filter(e -> e.prevEventHash() == null || !hashes.contains(e.prevEventHash()))
        .findFirst()
        .orElse(chained.get(0));
    int checked = 0;
    java.util.Set<UUID> visited = new java.util.HashSet<>();
    while (current != null && visited.add(current.id())) {
      boolean ok = chainHasher.matches(
          current.tenantId(), current.id(), current.prevEventHash(), current.actorId(), current.eventType(),
          current.resourceType(), current.resourceId(), current.payload(), current.createdAt(), current.eventHash());
      checked++;
      if (!ok) {
        return new AuditChainVerifyResponse(false, checked, current.id());
      }
      List<AuditEventEntity> next = byPrev.get(current.eventHash());
      current = next == null ? null : next.get(0);
    }
    if (checked != chained.size()) {
      UUID orphan = chained.stream().filter(e -> !visited.contains(e.id())).findFirst().map(AuditEventEntity::id).orElse(null);
      return new AuditChainVerifyResponse(false, checked, orphan);
    }
    return new AuditChainVerifyResponse(true, checked, null);
  }
}
