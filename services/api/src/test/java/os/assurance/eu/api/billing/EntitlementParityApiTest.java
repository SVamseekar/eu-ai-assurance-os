package os.assurance.eu.api.billing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import os.assurance.eu.api.email.EmailMessage;
import os.assurance.eu.api.email.EmailSender;
import os.assurance.eu.api.tenant.UserJpaRepository;

/**
 * Every write that targets one system must honour the plan's read-only rule, not just the endpoints that edit the
 * system record. Otherwise a workspace past its limit could keep changing a "read-only" system through side doors.
 */
@SpringBootTest(properties = {
    "assurance.eval.worker.enabled=false",
    "assurance.eval.callback.secret=test-eval-callback-secret",
    "assurance.app.base-url=https://app.test",
    "assurance.security.auth-rate.per-ip-per-15m=1000",
    "assurance.auth.email.cooldown-seconds=0",
    "spring.jackson.mapper.accept-case-insensitive-enums=true"
})
@AutoConfigureMockMvc
class EntitlementParityApiTest {
  private static final String PASSWORD = "correct-horse-battery";

  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper json;
  @Autowired JdbcTemplate jdbc;
  @Autowired UserJpaRepository users;
  @MockitoSpyBean EmailSender emailSender;

  private String bearer;
  private String oldest;
  private String newer;

  private void freeWorkspaceWithTwoSystems() throws Exception {
    String email = "par-" + UUID.randomUUID() + "@acme.example";
    mockMvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"" + email + "\",\"organisationName\":\"Parity " + UUID.randomUUID() + "\"}"))
        .andExpect(status().isAccepted());
    ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
    verify(emailSender, org.mockito.Mockito.atLeastOnce()).send(captor.capture());
    String body = captor.getAllValues().stream().filter(m -> email.equals(m.to()))
        .reduce((a, b) -> b).orElseThrow().textBody();
    String token = body.substring(body.indexOf("token=") + 6).split("\\s")[0];
    String verified = mockMvc.perform(post("/auth/verify-email").contentType(MediaType.APPLICATION_JSON)
            .content("{\"token\":\"" + token + "\",\"password\":\"" + PASSWORD + "\"}"))
        .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    bearer = "Bearer " + json.readTree(verified).get("accessToken").asText();
    oldest = createSystem("First");
    Thread.sleep(5);
    newer = createSystem("Second");
    UUID tenantId = users.findByEmailIgnoreCase(email).orElseThrow().tenantId();
    jdbc.update("update tenants set trial_ends_at = ? where id = ?",
        Timestamp.from(Instant.now().minusSeconds(60)), tenantId);
  }

  private String createSystem(String name) throws Exception {
    return json.readTree(mockMvc.perform(post("/api/v1/systems").header("Authorization", bearer)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"name":"%s","owner":"Ops","purpose":"parity test","riskClass":"limited",
                 "riskBasis":"fixture","deploymentRegion":"EU","evidenceCoverage":80,"evalScore":80,
                 "dataContractStatus":"warning","openGaps":[]}""".formatted(name)))
        .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("id").asText();
  }

  private ResultActions write(String method, String url, String body) throws Exception {
    var request = "PUT".equals(method) ? put(url) : post(url);
    return mockMvc.perform(request.header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON).content(body));
  }

  /** Each entry is a write that targets {@code systemId}. */
  private List<String[]> systemWrites(String systemId) throws Exception {
    String random = UUID.randomUUID().toString();
    String workflowId = json.readTree(mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .get("/api/v1/systems/" + systemId + "/workflows/active").header("Authorization", bearer))
        .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("id").asText();
    return List.of(
        new String[] {"PUT", "/api/v1/systems/" + systemId + "/conformity", "{}"},
        new String[] {"POST", "/api/v1/systems/" + systemId + "/determination/runs", "{\"answers\":{}}"},
        new String[] {"PUT", "/api/v1/systems/" + systemId + "/controls/" + random, "{\"status\":\"PASS\"}"},
        new String[] {"POST", "/api/v1/systems/" + systemId + "/proposals/map",
            "{\"documents\":[{\"title\":\"t\",\"text\":\"x\"}]}"},
        new String[] {"PUT", "/api/v1/systems/" + systemId + "/assessment/" + random, "{\"applicability\":\"APPLICABLE\"}"},
        new String[] {"POST", "/api/v1/systems/" + systemId + "/workflows/" + workflowId + "/stages/" + random + "/approve", "{}"},
        new String[] {"POST", "/api/v1/data-contracts",
            "{\"systemId\":\"" + systemId + "\",\"name\":\"c\",\"owner\":\"o\",\"version\":\"1\"}"});
  }

  @Test
  void everySystemWriteIsRefusedOnAReadOnlySystem() throws Exception {
    freeWorkspaceWithTwoSystems();
    for (String[] w : systemWrites(newer)) {
      write(w[0], w[1], w[2]).andExpect(status().isPaymentRequired())
          .andExpect(jsonPath("$.code").value("system_read_only"));
    }
  }

  @Test
  void theSameWritesAreNotBlockedByThePlanOnTheEditableSystem() throws Exception {
    freeWorkspaceWithTwoSystems();
    for (String[] w : systemWrites(oldest)) {
      int status = write(w[0], w[1], w[2]).andReturn().getResponse().getStatus();
      assertThat(status).as(w[1]).isNotEqualTo(402);
    }
  }

  @Test
  void aWorkflowOfAReadOnlySystemCannotBeActedOnThroughAnEditableSystemsPath() throws Exception {
    freeWorkspaceWithTwoSystems();
    String workflowOfNewer = json.readTree(mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .get("/api/v1/systems/" + newer + "/workflows/active").header("Authorization", bearer))
        .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("id").asText();
    String random = UUID.randomUUID().toString();
    // path names the editable (oldest) system, but the workflow belongs to the read-only one
    for (String action : new String[] {"approve", "reject", "override"}) {
      write("POST", "/api/v1/systems/" + oldest + "/workflows/" + workflowOfNewer + "/stages/" + random + "/" + action,
          "{\"rationale\":\"x\"}")
          .andExpect(status().isPaymentRequired()).andExpect(jsonPath("$.code").value("system_read_only"));
    }
  }

  @Test
  void theInsuranceIntegrationCannotCreateSystemsPastTheLimit() throws Exception {
    freeWorkspaceWithTwoSystems(); // Free allows one; this workspace already holds two
    write("POST", "/api/v1/integrations/insurance/claims-model-register",
        "{\"externalModelId\":\"ext-1\",\"name\":\"Claims model\"}")
        .andExpect(status().isPaymentRequired()).andExpect(jsonPath("$.code").value("gated_systems"));
  }
}
