package os.assurance.eu.api.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
    "assurance.eval.worker.enabled=false",
    "assurance.eval.callback.secret=test-eval-callback-secret",
    "assurance.demo.enabled=true",
    "assurance.demo.query-limit-per-15m=3"
})
@AutoConfigureMockMvc
class DemoWorkspaceApiTest {
  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper json;
  @Autowired org.springframework.jdbc.core.JdbcTemplate jdbc;
  @Autowired DemoQuestionCleanupJob demoCleanup;
  private String bearer;

  @BeforeEach
  void signInToDemo() throws Exception {
    String body = mockMvc.perform(post("/auth/demo")).andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();
    JsonNode tokens = json.readTree(body);
    assertThat(tokens.get("accessToken").asText()).isNotBlank();
    assertThat(tokens.get("refreshToken").asText()).isEmpty();
    bearer = "Bearer " + tokens.get("accessToken").asText();
  }

  private JsonNode systems() throws Exception {
    return json.readTree(mockMvc.perform(get("/api/v1/systems").header("Authorization", bearer))
        .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
  }

  private String idOf(String name) throws Exception {
    for (JsonNode s : systems()) {
      if (name.equals(s.get("name").asText())) return s.get("id").asText();
    }
    throw new AssertionError("demo system not found: " + name);
  }

  @Test
  void demoWorkspaceListsTheSeededSystemsAndNothingElse() throws Exception {
    JsonNode all = systems();
    assertThat(all).hasSize(2);
    String names = all.toString();
    assertThat(names).contains("Claims Triage AI (demo)").contains("Support Copilot (demo)");
  }

  @Test
  void seededHighRiskSystemHasEvidenceControlsAndAnOpenWorkflow() throws Exception {
    String id = idOf("Claims Triage AI (demo)");
    String docs = mockMvc.perform(get("/api/v1/evidence/systems/{id}/documents", id).header("Authorization", bearer))
        .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    assertThat(json.readTree(docs)).hasSize(2);
    mockMvc.perform(get("/api/v1/systems/{id}/release-gate", id).header("Authorization", bearer))
        .andExpect(status().isOk());
  }

  @Test
  void writesAreRefusedForTheDemoViewer() throws Exception {
    String id = idOf("Claims Triage AI (demo)");
    mockMvc.perform(post("/api/v1/systems").header("Authorization", bearer)
            .contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isForbidden())
        .andExpect(status().reason(org.hamcrest.Matchers.containsString("demo workspace is read-only")));
    mockMvc.perform(patch("/api/v1/systems/{id}", id).header("Authorization", bearer)
            .contentType(MediaType.APPLICATION_JSON).content("{\"owner\":\"x\"}"))
        .andExpect(status().isForbidden())
        .andExpect(status().reason(org.hamcrest.Matchers.containsString("demo workspace is read-only")));
    mockMvc.perform(post("/api/v1/api-keys").header("Authorization", bearer)
            .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"x\"}"))
        .andExpect(status().isForbidden())
        .andExpect(status().reason(org.hamcrest.Matchers.containsString("demo workspace is read-only")));
    mockMvc.perform(delete("/api/v1/api-keys/{id}", id).header("Authorization", bearer))
        .andExpect(status().isForbidden())
        .andExpect(status().reason(org.hamcrest.Matchers.containsString("demo workspace is read-only")));
    mockMvc.perform(post("/api/v1/evidence/documents").header("Authorization", bearer)
            .contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isForbidden())
        .andExpect(status().reason(org.hamcrest.Matchers.containsString("demo workspace is read-only")));
  }

  @Test
  void visitorQuestionsDoNotStayInTheSharedWorkspace() throws Exception {
    String id = idOf("Claims Triage AI (demo)");
    for (String q : new String[] {"old question from yesterday", "question from a minute ago"}) {
      mockMvc.perform(post("/api/v1/evidence/query").header("Authorization", bearer)
              .contentType(MediaType.APPLICATION_JSON)
              .content("{\"systemId\":\"" + id + "\",\"question\":\"" + q + "\"}"))
          .andExpect(status().isOk());
    }
    jdbc.update("update evidence_queries set created_at = ? where question = ?",
        java.sql.Timestamp.from(java.time.Instant.now().minusSeconds(7200)), "old question from yesterday");

    demoCleanup.clearOldQuestions();

    assertThat(jdbc.queryForList("select question from evidence_queries where tenant_id = ?", String.class,
        os.assurance.eu.api.demo.DemoProperties.DEMO_TENANT_ID)).contains("question from a minute ago").doesNotContain("old question from yesterday");
  }

  @Test
  void evidenceQuestionsAreAllowed() throws Exception {
    String id = idOf("Claims Triage AI (demo)");
    mockMvc.perform(post("/api/v1/evidence/query").header("Authorization", bearer)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"systemId\":\"" + id + "\",\"question\":\"Can reviewers override automated routing?\"}"))
        .andExpect(status().isOk());
  }

  @Test
  void evidenceQuestionsAreRateLimitedPerClientSoTheSharedWorkspaceCannotBeFlooded() throws Exception {
    String id = idOf("Claims Triage AI (demo)");
    String question = "{\"systemId\":\"" + id + "\",\"question\":\"Who can override routing?\"}";
    for (int i = 0; i < 3; i++) {
      mockMvc.perform(post("/api/v1/evidence/query").header("Authorization", bearer)
              .with(r -> { r.setRemoteAddr("203.0.113.9"); return r; })
              .contentType(MediaType.APPLICATION_JSON).content(question))
          .andExpect(status().isOk());
    }
    mockMvc.perform(post("/api/v1/evidence/query").header("Authorization", bearer)
            .with(r -> { r.setRemoteAddr("203.0.113.9"); return r; })
            .contentType(MediaType.APPLICATION_JSON).content(question))
        .andExpect(status().isTooManyRequests());
    // a different client is unaffected
    mockMvc.perform(post("/api/v1/evidence/query").header("Authorization", bearer)
            .with(r -> { r.setRemoteAddr("203.0.113.10"); return r; })
            .contentType(MediaType.APPLICATION_JSON).content(question))
        .andExpect(status().isOk());
  }

  @Test
  void whoAmIFlagsTheDemoWorkspace() throws Exception {
    JsonNode me = json.readTree(mockMvc.perform(get("/api/v1/me").header("Authorization", bearer))
        .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    assertThat(me.get("demo").asBoolean()).isTrue();
    assertThat(me.get("role").asText()).isEqualTo("AUDITOR");
  }

  @Test
  void demoTokenCannotBeRefreshed() throws Exception {
    mockMvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON)
            .content("{\"refreshToken\":\"\"}"))
        .andExpect(status().isUnauthorized());
  }
}
