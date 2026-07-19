package os.assurance.eu.api.determination;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeterminationObligationJpaRepository
    extends JpaRepository<DeterminationObligationEntity, UUID> {
  List<DeterminationObligationEntity> findAllByRunIdOrderByRuleCodeAsc(UUID runId);
}
