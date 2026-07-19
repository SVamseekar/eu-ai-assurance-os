package os.assurance.eu.api.regmonitor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegItemReviewJpaRepository extends JpaRepository<RegItemReviewEntity, UUID> {
  Optional<RegItemReviewEntity> findByTenantIdAndRegItemId(UUID tenantId, UUID regItemId);

  List<RegItemReviewEntity> findAllByTenantIdAndRegItemIdIn(UUID tenantId, Collection<UUID> regItemIds);
}
