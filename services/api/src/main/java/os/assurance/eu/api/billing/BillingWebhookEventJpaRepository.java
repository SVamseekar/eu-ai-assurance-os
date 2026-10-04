package os.assurance.eu.api.billing;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingWebhookEventJpaRepository extends JpaRepository<BillingWebhookEventEntity, String> {
}
