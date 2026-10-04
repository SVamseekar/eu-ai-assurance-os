package os.assurance.eu.api.billing;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import os.assurance.eu.api.email.EmailMessage;
import os.assurance.eu.api.email.EmailSender;
import os.assurance.eu.api.tenant.UserJpaRepository;

@SpringBootTest(properties = {
    "assurance.eval.worker.enabled=false",
    "assurance.eval.callback.secret=test-eval-callback-secret",
    "assurance.app.base-url=https://app.test",
    "assurance.security.auth-rate.per-ip-per-15m=1000",
    "assurance.auth.email.cooldown-seconds=0",
    "assurance.dodo.product-team-monthly=prod_team_m",
    "assurance.dodo.product-team-yearly=prod_team_y",
    "assurance.dodo.product-business-monthly=prod_bus_m",
    "assurance.dodo.product-business-yearly=prod_bus_y"
})
@AutoConfigureMockMvc
class BillingCheckoutApiTest {
  private static final String PASSWORD = "correct-horse-battery";

  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper json;
  @Autowired JdbcTemplate jdbc;
  @Autowired UserJpaRepository users;
  @MockitoBean DodoClient dodo;
  @MockitoSpyBean EmailSender emailSender;

  private String adminBearer(String email) throws Exception {
    mockMvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"" + email + "\",\"organisationName\":\"Checkout " + UUID.randomUUID() + "\"}"))
        .andExpect(status().isAccepted());
    ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
    verify(emailSender, org.mockito.Mockito.atLeastOnce()).send(captor.capture());
    String body = captor.getAllValues().stream().filter(m -> email.equals(m.to()))
        .reduce((a, b) -> b).orElseThrow().textBody();
    String token = body.substring(body.indexOf("token=") + 6).split("\\s")[0];
    String verified = mockMvc.perform(post("/auth/verify-email").contentType(MediaType.APPLICATION_JSON)
            .content("{\"token\":\"" + token + "\",\"password\":\"" + PASSWORD + "\"}"))
        .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    return "Bearer " + json.readTree(verified).get("accessToken").asText();
  }

  @Test
  void adminStartsACheckoutForTheChosenProductAndTenant() throws Exception {
    String email = "co-" + UUID.randomUUID() + "@acme.example";
    String bearer = adminBearer(email);
    UUID tenantId = users.findByEmailIgnoreCase(email).orElseThrow().tenantId();
    when(dodo.createCheckout(any(), any(), any(), any(), any()))
        .thenReturn(new DodoClient.CheckoutSession("cks_1", "https://checkout.test/pay/cks_1"));

    mockMvc.perform(post("/api/v1/billing/checkout").header("Authorization", bearer)
            .contentType(MediaType.APPLICATION_JSON).content("{\"plan\":\"TEAM\",\"interval\":\"MONTHLY\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.checkoutUrl").value("https://checkout.test/pay/cks_1"));

    verify(dodo).createCheckout(eq("prod_team_m"), eq(email), any(), eq(tenantId), any());
  }

  @Test
  void enterpriseAndUnknownCombinationsCannotBeBoughtOnline() throws Exception {
    String bearer = adminBearer("co2-" + UUID.randomUUID() + "@acme.example");
    mockMvc.perform(post("/api/v1/billing/checkout").header("Authorization", bearer)
            .contentType(MediaType.APPLICATION_JSON).content("{\"plan\":\"ENTERPRISE\",\"interval\":\"YEARLY\"}"))
        .andExpect(status().isBadRequest());
    mockMvc.perform(post("/api/v1/billing/checkout").header("Authorization", bearer)
            .contentType(MediaType.APPLICATION_JSON).content("{\"plan\":\"TEAM\",\"interval\":\"WEEKLY\"}"))
        .andExpect(status().isBadRequest());
    verify(dodo, never()).createCheckout(any(), any(), any(), any(), any());
  }

  @Test
  void anApiKeyCannotStartACheckoutOrOpenThePortal() throws Exception {
    String bearer = adminBearer("co3-" + UUID.randomUUID() + "@acme.example");
    String key = json.readTree(mockMvc.perform(post("/api/v1/api-keys").header("Authorization", bearer)
            .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"CI\"}"))
        .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("key").asText();
    mockMvc.perform(post("/api/v1/billing/checkout").header("X-Api-Key", key)
            .contentType(MediaType.APPLICATION_JSON).content("{\"plan\":\"TEAM\",\"interval\":\"MONTHLY\"}"))
        .andExpect(status().isForbidden());
    mockMvc.perform(post("/api/v1/billing/portal").header("X-Api-Key", key)).andExpect(status().isForbidden());
  }

  @Test
  void aWorkspaceThatAlreadyPaysCannotStartASecondSubscription() throws Exception {
    String email = "co5-" + UUID.randomUUID() + "@acme.example";
    String bearer = adminBearer(email);
    UUID tenantId = users.findByEmailIgnoreCase(email).orElseThrow().tenantId();
    jdbc.update("insert into tenant_subscriptions(tenant_id, plan_code, status, current_period_end, updated_at) "
        + "values (?, 'TEAM', 'ACTIVE', current_timestamp + interval '20' day, current_timestamp)", tenantId);

    mockMvc.perform(post("/api/v1/billing/checkout").header("Authorization", bearer)
            .contentType(MediaType.APPLICATION_JSON).content("{\"plan\":\"BUSINESS\",\"interval\":\"MONTHLY\"}"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("billing portal")));
    verify(dodo, never()).createCheckout(any(), any(), any(), any(), any());
  }

  @Test
  void aCancelledAndLapsedWorkspaceCanSubscribeAgain() throws Exception {
    String email = "co6-" + UUID.randomUUID() + "@acme.example";
    String bearer = adminBearer(email);
    UUID tenantId = users.findByEmailIgnoreCase(email).orElseThrow().tenantId();
    jdbc.update("insert into tenant_subscriptions(tenant_id, plan_code, status, grace_until, updated_at) "
        + "values (?, 'TEAM', 'CANCELLED', current_timestamp - interval '1' day, current_timestamp)", tenantId);
    when(dodo.createCheckout(any(), any(), any(), any(), any()))
        .thenReturn(new DodoClient.CheckoutSession("cks_2", "https://checkout.test/pay/cks_2"));
    mockMvc.perform(post("/api/v1/billing/checkout").header("Authorization", bearer)
            .contentType(MediaType.APPLICATION_JSON).content("{\"plan\":\"TEAM\",\"interval\":\"YEARLY\"}"))
        .andExpect(status().isOk());
  }

  @Test
  void aBillingProviderOutageIsABadGatewayNotAServerError() throws Exception {
    String bearer = adminBearer("co7-" + UUID.randomUUID() + "@acme.example");
    when(dodo.createCheckout(any(), any(), any(), any(), any()))
        .thenThrow(new org.springframework.web.client.ResourceAccessException("timed out"));
    mockMvc.perform(post("/api/v1/billing/checkout").header("Authorization", bearer)
            .contentType(MediaType.APPLICATION_JSON).content("{\"plan\":\"TEAM\",\"interval\":\"MONTHLY\"}"))
        .andExpect(status().isBadGateway()).andExpect(jsonPath("$.error").value("billing_provider_unavailable"));
  }

  @Test
  void portalNeedsAnExistingDodoCustomer() throws Exception {
    String email = "co4-" + UUID.randomUUID() + "@acme.example";
    String bearer = adminBearer(email);
    mockMvc.perform(post("/api/v1/billing/portal").header("Authorization", bearer))
        .andExpect(status().isConflict());

    UUID tenantId = users.findByEmailIgnoreCase(email).orElseThrow().tenantId();
    jdbc.update("insert into tenant_subscriptions(tenant_id, plan_code, status, dodo_customer_id, updated_at) "
        + "values (?, 'TEAM', 'ACTIVE', 'cus_9', current_timestamp)", tenantId);
    when(dodo.createPortalSession("cus_9")).thenReturn("https://portal.test/session/abc");
    mockMvc.perform(post("/api/v1/billing/portal").header("Authorization", bearer))
        .andExpect(status().isOk()).andExpect(jsonPath("$.url").value("https://portal.test/session/abc"));
  }
}
