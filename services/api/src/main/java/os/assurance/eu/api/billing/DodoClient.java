package os.assurance.eu.api.billing;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Thin REST client for Dodo Payments (there is no Java SDK). No card data ever passes through here. */
@Component
public class DodoClient {
  public record CheckoutSession(String sessionId, String checkoutUrl) {}

  private final RestClient http;
  private final DodoProperties props;

  public DodoClient(DodoProperties props) {
    this.props = props;
    var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(5_000);
    factory.setReadTimeout(10_000);
    this.http = RestClient.builder()
        .requestFactory(factory)
        .baseUrl(props.getBaseUrl())
        .defaultHeader("Authorization", "Bearer " + props.getApiKey())
        .build();
  }

  public CheckoutSession createCheckout(String productId, String email, String name, UUID tenantId, String returnUrl) {
    JsonNode body = http.post().uri("/checkouts")
        .contentType(MediaType.APPLICATION_JSON)
        .body(Map.of(
            "product_cart", List.of(Map.of("product_id", productId, "quantity", 1)),
            "customer", Map.of("email", email, "name", name),
            "return_url", returnUrl,
            "metadata", Map.of("tenant_id", tenantId.toString())))
        .retrieve()
        .body(JsonNode.class);
    return new CheckoutSession(body.path("session_id").asText(), body.path("checkout_url").asText());
  }

  /** Customer portal link. Falls back to the static portal login when the session endpoint is unavailable. */
  public String createPortalSession(String customerId) {
    try {
      JsonNode body = http.post().uri("/customers/{id}/customer-portal/session", customerId)
          .retrieve()
          .body(JsonNode.class);
      String link = body.path("link").asText(body.path("url").asText(""));
      if (!link.isBlank()) {
        return link;
      }
    } catch (RuntimeException e) {
      if (props.getBusinessId().isBlank()) {
        throw e;
      }
    }
    return "https://customer.dodopayments.com/login/" + props.getBusinessId();
  }
}
