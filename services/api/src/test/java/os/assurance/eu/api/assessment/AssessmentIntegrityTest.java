package os.assurance.eu.api.assessment;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;
import os.assurance.eu.api.auth.JwtService;
import os.assurance.eu.api.tenant.TenantContext;
import os.assurance.eu.api.tenant.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@SpringBootTest(properties = {
    "assurance.eval.worker.enabled=false",
    "assurance.eval.callback.secret=test-eval-callback-secret",
    "assurance.corpus.as-of=2026-09-23"
})
@AutoConfigureMockMvc
class AssessmentIntegrityTest {
  private static final UUID ENGINEER = UUID.fromString("00000000-0000-0000-0000-000000000102");
  private static final String CALLER = "00000000-0000-0000-0000-000000000101";
  private static final String PROVISION = "02016R0679-20160504#5:1:";

  @Autowired MockMvc mockMvc;
  @Autowired JwtService jwtService;
  @Autowired ObjectMapper objectMapper;

  @Test
  void reviewerIsTheCallerAndExceptionsAreBounded() throws Exception {
    String systemId = createSystem();
    String proposalId = proposal(systemId);

    mockMvc.perform(put("/api/v1/systems/{id}/assessment/{proposalId}", systemId, proposalId)
            .with(officer())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"applicability":"NOT_APPLICABLE","reviewerId":"%s"}
                """.formatted(ENGINEER)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.reviewerId").value(CALLER));

    LocalDate yesterday = LocalDate.now(ZoneOffset.UTC).minusDays(1);
    mockMvc.perform(post("/api/v1/systems/{id}/assessment/exceptions", systemId)
            .with(officer())
            .contentType(MediaType.APPLICATION_JSON)
            .content(exceptionBody(proposalId, yesterday, "Short rationale.")))
        .andExpect(status().isBadRequest());

    LocalDate tooFar = LocalDate.now(ZoneOffset.UTC).plusYears(2);
    mockMvc.perform(post("/api/v1/systems/{id}/assessment/exceptions", systemId)
            .with(officer())
            .contentType(MediaType.APPLICATION_JSON)
            .content(exceptionBody(proposalId, tooFar, "Short rationale.")))
        .andExpect(status().isBadRequest());

    mockMvc.perform(post("/api/v1/systems/{id}/assessment/exceptions", systemId)
            .with(officer())
            .contentType(MediaType.APPLICATION_JSON)
            .content(exceptionBody(proposalId, LocalDate.now(ZoneOffset.UTC).plusDays(30), "r".repeat(3000))))
        .andExpect(status().isBadRequest());

    LocalDate ok = LocalDate.now(ZoneOffset.UTC).plusDays(30);
    mockMvc.perform(post("/api/v1/systems/{id}/assessment/exceptions", systemId)
            .with(officer())
            .contentType(MediaType.APPLICATION_JSON)
            .content(exceptionBody(proposalId, ok, "Recorded gap with an owner.")))
        .andExpect(status().isCreated());

    mockMvc.perform(get("/api/v1/audit-events").with(officer()).param("systemId", systemId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.eventType=='assessment.applicability_set')].payload.from").isNotEmpty())
        .andExpect(jsonPath("$[?(@.eventType=='assessment.applicability_set')].payload.to", hasItem("NOT_APPLICABLE")))
        .andExpect(jsonPath("$[?(@.eventType=='assessment.applicability_set')].payload.reviewerId", hasItem(CALLER)))
        .andExpect(jsonPath("$[?(@.eventType=='assessment.exception_recorded')].payload.expiresOn", hasItem(ok.toString())));
  }

  private String createSystem() throws Exception {
    MvcResult created = mockMvc.perform(post("/api/v1/systems")
            .with(officer())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"name":"Assessed system","owner":"Reviewer","purpose":"Bind the reviewer",
                 "riskClass":"HIGH","riskBasis":"Annex III","deploymentRegion":"EU"}
                """))
        .andExpect(status().isCreated())
        .andReturn();
    return objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText();
  }

  private String proposal(String systemId) throws Exception {
    MvcResult created = mockMvc.perform(post("/api/v1/systems/{id}/proposals", systemId)
            .with(officer())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"relation\":\"supports\",\"provisionKey\":\"" + PROVISION + "\",\"excerpt\":\"Excerpt.\"}"))
        .andExpect(status().isCreated())
        .andReturn();
    return objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText();
  }

  private static String exceptionBody(String proposalId, LocalDate expiresOn, String rationale) {
    return """
        {"proposalId":"%s","rationale":"%s","expiresOn":"%s"}
        """.formatted(proposalId, rationale, expiresOn);
  }

  private RequestPostProcessor officer() {
    String token = jwtService.issueAccessToken(
        TenantContext.DEFAULT_USER_ID, TenantContext.DEFAULT_TENANT_ID, UserRole.COMPLIANCE_OFFICER);
    return request -> {
      request.addHeader("Authorization", "Bearer " + token);
      return request;
    };
  }
}
