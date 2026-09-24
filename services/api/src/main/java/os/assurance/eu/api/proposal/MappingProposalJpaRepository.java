package os.assurance.eu.api.proposal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MappingProposalJpaRepository extends JpaRepository<MappingProposalEntity, UUID> {
  List<MappingProposalEntity> findAllByTenantIdAndSystemIdOrderByCreatedAtAsc(UUID tenantId, UUID systemId);

  Optional<MappingProposalEntity> findByIdAndTenantIdAndSystemId(UUID id, UUID tenantId, UUID systemId);
}
