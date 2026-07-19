package os.assurance.eu.api.regmonitor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegSourceJpaRepository extends JpaRepository<RegSourceEntity, UUID> {
  List<RegSourceEntity> findAllByEnabledTrueOrderByCreatedAtAsc();

  Optional<RegSourceEntity> findByCode(String code);
}
