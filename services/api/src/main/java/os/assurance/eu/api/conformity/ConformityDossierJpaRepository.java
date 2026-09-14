package os.assurance.eu.api.conformity;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConformityDossierJpaRepository extends JpaRepository<ConformityDossierEntity, UUID> {
  Optional<ConformityDossierEntity> findByTenantIdAndSystemId(UUID tenantId, UUID systemId);
}
