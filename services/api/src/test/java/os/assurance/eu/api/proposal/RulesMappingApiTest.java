package os.assurance.eu.api.proposal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import os.assurance.eu.api.auth.JwtService;
import os.assurance.eu.api.tenant.TenantContext;
import os.assurance.eu.api.tenant.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
class RulesMappingApiTest {
  private static final UUID ENGINEER = UUID.fromString("00000000-0000-0000-0000-000000000102");

  @Autowired MockMvc mockMvc;
  @Autowired JwtService jwtService;
  @Autowired ObjectMapper objectMapper;

  @Test
  void fiveDocumentsBecomePendingCitationsWithoutBlockingOnAnnexIii() throws Exception {
    String systemId = createSystem(90);
    mockMvc.perform(post("/api/v1/systems/{id}/proposals/map", systemId)
            .with(officer())
            .contentType(MediaType.APPLICATION_JSON)
            .content(fiveDocuments()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.length()").value(5));

    MvcResult listed = mockMvc.perform(get("/api/v1/systems/{id}/proposals", systemId).with(officer()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items.length()").value(5))
        .andReturn();
    JsonNode items = objectMapper.readTree(listed.getResponse().getContentAsString()).get("items");
    JsonNode corpus = objectMapper.readTree(mockMvc.perform(get("/api/v1/corpus").with(officer()))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString());
    String current = corpus.get("corpusVersion").asText();
    boolean sawInForce = false;
    boolean sawAnnex = false;
    for (JsonNode item : items) {
      assertThat(item.get("status").asText()).isEqualTo("PENDING");
      assertThat(item.get("adapterVersion").isNull()).isTrue();
      assertThat(item.get("corpusVersion").asText()).isEqualTo(current);
      String key = item.get("provisionKey").asText();
      assertThat(corpus.get("provisions").findValuesAsText("provisionKey")).contains(key);
      if ("02024R1689-20260727#annexIII::".equals(key)) {
        sawAnnex = true;
        assertThat(item.get("forceStatus").asText()).isEqualTo("FUTURE");
        assertThat(item.get("forceFrom").asText()).isEqualTo("2027-12-02");
      }
      if ("IN_FORCE".equals(item.get("forceStatus").asText())) {
        sawInForce = true;
      }
    }
    assertThat(sawAnnex).isTrue();
    assertThat(sawInForce).isTrue();

    mockMvc.perform(get("/api/v1/systems/{id}/release-gate", systemId).with(officer()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.blockers[?(@ =~ /.*[Aa]nnex III.*/)]").isEmpty())
        .andExpect(jsonPath("$.blockers[?(@ =~ /.*proposal.*/)]").isEmpty());

    mockMvc.perform(patch("/api/v1/systems/{id}", systemId)
            .with(officer())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"evalScore\":70}"))
        .andExpect(status().isOk());
    mockMvc.perform(get("/api/v1/systems/{id}/release-gate", systemId).with(officer()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.decision").value("BLOCKED"))
        .andExpect(jsonPath("$.blockers[?(@ == 'Eval score is below hard release threshold')]").exists());
  }

  @Test
  void officerAcceptsMappedRowEngineerIsForbiddenAndAbstainConflicts() throws Exception {
    String systemId = createSystem(90);
    MvcResult mapped = mockMvc.perform(post("/api/v1/systems/{id}/proposals/map", systemId)
            .with(officer())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"documents":[
                  {"title":"Approval note","text":"Approval note: natural persons are informed that they interact with an AI system."},
                  {"title":"Lunch rota","text":"Sandwiches on Tuesday. No statute and no model."}
                ]}
                """))
        .andExpect(status().isCreated())
        .andReturn();
    JsonNode rows = objectMapper.readTree(mapped.getResponse().getContentAsString());
    String supports = null;
    String abstain = null;
    for (JsonNode row : rows) {
      if ("abstain".equals(row.get("relation").asText())) {
        abstain = row.get("id").asText();
      } else {
        supports = row.get("id").asText();
      }
    }
    assertThat(supports).isNotBlank();
    assertThat(abstain).isNotBlank();

    mockMvc.perform(post("/api/v1/evidence/documents")
            .with(officer())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"systemId":"%s","type":"NOTE","title":"Tenant only","sourceUri":"memory://tenant",
                 "content":"Annex III high-risk credit scoring lives only in this tenant file."}
                """.formatted(systemId)))
        .andExpect(status().isCreated());
    MvcResult isolated = mockMvc.perform(post("/api/v1/systems/{id}/proposals/map", systemId)
            .with(officer())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"documents":[{"title":"Unrelated note","text":"Sandwiches on Tuesday. No statute and no model."}]}
                """))
        .andExpect(status().isCreated())
        .andReturn();
    JsonNode isolatedRow = objectMapper.readTree(isolated.getResponse().getContentAsString()).get(0);
    assertThat(isolatedRow.get("relation").asText()).isEqualTo("abstain");
    assertThat(isolatedRow.get("provisionKey").isNull()).isTrue();

    mockMvc.perform(post("/api/v1/systems/{id}/proposals/{proposalId}/accept", systemId, supports)
            .with(engineer()))
        .andExpect(status().isForbidden());
    mockMvc.perform(post("/api/v1/systems/{id}/proposals/{proposalId}/accept", systemId, abstain)
            .with(officer()))
        .andExpect(status().isConflict());
    mockMvc.perform(post("/api/v1/systems/{id}/proposals/{proposalId}/accept", systemId, supports)
            .with(officer()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("ACCEPTED"));
  }

  private String fiveDocuments() {
    return """
        {"documents":[
          {"title":"Model card","text":"Model card for a credit-scoring assistant. The system is a high-risk AI system listed in Annex III."},
          {"title":"Approval note","text":"Approval note: natural persons are informed that they interact with an AI system before the release."},
          {"title":"Dataset note","text":"Dataset note. Training rows are personal data and shall be processed lawfully, fairly and in a transparent manner."},
          {"title":"Eval summary","text":"Eval summary for security of network and information systems supporting the business processes of financial entities. Score 91."},
          {"title":"Retention policy","text":"Retention policy. Personal data shall be processed lawfully and kept only for the stated retention window."}
        ]}
        """;
  }

  private String createSystem(int evalScore) throws Exception {
    MvcResult created = mockMvc.perform(post("/api/v1/systems")
            .with(officer())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"name":"Rules mapper sitting","owner":"Compliance","purpose":"Map five documents",
                 "riskClass":"LIMITED","riskBasis":"Limited transparency duties",
                 "deploymentRegion":"EU","evidenceCoverage":90,"evalScore":%d,
                 "dataContractStatus":"HEALTHY","openGaps":[]}
                """.formatted(evalScore)))
        .andExpect(status().isCreated())
        .andReturn();
    return objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText();
  }

  private RequestPostProcessor officer() {
    return bearer(TenantContext.DEFAULT_USER_ID, UserRole.COMPLIANCE_OFFICER);
  }

  private RequestPostProcessor engineer() {
    return bearer(ENGINEER, UserRole.AI_ENGINEERING_LEAD);
  }

  private RequestPostProcessor bearer(UUID userId, UserRole role) {
    String token = jwtService.issueAccessToken(userId, TenantContext.DEFAULT_TENANT_ID, role);
    return request -> {
      request.addHeader("Authorization", "Bearer " + token);
      return request;
    };
  }
}
