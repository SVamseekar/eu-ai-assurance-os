package os.assurance.eu.api.billing;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** One row per webhook id already processed, so a redelivery changes nothing. Payloads are hashed, not stored. */
@Entity
@Table(name = "billing_webhook_events")
public class BillingWebhookEventEntity {
  @Id
  private String webhookId;

  @Column(nullable = false)
  private String eventType;

  @Column(name = "payload_sha256", nullable = false)
  private String payloadSha256;

  @Column(nullable = false)
  private Instant receivedAt;

  protected BillingWebhookEventEntity() {
  }

  public BillingWebhookEventEntity(String webhookId, String eventType, String payloadSha256, Instant receivedAt) {
    this.webhookId = webhookId;
    this.eventType = eventType;
    this.payloadSha256 = payloadSha256;
    this.receivedAt = receivedAt;
  }
}
