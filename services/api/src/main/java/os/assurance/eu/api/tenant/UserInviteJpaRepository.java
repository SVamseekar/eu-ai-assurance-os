package os.assurance.eu.api.tenant;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserInviteJpaRepository extends JpaRepository<UserInviteEntity, UUID> {
  Optional<UserInviteEntity> findByTokenHash(String tokenHash);

  List<UserInviteEntity> findAllByTenantIdOrderByCreatedAtDesc(UUID tenantId);

  Optional<UserInviteEntity> findFirstByTenantIdAndEmailIgnoreCaseAndAcceptedAtIsNullOrderByCreatedAtDesc(
      UUID tenantId, String email);
}
