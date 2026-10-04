package os.assurance.eu.api.billing;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import os.assurance.eu.api.tenant.SessionOnly;
import os.assurance.eu.api.tenant.TenantAuthorizationService;
import os.assurance.eu.api.tenant.TenantContext;
import os.assurance.eu.api.tenant.TenantEntity;
import os.assurance.eu.api.tenant.TenantJpaRepository;
import os.assurance.eu.api.tenant.UserEntity;
import os.assurance.eu.api.tenant.UserJpaRepository;
import os.assurance.eu.api.tenant.UserRole;

@RestController
public class BillingController {
  public record CheckoutBody(@NotNull String plan, @NotNull String interval) {}

  private final EntitlementService entitlements;
  private final TenantSubscriptionJpaRepository subscriptions;
  private final TenantJpaRepository tenants;
  private final UserJpaRepository users;
  private final BillingProducts products;
  private final DodoClient dodo;
  private final DodoWebhookService webhooks;
  private final DodoProperties props;
  private final TenantAuthorizationService authorization;
  private final TenantContext tenantContext;
  private final Clock clock;

  public BillingController(EntitlementService entitlements, TenantSubscriptionJpaRepository subscriptions,
      TenantJpaRepository tenants, UserJpaRepository users, BillingProducts products, DodoClient dodo,
      DodoWebhookService webhooks, DodoProperties props, TenantAuthorizationService authorization,
      TenantContext tenantContext, Clock clock) {
    this.entitlements = entitlements;
    this.subscriptions = subscriptions;
    this.tenants = tenants;
    this.users = users;
    this.products = products;
    this.dodo = dodo;
    this.webhooks = webhooks;
    this.props = props;
    this.authorization = authorization;
    this.tenantContext = tenantContext;
    this.clock = clock;
  }

  @GetMapping("/api/v1/billing")
  public Map<String, Object> summary() {
    UUID tenantId = tenantContext.tenantId();
    PlanCatalog plan = entitlements.effectivePlan(tenantId);
    TenantEntity tenant = tenants.findById(tenantId).orElseThrow();
    var sub = subscriptions.findById(tenantId).orElse(null);
    boolean paying = sub != null && sub.planCode().equals(plan.name());
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("plan", plan.name());
    out.put("status", paying ? sub.status() : plan == PlanCatalog.TRIAL ? "TRIAL" : "NONE");
    out.put("interval", paying ? sub.billingInterval() : null);
    out.put("trialEndsAt", tenant.trialEndsAt());
    out.put("currentPeriodEnd", sub == null ? null : sub.currentPeriodEnd());
    out.put("graceUntil", paying ? sub.graceUntil() : null);
    var usage = entitlements.usage(tenantId);
    out.put("usage", Map.of("systems", usage.systems(), "editors", usage.editors(),
        "gateRunsThisMonth", usage.gateRunsThisMonth()));
    out.put("limits", Map.of("gatedSystems", plan.gatedSystems(), "editorSeats", plan.editorSeats(),
        "gateRunsPerMonth", plan.gateRunsPerMonth()));
    return out;
  }

  /** Starting a purchase needs a signed-in admin; a CI key must not be able to. */
  @PostMapping("/api/v1/billing/checkout")
  public Map<String, String> checkout(HttpServletRequest request, @Valid @RequestBody CheckoutBody body) {
    SessionOnly.require(request);
    authorization.requireAnyRole(UserRole.ADMIN);
    String productId = products.productId(body.plan(), body.interval());
    if (entitlements.hasBillingSubscription(tenantContext.tenantId())) {
      throw new BillingConflictException(
          "This workspace already has a subscription. Change or cancel it in the billing portal.");
    }
    UserEntity admin = users.findByIdAndTenantId(tenantContext.actorId(), tenantContext.tenantId()).orElseThrow();
    TenantEntity tenant = tenants.findById(tenantContext.tenantId()).orElseThrow();
    var session = dodo.createCheckout(productId, admin.email(), tenant.name(), tenant.id(), props.getReturnUrl());
    return Map.of("checkoutUrl", session.checkoutUrl());
  }

  @PostMapping("/api/v1/billing/portal")
  public Map<String, String> portal(HttpServletRequest request) {
    SessionOnly.require(request);
    authorization.requireAnyRole(UserRole.ADMIN);
    String customerId = subscriptions.findById(tenantContext.tenantId())
        .map(TenantSubscriptionEntity::dodoCustomerId).orElse(null);
    if (customerId == null || customerId.isBlank()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "No billing account yet. Upgrade first.");
    }
    return Map.of("url", dodo.createPortalSession(customerId));
  }

  /** Public: authenticated by the Standard Webhooks signature over the exact request bytes. */
  @PostMapping("/api/v1/billing/webhooks/dodo")
  public ResponseEntity<Void> webhook(
      @RequestHeader(name = "webhook-id", required = false) String id,
      @RequestHeader(name = "webhook-timestamp", required = false) String timestamp,
      @RequestHeader(name = "webhook-signature", required = false) String signature,
      @RequestBody byte[] body) throws Exception {
    if (props.getWebhookSecret() == null || props.getWebhookSecret().isBlank()) {
      throw new WebhookSignatureException("Webhook secret is not configured");
    }
    new StandardWebhookVerifier(props.getWebhookSecret(), Duration.ofMinutes(5), clock)
        .verify(id, timestamp, signature, body);
    webhooks.handle(id, body);
    return ResponseEntity.ok().build();
  }
}
