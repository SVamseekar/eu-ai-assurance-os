package os.assurance.eu.api.eval;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvalRunJpaRepository extends JpaRepository<EvalRunEntity, UUID> {
  Optional<EvalRunEntity> findByTenantIdAndId(UUID tenantId, UUID id);

  Optional<EvalRunEntity> findFirstByStatusAndQueuedAtLessThanEqualOrderByQueuedAtAsc(
      String status,
      Instant queuedAt);
}
