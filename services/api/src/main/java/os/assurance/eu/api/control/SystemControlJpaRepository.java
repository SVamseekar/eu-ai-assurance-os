package os.assurance.eu.api.control;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemControlJpaRepository extends JpaRepository<SystemControlEntity, UUID> {
  List<SystemControlEntity> findAllByTenantIdAndSystemIdOrderByUpdatedAtDesc(
      UUID tenantId, UUID systemId);

  Optional<SystemControlEntity> findByTenantIdAndSystemIdAndControlId(
      UUID tenantId, UUID systemId, UUID controlId);

  List<SystemControlEntity> findAllByTenantIdAndSystemIdAndStatus(
      UUID tenantId, UUID systemId, ControlStatus status);
}
