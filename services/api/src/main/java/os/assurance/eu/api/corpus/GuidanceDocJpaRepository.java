package os.assurance.eu.api.corpus;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuidanceDocJpaRepository extends JpaRepository<GuidanceDocEntity, UUID> {
  List<GuidanceDocEntity> findAllByCorpusVersionIdOrderBySourceKeyAsc(UUID corpusVersionId);
}
