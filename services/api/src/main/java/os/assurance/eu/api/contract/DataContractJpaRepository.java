package os.assurance.eu.api.contract;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataContractJpaRepository extends JpaRepository<DataContractEntity, UUID> {
  List<DataContractEntity> findAllByTenantIdOrderByCreatedAtAsc(UUID tenantId);

  List<DataContractEntity> findAllByTenantIdAndSystemIdOrderByCreatedAtAsc(UUID tenantId, UUID systemId);

  Optional<DataContractEntity> findByTenantIdAndId(UUID tenantId, UUID id);

  boolean existsByTenantIdAndSystemIdAndNameAndVersion(UUID tenantId, UUID systemId, String name, String version);
}
