package os.assurance.eu.api.tenant;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ApiKeyJpaRepository extends JpaRepository<ApiKeyEntity, UUID> {
    Optional<ApiKeyEntity> findByKeyHash(String keyHash);

    List<ApiKeyEntity> findAllByTenantIdAndRevokedAtIsNullOrderByCreatedAtDesc(UUID tenantId);

    Optional<ApiKeyEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    @Modifying
    @Query("update ApiKeyEntity k set k.lastUsedAt = :at where k.id = :id")
    int touch(@Param("id") UUID id, @Param("at") Instant at);
}
