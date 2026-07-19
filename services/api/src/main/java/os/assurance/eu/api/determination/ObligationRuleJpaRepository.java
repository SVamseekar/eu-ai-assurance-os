package os.assurance.eu.api.determination;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ObligationRuleJpaRepository extends JpaRepository<ObligationRuleEntity, UUID> {
  List<ObligationRuleEntity> findAllByRulesetVersionAndActiveTrueOrderByCodeAsc(String rulesetVersion);
}
