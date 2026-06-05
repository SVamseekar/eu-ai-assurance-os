package os.assurance.eu.api.evidence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvidenceDocumentJpaRepository extends JpaRepository<EvidenceDocumentEntity, UUID> {
  List<EvidenceDocumentEntity> findAllByTenantIdAndSystemIdOrderByCreatedAtDesc(UUID tenantId, UUID systemId);

  List<EvidenceDocumentEntity> findAllByTenantIdAndSystemIdInOrderByCreatedAtDesc(UUID tenantId, List<UUID> systemIds);
}
