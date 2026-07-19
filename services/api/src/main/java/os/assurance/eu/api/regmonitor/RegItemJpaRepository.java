package os.assurance.eu.api.regmonitor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegItemJpaRepository extends JpaRepository<RegItemEntity, UUID> {
  boolean existsByContentHash(String contentHash);

  boolean existsBySourceIdAndExternalId(UUID sourceId, String externalId);

  List<RegItemEntity> findAllByOrderByFetchedAtDesc();

  List<RegItemEntity> findAllByFetchedAtGreaterThanEqualOrderByFetchedAtDesc(Instant since);

  Optional<RegItemEntity> findById(UUID id);
}
