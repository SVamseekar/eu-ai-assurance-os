package os.assurance.eu.api.eval;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvalDatasetJpaRepository extends JpaRepository<EvalDatasetEntity, UUID> {
  List<EvalDatasetEntity> findAllByTenantIdOrderByCreatedAtAsc(UUID tenantId);

  Optional<EvalDatasetEntity> findByTenantIdAndId(UUID tenantId, UUID id);

  Optional<EvalDatasetEntity> findFirstByTenantIdAndNameOrderByCreatedAtDesc(UUID tenantId, String name);

  boolean existsByTenantIdAndNameAndVersion(UUID tenantId, String name, String version);
}
