package os.assurance.eu.api.evidence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvidenceChunkJpaRepository extends JpaRepository<EvidenceChunkEntity, UUID> {
  List<EvidenceChunkEntity> findAllByDocumentIdInOrderByDocumentIdAscOrdinalAsc(List<UUID> documentIds);
}
