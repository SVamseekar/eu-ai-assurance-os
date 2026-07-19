package os.assurance.eu.api.tenant;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {
  boolean existsByIdAndTenantId(UUID id, UUID tenantId);

  Optional<UserEntity> findByIdAndTenantId(UUID id, UUID tenantId);

  Optional<UserEntity> findFirstByTenantIdAndRoleOrderByCreatedAtAsc(UUID tenantId, UserRole role);

  Optional<UserEntity> findByEmail(String email);
}
