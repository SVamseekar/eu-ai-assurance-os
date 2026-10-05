package os.assurance.eu.api.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import os.assurance.eu.api.auth.JwtService;
import os.assurance.eu.api.tenant.TenantContext;
import os.assurance.eu.api.tenant.UserRole;

@SpringBootTest(properties = {
    "assurance.eval.worker.enabled=false",
    "assurance.eval.callback.secret=test-eval-callback-secret"
})
@AutoConfigureMockMvc
class DpaAcceptanceApiTest {
  private static final UUID ADMIN = UUID.fromString("00000000-0000-0000-0000-000000000105");
  private static final UUID ENGINEER = UUID.fromString("00000000-0000-0000-0000-000000000102");
  private static final String API_KEY = "00000000-0000-0000-0000-000000000a01";

  @Autowired MockMvc mockMvc;
  @Autowired JwtService jwtService;
  @Autowired JdbcTemplate jdbc;

  private RequestPostProcessor as(UUID userId, UserRole role) {
    String token = jwtService.issueAccessToken(userId, TenantContext.DEFAULT_TENANT_ID, role);
    return request -> {
      request.addHeader("Authorization", "Bearer " + token);
      return request;
    };
  }

  private int auditEvents() {
    return jdbc.queryForObject(
        "select count(*) from audit_events where event_type = 'account.dpa_accepted' and tenant_id = ?",
        Integer.class, TenantContext.DEFAULT_TENANT_ID);
  }

  @Test
  void adminAcceptsTheDpaAndTheAcceptanceIsStoredAndAudited() throws Exception {
    int before = auditEvents();
    mockMvc.perform(post("/api/v1/account/dpa-acceptance").with(as(ADMIN, UserRole.ADMIN))
            .contentType(MediaType.APPLICATION_JSON).content("{\"version\":\"cp1.1-2026-10\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.version").value("cp1.1-2026-10"))
        .andExpect(jsonPath("$.acceptedAt").isNotEmpty());

    mockMvc.perform(get("/api/v1/account/dpa-acceptance").with(as(ENGINEER, UserRole.AI_ENGINEERING_LEAD)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.version").value("cp1.1-2026-10"));

    assertThat(auditEvents()).isEqualTo(before + 1);
    String stored = jdbc.queryForObject("select dpa_version from tenants where id = ?", String.class,
        TenantContext.DEFAULT_TENANT_ID);
    assertThat(stored).isEqualTo("cp1.1-2026-10");
  }

  @Test
  void onlyAdminsWithASessionCanAccept() throws Exception {
    mockMvc.perform(post("/api/v1/account/dpa-acceptance").with(as(ENGINEER, UserRole.AI_ENGINEERING_LEAD))
            .contentType(MediaType.APPLICATION_JSON).content("{\"version\":\"cp1.1-2026-10\"}"))
        .andExpect(status().isForbidden());
    mockMvc.perform(post("/api/v1/account/dpa-acceptance").header("X-Api-Key", API_KEY)
            .contentType(MediaType.APPLICATION_JSON).content("{\"version\":\"cp1.1-2026-10\"}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void rejectsMissingOrOversizedVersions() throws Exception {
    for (String body : new String[] {"{}", "{\"version\":\"\"}", "{\"version\":\"this-version-is-too-long\"}"}) {
      mockMvc.perform(post("/api/v1/account/dpa-acceptance").with(as(ADMIN, UserRole.ADMIN))
              .contentType(MediaType.APPLICATION_JSON).content(body))
          .andExpect(status().isBadRequest());
    }
  }
}
