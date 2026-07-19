package os.assurance.eu.api.audit;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditEventJpaRepository extends JpaRepository<AuditEventEntity, UUID> {
  List<AuditEventEntity> findAllByTenantIdOrderByCreatedAtDesc(UUID tenantId);

  List<AuditEventEntity> findAllByTenantIdAndSystemIdOrderByCreatedAtDesc(UUID tenantId, UUID systemId);

  List<AuditEventEntity> findAllByTenantIdOrderByCreatedAtAsc(UUID tenantId);

  @Query(value = """
      select * from audit_events
      where tenant_id = :tenantId
      order by created_at desc, id desc
      limit 1
      """, nativeQuery = true)
  Optional<AuditEventEntity> findLatestByTenantId(@Param("tenantId") UUID tenantId);
}
