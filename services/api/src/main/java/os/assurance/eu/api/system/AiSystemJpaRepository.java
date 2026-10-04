package os.assurance.eu.api.system;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AiSystemJpaRepository extends JpaRepository<AiSystemEntity, UUID> {
  List<AiSystemEntity> findAllByTenantIdOrderByCreatedAtAsc(UUID tenantId);

  Optional<AiSystemEntity> findByTenantIdAndId(UUID tenantId, UUID id);

  Optional<AiSystemEntity> findByTenantIdAndModelName(UUID tenantId, String modelName);

  boolean existsByTenantId(UUID tenantId);

  long countByTenantId(UUID tenantId);

  @Query("select s.id from AiSystemEntity s where s.tenantId = :tenantId order by s.createdAt asc")
  List<UUID> findIdsByTenantIdOrderByCreatedAtAsc(@Param("tenantId") UUID tenantId);
}
