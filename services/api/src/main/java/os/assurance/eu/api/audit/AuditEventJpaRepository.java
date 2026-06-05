package os.assurance.eu.api.audit;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventJpaRepository extends JpaRepository<AuditEventEntity, UUID> {
  List<AuditEventEntity> findAllByTenantIdOrderByCreatedAtDesc(UUID tenantId);

  List<AuditEventEntity> findAllByTenantIdAndSystemIdOrderByCreatedAtDesc(UUID tenantId, UUID systemId);
}
