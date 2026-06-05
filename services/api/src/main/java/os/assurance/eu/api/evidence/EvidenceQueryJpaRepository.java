package os.assurance.eu.api.evidence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvidenceQueryJpaRepository extends JpaRepository<EvidenceQueryEntity, UUID> {
}
