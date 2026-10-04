package os.assurance.eu.api.billing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import os.assurance.eu.api.audit.AuditService;
import os.assurance.eu.api.tenant.TenantContext;
import os.assurance.eu.api.tenant.TenantJpaRepository;
import os.assurance.eu.api.tenant.UserJpaRepository;
import os.assurance.eu.api.tenant.UserRole;

/**
 * Applies verified Dodo webhooks. Webhooks are the only way a paid plan becomes active. Redeliveries are no-ops
 * (keyed by webhook id), and events stamped earlier than the stored state are ignored, so out-of-order delivery
 * converges on the newest state.
 */
@Service
public class DodoWebhookService {
  static final Duration PAYMENT_GRACE = Duration.ofDays(7);

  private final BillingWebhookEventJpaRepository events;
  private final TenantSubscriptionJpaRepository subscriptions;
  private final TenantJpaRepository tenants;
  private final UserJpaRepository users;
  private final BillingProducts products;
  private final AuditService audit;
  private final TenantContext tenantContext;
  private final ObjectMapper objectMapper;
  private final Clock clock;

  public DodoWebhookService(BillingWebhookEventJpaRepository events, TenantSubscriptionJpaRepository subscriptions,
      TenantJpaRepository tenants, UserJpaRepository users, BillingProducts products, AuditService audit,
      TenantContext tenantContext, ObjectMapper objectMapper, Clock clock) {
    this.events = events;
    this.subscriptions = subscriptions;
    this.tenants = tenants;
    this.users = users;
    this.products = products;
    this.audit = audit;
    this.tenantContext = tenantContext;
    this.objectMapper = objectMapper;
    this.clock = clock;
  }

  @Transactional
  public void handle(String webhookId, byte[] rawBody) throws Exception {
    if (events.existsById(webhookId)) {
      return; // redelivery
    }
    JsonNode root = objectMapper.readTree(rawBody);
    String type = root.path("type").asText();
    Instant now = clock.instant();
    events.save(new BillingWebhookEventEntity(webhookId, type, sha256Hex(rawBody), now));
    JsonNode data = root.path("data");
    String tenantIdText = data.path("metadata").path("tenant_id").asText("");
    if (tenantIdText.isBlank() || !type.startsWith("subscription.")) {
      return; // payments, refunds and disputes are recorded by id only; nothing to change
    }
    UUID tenantId;
    try {
      tenantId = UUID.fromString(tenantIdText);
    } catch (IllegalArgumentException e) {
      return;
    }
    if (!tenants.existsById(tenantId)) {
      return; // acknowledge so Dodo stops retrying
    }
    BillingProducts.PlanAndInterval pi = products.lookup(data.path("product_id").asText());
    if (pi == null) {
      return;
    }
    Instant eventTime = parseInstant(root.path("timestamp").asText(null));
    if (eventTime == null) {
      eventTime = now;
    }
    TenantSubscriptionEntity sub = subscriptions.findById(tenantId)
        .orElseGet(() -> new TenantSubscriptionEntity(tenantId));
    if (eventTime.isBefore(sub.updatedAt())) {
      return; // older than what we already hold
    }
    Instant periodEnd = parseInstant(data.path("next_billing_date").asText(null));
    switch (type) {
      case "subscription.active", "subscription.renewed", "subscription.plan_changed", "subscription.unpaused" ->
          sub.activate(pi.plan().name(), pi.interval(), textOrNull(data.path("customer").path("customer_id")),
              textOrNull(data.path("subscription_id")), periodEnd, eventTime);
      case "subscription.on_hold", "subscription.past_due" -> sub.hold(eventTime.plus(PAYMENT_GRACE), eventTime);
      case "subscription.cancelled" -> sub.cancel(periodEnd != null ? periodEnd : eventTime, eventTime);
      case "subscription.expired" -> sub.cancel(eventTime, eventTime);
      default -> {
        return;
      }
    }
    subscriptions.save(sub);
    users.findFirstByTenantIdAndRoleOrderByCreatedAtAsc(tenantId, UserRole.ADMIN).ifPresent(admin -> {
      tenantContext.setOverrides(tenantId, admin.id());
      try {
        audit.append(null, "billing." + type, "subscription", String.valueOf(sub.dodoSubscriptionId()),
            Map.of("plan", sub.planCode(), "status", sub.status()));
      } finally {
        tenantContext.clearOverrides();
      }
    });
  }

  private static String textOrNull(JsonNode node) {
    return node.isMissingNode() || node.isNull() || node.asText().isBlank() ? null : node.asText();
  }

  private static Instant parseInstant(String text) {
    if (text == null || text.isBlank() || "null".equals(text)) {
      return null;
    }
    try {
      return Instant.parse(text);
    } catch (DateTimeParseException e) {
      try {
        return java.time.OffsetDateTime.parse(text).toInstant();
      } catch (DateTimeParseException e2) {
        return null;
      }
    }
  }

  private static String sha256Hex(byte[] data) throws Exception {
    return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data));
  }
}
