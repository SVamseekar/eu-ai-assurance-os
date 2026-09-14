package os.assurance.eu.api.system;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiSystemJpaRepository extends JpaRepository<AiSystemEntity, UUID> {
  List<AiSystemEntity> findAllByTenantIdOrderByCreatedAtAsc(UUID tenantId);

  Optional<AiSystemEntity> findByTenantIdAndId(UUID tenantId, UUID id);

  Optional<AiSystemEntity> findByTenantIdAndModelName(UUID tenantId, String modelName);

  boolean existsByTenantId(UUID tenantId);
}
