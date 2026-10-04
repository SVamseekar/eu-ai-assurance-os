package os.assurance.eu.api.billing;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/** Maps purchasable (plan, interval) pairs to Dodo product ids and back. */
@Component
public class BillingProducts {
  public record PlanAndInterval(PlanCatalog plan, String interval) {}

  private final DodoProperties props;

  public BillingProducts(DodoProperties props) {
    this.props = props;
  }

  public String productId(String plan, String interval) {
    PlanCatalog catalog;
    try {
      catalog = PlanCatalog.valueOf(plan.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown plan");
    }
    String id = switch (catalog) {
      case TEAM -> "MONTHLY".equalsIgnoreCase(interval) ? props.getProductTeamMonthly()
          : "YEARLY".equalsIgnoreCase(interval) ? props.getProductTeamYearly() : null;
      case BUSINESS -> "MONTHLY".equalsIgnoreCase(interval) ? props.getProductBusinessMonthly()
          : "YEARLY".equalsIgnoreCase(interval) ? props.getProductBusinessYearly() : null;
      default -> null;
    };
    if (id == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "That plan and interval cannot be purchased online");
    }
    if (id.isBlank()) {
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Billing is not configured");
    }
    return id;
  }

  /** Returns null for a product that is not one of ours, so unrelated webhooks change nothing. */
  public PlanAndInterval lookup(String productId) {
    if (productId == null || productId.isBlank()) {
      return null;
    }
    if (productId.equals(props.getProductTeamMonthly())) return new PlanAndInterval(PlanCatalog.TEAM, "MONTHLY");
    if (productId.equals(props.getProductTeamYearly())) return new PlanAndInterval(PlanCatalog.TEAM, "YEARLY");
    if (productId.equals(props.getProductBusinessMonthly())) return new PlanAndInterval(PlanCatalog.BUSINESS, "MONTHLY");
    if (productId.equals(props.getProductBusinessYearly())) return new PlanAndInterval(PlanCatalog.BUSINESS, "YEARLY");
    return null;
  }
}
