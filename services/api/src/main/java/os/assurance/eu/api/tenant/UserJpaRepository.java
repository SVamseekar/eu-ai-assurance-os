package os.assurance.eu.api.tenant;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {
  boolean existsByIdAndTenantId(UUID id, UUID tenantId);

  Optional<UserEntity> findByIdAndTenantId(UUID id, UUID tenantId);

  Optional<UserEntity> findFirstByTenantIdAndRoleOrderByCreatedAtAsc(UUID tenantId, UserRole role);

  Optional<UserEntity> findByEmail(String email);

  Optional<UserEntity> findByOauthProviderAndOauthSubject(String oauthProvider, String oauthSubject);

  List<UserEntity> findAllByTenantIdOrderByCreatedAtAsc(UUID tenantId);

  @Query("select u from UserEntity u where lower(u.email) = lower(:email)")
  Optional<UserEntity> findByEmailIgnoreCase(@Param("email") String email);

  @Query("select count(u) > 0 from UserEntity u where lower(u.email) = lower(:email)")
  boolean existsByEmailIgnoreCase(@Param("email") String email);
}
