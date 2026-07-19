package os.assurance.eu.api.regmonitor;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegImpactHintJpaRepository extends JpaRepository<RegImpactHintEntity, UUID> {
  List<RegImpactHintEntity> findAllByRegItemId(UUID regItemId);

  List<RegImpactHintEntity> findAllByRegItemIdIn(Collection<UUID> regItemIds);

  List<RegImpactHintEntity> findAllByControlCodeIn(Collection<String> controlCodes);
}
