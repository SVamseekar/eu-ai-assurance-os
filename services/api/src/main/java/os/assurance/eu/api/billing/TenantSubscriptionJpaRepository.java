package os.assurance.eu.api.billing;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantSubscriptionJpaRepository extends JpaRepository<TenantSubscriptionEntity, UUID> {
}
