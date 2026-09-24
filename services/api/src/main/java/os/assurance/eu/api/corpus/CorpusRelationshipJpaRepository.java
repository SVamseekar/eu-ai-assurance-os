package os.assurance.eu.api.corpus;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CorpusRelationshipJpaRepository extends JpaRepository<CorpusRelationshipEntity, UUID> {
  List<CorpusRelationshipEntity> findAllByCorpusVersionId(UUID corpusVersionId);
}
