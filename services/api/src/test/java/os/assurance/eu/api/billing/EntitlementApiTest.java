package os.assurance.eu.api.billing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
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
import os.assurance.eu.api.email.EmailMessage;
import os.assurance.eu.api.email.EmailSender;
import os.assurance.eu.api.tenant.TenantContext;
import os.assurance.eu.api.tenant.UserJpaRepository;

@SpringBootTest(properties = {
    "assurance.eval.worker.enabled=false",
    "assurance.eval.callback.secret=test-eval-callback-secret",
    "assurance.app.base-url=https://app.test",
    "assurance.security.auth-rate.per-ip-per-15m=1000",
    "assurance.auth.email.cooldown-seconds=0",
    "spring.jackson.mapper.accept-case-insensitive-enums=true"
})
@AutoConfigureMockMvc
class EntitlementApiTest {
  private static final String PASSWORD = "correct-horse-battery";

  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper json;
  @Autowired JdbcTemplate jdbc;
  @Autowired UserJpaRepository users;
  @Autowired EntitlementService entitlements;
  @MockitoSpyBean EmailSender emailSender;

  record Workspace(String bearer, UUID tenantId, List<String> systemIds) {}

  private Workspace newWorkspace(int systems) throws Exception {
    String email = "ent-" + UUID.randomUUID() + "@acme.example";
    mockMvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"" + email + "\",\"organisationName\":\"Ent " + UUID.randomUUID() + "\"}"))
        .andExpect(status().isAccepted());
    ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
    verify(emailSender, org.mockito.Mockito.atLeastOnce()).send(captor.capture());
    String body = captor.getAllValues().stream().filter(m -> email.equals(m.to()))
        .reduce((a, b) -> b).orElseThrow().textBody();
    String token = body.substring(body.indexOf("token=") + 6).split("\\s")[0];
    String verified = mockMvc.perform(post("/auth/verify-email").contentType(MediaType.APPLICATION_JSON)
            .content("{\"token\":\"" + token + "\",\"password\":\"" + PASSWORD + "\"}"))
        .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    String bearer = "Bearer " + json.readTree(verified).get("accessToken").asText();
    UUID tenantId = users.findByEmailIgnoreCase(email).orElseThrow().tenantId();
    List<String> ids = new ArrayList<>();
    for (int i = 0; i < systems; i++) {
      ids.add(createSystem(bearer, "System " + i).andReturnId());
      Thread.sleep(5); // distinct creation times keep "oldest first" deterministic
    }
    return new Workspace(bearer, tenantId, ids);
  }

  record Created(org.springframework.test.web.servlet.ResultActions actions, ObjectMapper json) {
    String andReturnId() throws Exception {
      return json.readTree(actions.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString())
          .get("id").asText();
    }
  }

  private Created createSystem(String bearer, String name) throws Exception {
    return new Created(mockMvc.perform(post("/api/v1/systems").header("Authorization", bearer)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"name":"%s","owner":"Ops","purpose":"entitlement test","riskClass":"limited",
             "riskBasis":"fixture","deploymentRegion":"EU","evidenceCoverage":80,"evalScore":80,
             "dataContractStatus":"warning","openGaps":[]}""".formatted(name))), json);
  }

  private void endTrial(Workspace w) {
    jdbc.update("update tenants set trial_ends_at = ? where id = ?",
        Timestamp.from(Instant.now().minusSeconds(60)), w.tenantId());
  }

  @Test
  void aTrialWorkspaceCanRegisterManySystems() throws Exception {
    Workspace w = newWorkspace(4);
    assertThat(w.systemIds()).hasSize(4);
    assertThat(entitlements.effectivePlan(w.tenantId())).isEqualTo(PlanCatalog.TRIAL);
  }

  @Test
  void afterTheTrialOnlyTheOldestSystemStaysEditableAndExtrasAreReadOnlyNotDeleted() throws Exception {
    Workspace w = newWorkspace(2);
    endTrial(w);

    createSystem(w.bearer(), "Third").actions().andExpect(status().isPaymentRequired())
        .andExpect(jsonPath("$.error").value("plan_limit"))
        .andExpect(jsonPath("$.code").value("gated_systems"))
        .andExpect(jsonPath("$.upgradeUrl").value("/settings#billing"));

    String oldest = w.systemIds().get(0);
    String newer = w.systemIds().get(1);
    mockMvc.perform(patch("/api/v1/systems/{id}", oldest).header("Authorization", w.bearer())
            .contentType(MediaType.APPLICATION_JSON).content("{\"owner\":\"New Owner\"}"))
        .andExpect(status().isOk());
    mockMvc.perform(patch("/api/v1/systems/{id}", newer).header("Authorization", w.bearer())
            .contentType(MediaType.APPLICATION_JSON).content("{\"owner\":\"Blocked\"}"))
        .andExpect(status().isPaymentRequired()).andExpect(jsonPath("$.code").value("system_read_only"));

    mockMvc.perform(get("/api/v1/systems/{id}", newer).header("Authorization", w.bearer()))
        .andExpect(status().isOk());
    mockMvc.perform(get("/api/v1/systems/{id}/evidence-pack", oldest).header("Authorization", w.bearer()))
        .andExpect(status().isOk());
    mockMvc.perform(get("/api/v1/systems/{id}/evidence-pack.pdf", oldest).header("Authorization", w.bearer()))
        .andExpect(status().isPaymentRequired()).andExpect(jsonPath("$.code").value("feature_signed_pdf"));
  }

  @Test
  void theFreePlanAllowsThreeHundredGateRunsAMonth() throws Exception {
    Workspace w = newWorkspace(1);
    endTrial(w);
    String key = json.readTree(mockMvc.perform(post("/api/v1/api-keys").header("Authorization", w.bearer())
            .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"CI\"}"))
        .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("key").asText();
    for (int i = 0; i < 300; i++) {
      mockMvc.perform(get("/api/v1/ci/release-gate").param("systemId", w.systemIds().get(0)).header("X-Api-Key", key))
          .andExpect(status().isOk());
    }
    mockMvc.perform(get("/api/v1/ci/release-gate").param("systemId", w.systemIds().get(0)).header("X-Api-Key", key))
        .andExpect(status().isPaymentRequired()).andExpect(jsonPath("$.code").value("gate_runs"));
  }

  @Test
  void editorSeatsAreLimitedButViewersAreFree() throws Exception {
    Workspace w = newWorkspace(1);
    endTrial(w); // FREE: 3 editor seats; the admin already holds one
    for (String role : new String[] {"AI_ENGINEERING_LEAD", "COMPLIANCE_OFFICER"}) {
      invite(w, "e-" + UUID.randomUUID() + "@acme.example", role).andExpect(status().isCreated());
    }
    invite(w, "e-" + UUID.randomUUID() + "@acme.example", "ADMIN")
        .andExpect(status().isPaymentRequired()).andExpect(jsonPath("$.code").value("editor_seats"));
    invite(w, "v-" + UUID.randomUUID() + "@acme.example", "AUDITOR").andExpect(status().isCreated());
    invite(w, "l-" + UUID.randomUUID() + "@acme.example", "LEGAL_COUNSEL").andExpect(status().isCreated());
  }

  private org.springframework.test.web.servlet.ResultActions invite(Workspace w, String email, String role)
      throws Exception {
    return mockMvc.perform(post("/api/v1/admin/users/invites").header("Authorization", w.bearer())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"email\":\"" + email + "\",\"role\":\"" + role + "\"}"));
  }

  @Test
  void legacyDefaultWorkspaceIsNotLimited() {
    assertThat(entitlements.effectivePlan(TenantContext.DEFAULT_TENANT_ID)).isEqualTo(PlanCatalog.ENTERPRISE);
  }
}
