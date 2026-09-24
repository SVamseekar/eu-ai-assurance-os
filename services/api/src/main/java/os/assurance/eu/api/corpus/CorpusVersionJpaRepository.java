package os.assurance.eu.api.corpus;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CorpusVersionJpaRepository extends JpaRepository<CorpusVersionEntity, UUID> {
  Optional<CorpusVersionEntity> findFirstByOrderByBuiltAtDesc();

  Optional<CorpusVersionEntity> findByVersionHash(String versionHash);
}
