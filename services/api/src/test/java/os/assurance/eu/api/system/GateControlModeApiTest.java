package os.assurance.eu.api.system;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class GateControlModeApiTest {
  @Autowired MockMvc mockMvc;
  @Autowired JwtService jwtService;
  @Autowired ObjectMapper objectMapper;

  @Test
  void acceptedAnnexIiiStaysInformationalAndDoesNotMoveTheDecision() throws Exception {
    String systemId = createSystem(90);
    String annexId = acceptAnnex(systemId);

    mockMvc.perform(get("/api/v1/systems/{id}/release-gate", systemId).with(officer()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.decision").value("PASS"))
        .andExpect(jsonPath("$.controls[?(@.proposalId == '" + annexId + "')].mode").value("INFORMATIONAL"))
        .andExpect(jsonPath("$.controls[?(@.proposalId == '" + annexId + "')].forceFrom").value("2027-12-02"))
        .andExpect(jsonPath("$.blockers[?(@ =~ /.*[Aa]nnex.*/)]").isEmpty());
  }

  @Test
  void futureControlSetToBlockingIsRejected() throws Exception {
    String systemId = createSystem(90);
    String annexId = acceptAnnex(systemId);

    mockMvc.perform(put("/api/v1/systems/{id}/proposals/{proposalId}/mode", systemId, annexId)
            .with(officer())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"mode\":\"BLOCKING\"}"))
        .andExpect(status().isConflict());

    mockMvc.perform(get("/api/v1/systems/{id}/release-gate", systemId).with(officer()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.decision").value("PASS"))
        .andExpect(jsonPath("$.controls[?(@.proposalId == '" + annexId + "')].mode").value("INFORMATIONAL"));
  }

  @Test
  void evalScoreSeventyStillBlocks() throws Exception {
    String systemId = createSystem(90);
    acceptAnnex(systemId);
    mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch(
                "/api/v1/systems/{id}", systemId)
            .with(officer())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"evalScore\":70}"))
        .andExpect(status().isOk());

    mockMvc.perform(get("/api/v1/systems/{id}/release-gate", systemId).with(officer()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.decision").value("BLOCKED"))
        .andExpect(jsonPath("$.blockers[?(@ == 'Eval score is below hard release threshold')]").exists());
  }

  private String acceptAnnex(String systemId) throws Exception {
    mockMvc.perform(post("/api/v1/systems/{id}/proposals/map", systemId)
            .with(officer())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"documents":[{"title":"Model card","text":"Model card for a credit-scoring assistant. The system is a high-risk AI system listed in Annex III."}]}
                """))
        .andExpect(status().isCreated());
    JsonNode items = objectMapper.readTree(mockMvc.perform(get("/api/v1/systems/{id}/proposals", systemId).with(officer()))
            .andReturn()
            .getResponse()
            .getContentAsString())
        .get("items");
    String annexId = null;
    for (JsonNode item : items) {
      if ("FUTURE".equals(text(item, "forceStatus")) && "2027-12-02".equals(text(item, "forceFrom"))) {
        annexId = item.get("id").asText();
      }
    }
    assertThat(annexId).isNotBlank();
    mockMvc.perform(post("/api/v1/systems/{id}/proposals/{proposalId}/accept", systemId, annexId).with(officer()))
        .andExpect(status().isOk());
    return annexId;
  }

  private String createSystem(int evalScore) throws Exception {
    MvcResult created = mockMvc.perform(post("/api/v1/systems")
            .with(officer())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"name":"Gate mode sitting","owner":"Reviewer","purpose":"Score a claim",
                 "riskClass":"LIMITED","riskBasis":"Limited transparency duties",
                 "deploymentRegion":"EU","evidenceCoverage":90,"evalScore":%d,
                 "dataContractStatus":"HEALTHY","openGaps":[]}
                """.formatted(evalScore)))
        .andExpect(status().isCreated())
        .andReturn();
    return objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText();
  }

  private static String text(JsonNode node, String field) {
    JsonNode value = node.get(field);
    return value == null || value.isNull() ? "" : value.asText();
  }

  private RequestPostProcessor officer() {
    String token = jwtService.issueAccessToken(
        TenantContext.DEFAULT_USER_ID,
        TenantContext.DEFAULT_TENANT_ID,
        UserRole.COMPLIANCE_OFFICER);
    return request -> {
      request.addHeader("Authorization", "Bearer " + token);
      return request;
    };
  }
}
