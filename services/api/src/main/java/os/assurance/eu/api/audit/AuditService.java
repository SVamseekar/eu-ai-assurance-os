package os.assurance.eu.api.audit;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import os.assurance.eu.api.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {
  private final AuditEventJpaRepository repository;
  private final TenantContext tenantContext;

  public AuditService(AuditEventJpaRepository repository, TenantContext tenantContext) {
    this.repository = repository;
    this.tenantContext = tenantContext;
  }

  @Transactional
  public AuditEvent append(
      UUID systemId,
      String eventType,
      String resourceType,
      String resourceId,
      Map<String, Object> payload) {
    AuditEventEntity event = new AuditEventEntity(
        UUID.randomUUID(),
        tenantContext.tenantId(),
        systemId,
        tenantContext.actorId(),
        eventType,
        resourceType,
        resourceId,
        payload,
        Instant.now());
    return repository.save(event).toDomain();
  }

  @Transactional(readOnly = true)
  public List<AuditEvent> findAll() {
    return repository.findAllByTenantIdOrderByCreatedAtDesc(tenantContext.tenantId()).stream()
        .map(AuditEventEntity::toDomain)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<AuditEvent> findBySystemId(UUID systemId) {
    return repository.findAllByTenantIdAndSystemIdOrderByCreatedAtDesc(tenantContext.tenantId(), systemId).stream()
        .map(AuditEventEntity::toDomain)
        .toList();
  }
}
