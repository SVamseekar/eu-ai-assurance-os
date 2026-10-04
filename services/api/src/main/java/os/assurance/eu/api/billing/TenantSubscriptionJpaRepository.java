package os.assurance.eu.api.billing;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantSubscriptionJpaRepository extends JpaRepository<TenantSubscriptionEntity, UUID> {
  Optional<TenantSubscriptionEntity> findByDodoSubscriptionId(String dodoSubscriptionId);

}
