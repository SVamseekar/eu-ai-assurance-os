package os.assurance.eu.api.corpus;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstrumentJpaRepository extends JpaRepository<InstrumentEntity, UUID> {
  List<InstrumentEntity> findAllByCorpusVersionIdOrderBySeedCelexAsc(UUID corpusVersionId);
}
