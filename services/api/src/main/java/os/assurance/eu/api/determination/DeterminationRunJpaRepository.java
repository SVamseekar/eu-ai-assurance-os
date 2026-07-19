package os.assurance.eu.api.determination;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeterminationRunJpaRepository extends JpaRepository<DeterminationRunEntity, UUID> {
  Optional<DeterminationRunEntity> findByIdAndTenantId(UUID id, UUID tenantId);

  List<DeterminationRunEntity> findAllByTenantIdAndSystemIdOrderByCreatedAtDesc(
      UUID tenantId, UUID systemId);

  Optional<DeterminationRunEntity> findFirstByTenantIdAndSystemIdOrderByCreatedAtDesc(
      UUID tenantId, UUID systemId);
}
