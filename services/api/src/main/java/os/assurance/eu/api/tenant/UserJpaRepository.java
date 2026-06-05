package os.assurance.eu.api.tenant;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {
  boolean existsByIdAndTenantId(UUID id, UUID tenantId);
}
