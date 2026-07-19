package os.assurance.eu.api.control;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ControlJpaRepository extends JpaRepository<ControlEntity, UUID> {
  Optional<ControlEntity> findByCode(String code);

  boolean existsByCode(String code);
}
