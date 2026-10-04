package os.assurance.eu.api.billing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Dodo Payments settings. Every value comes from the environment; blanks mean billing is not configured. */
@Component
public class DodoProperties {
  private final String baseUrl;
  private final String apiKey;
  private final String webhookSecret;
  private final String productTeamMonthly;
  private final String productTeamYearly;
  private final String productBusinessMonthly;
  private final String productBusinessYearly;
  private final String returnUrl;
  private final String businessId;

  public DodoProperties(
      @Value("${assurance.dodo.base-url:https://test.dodopayments.com}") String baseUrl,
      @Value("${assurance.dodo.api-key:}") String apiKey,
      @Value("${assurance.dodo.webhook-secret:}") String webhookSecret,
      @Value("${assurance.dodo.product-team-monthly:}") String productTeamMonthly,
      @Value("${assurance.dodo.product-team-yearly:}") String productTeamYearly,
      @Value("${assurance.dodo.product-business-monthly:}") String productBusinessMonthly,
      @Value("${assurance.dodo.product-business-yearly:}") String productBusinessYearly,
      @Value("${assurance.dodo.return-url:http://localhost:3000/settings?billing=return}") String returnUrl,
      @Value("${assurance.dodo.business-id:}") String businessId) {
    this.baseUrl = baseUrl;
    this.apiKey = apiKey;
    this.webhookSecret = webhookSecret;
    this.productTeamMonthly = productTeamMonthly;
    this.productTeamYearly = productTeamYearly;
    this.productBusinessMonthly = productBusinessMonthly;
    this.productBusinessYearly = productBusinessYearly;
    this.returnUrl = returnUrl;
    this.businessId = businessId;
  }

  public String getBaseUrl() { return baseUrl; }
  public String getApiKey() { return apiKey; }
  public String getWebhookSecret() { return webhookSecret; }
  public String getProductTeamMonthly() { return productTeamMonthly; }
  public String getProductTeamYearly() { return productTeamYearly; }
  public String getProductBusinessMonthly() { return productBusinessMonthly; }
  public String getProductBusinessYearly() { return productBusinessYearly; }
  public String getReturnUrl() { return returnUrl; }
  public String getBusinessId() { return businessId; }
}
