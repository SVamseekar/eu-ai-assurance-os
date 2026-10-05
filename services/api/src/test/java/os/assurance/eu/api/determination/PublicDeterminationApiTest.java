package os.assurance.eu.api.determination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
    "assurance.eval.worker.enabled=false",
    "assurance.eval.callback.secret=test-eval-callback-secret",
    "assurance.security.auth-rate.per-ip-per-15m=1000"
})
@AutoConfigureMockMvc
class PublicDeterminationApiTest {
  private static final String CREDIT_SCORING = """
      {
        "answers": {
          "operator_role": "provider",
          "sector": "finance",
          "users_affected": "many",
          "decision_impact": "eligibility",
          "biometric": "false",
          "employment": "false",
          "essential_private_service": "true",
          "human_in_loop": "unknown",
          "interacts_with_natural_persons": "false",
          "profiling": "true"
        }
      }
      """;

  @Autowired MockMvc mockMvc;
  @Autowired JdbcTemplate jdbc;

  @Test
  void questionnaireNeedsNoSignIn() throws Exception {
    mockMvc.perform(get("/api/public/v1/determination/questionnaire"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.rulesetVersion").value("v2"))
        .andExpect(jsonPath("$.questions.length()", greaterThanOrEqualTo(8)));
  }

  @Test
  void creditScoringPreviewSuggestsHighRiskAndStoresNothing() throws Exception {
    int runsBefore = jdbc.queryForObject("select count(*) from determination_runs", Integer.class);
    int auditBefore = jdbc.queryForObject("select count(*) from audit_events", Integer.class);

    mockMvc.perform(post("/api/public/v1/determination/preview")
            .contentType(MediaType.APPLICATION_JSON).content(CREDIT_SCORING))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.riskSuggestion.suggestedRiskClass").value("HIGH"))
        .andExpect(jsonPath("$.obligations.length()", greaterThanOrEqualTo(1)))
        .andExpect(jsonPath("$.obligations[0].ruleCode").isNotEmpty())
        .andExpect(jsonPath("$.obligations[0].applicability").isNotEmpty())
        .andExpect(jsonPath("$.disclaimer", containsString("not legal advice")))
        .andExpect(jsonPath("$.rulesetVersion").value("v2"));

    assertThat(jdbc.queryForObject("select count(*) from determination_runs", Integer.class)).isEqualTo(runsBefore);
    assertThat(jdbc.queryForObject("select count(*) from audit_events", Integer.class)).isEqualTo(auditBefore);
  }

  @Test
  void rejectsBodiesOver16KbAndMissingAnswers() throws Exception {
    String big = "{\"answers\":{\"sector\":\"" + "x".repeat(17_000) + "\"}}";
    mockMvc.perform(post("/api/public/v1/determination/preview")
            .contentType(MediaType.APPLICATION_JSON).content(big))
        .andExpect(status().isBadRequest());
    mockMvc.perform(post("/api/public/v1/determination/preview")
            .contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest());
    mockMvc.perform(post("/api/public/v1/determination/preview")
            .contentType(MediaType.APPLICATION_JSON).content("not json"))
        .andExpect(status().isBadRequest());
  }
}
