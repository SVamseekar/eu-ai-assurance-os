package os.assurance.eu.api.assessment;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvidenceExceptionJpaRepository extends JpaRepository<EvidenceExceptionEntity, UUID> {
  List<EvidenceExceptionEntity> findAllByTenantIdAndSystemIdAndProposalIdOrderByCreatedAtDesc(
      UUID tenantId, UUID systemId, UUID proposalId);
}
