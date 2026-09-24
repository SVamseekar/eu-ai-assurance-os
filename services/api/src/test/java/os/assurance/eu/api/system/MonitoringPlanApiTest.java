package os.assurance.eu.api.system;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Locale;
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
    "assurance.corpus.as-of=2026-09-24"
})
@AutoConfigureMockMvc
class MonitoringPlanApiTest {
  @Autowired MockMvc mockMvc;
  @Autowired JwtService jwtService;
  @Autowired ObjectMapper objectMapper;

  @Test
  void packNamesTheStandInAndAnnexIiiStaysInformational() throws Exception {
    String systemId = createSystem(90);
    String annexId = acceptAnnex(systemId);

    MvcResult packed = mockMvc.perform(get("/api/v1/systems/{id}/evidence-pack", systemId).with(officer()))
        .andExpect(status().isOk())
        .andReturn();
    JsonNode plan = objectMapper.readTree(packed.getResponse().getContentAsString()).get("monitoringPlan");
    assertThat(plan).isNotNull();
    assertThat(plan.get("signals").get(0).get("name").asText()).isEqualTo("eval drift");
    assertThat(plan.get("signals").get(0).get("path").asText()).isEqualTo("/api/v1/eval-runs");
    assertThat(plan.get("signals").get(1).get("name").asText()).isEqualTo("contract drift");
    assertThat(plan.get("signals").get(1).get("path").asText())
        .isEqualTo("/api/v1/data-contracts/{contractId}/drift-events");
    assertThat(plan.get("signals").get(2).get("name").asText()).isEqualTo("reg-monitor diff");
    assertThat(plan.get("signals").get(2).get("path").asText()).isEqualTo("/api/v1/reg-monitor/items");
    assertThat(plan.get("commissionTemplateDue").asText()).isEqualTo("2027-09-02");
    assertThat(plan.get("annexIiiTechnicalDocumentationMonitoring").get("forceStatus").asText()).isEqualTo("FUTURE");
    assertThat(plan.get("annexIiiTechnicalDocumentationMonitoring").get("until").asText()).isEqualTo("2027-12-02");
    assertThat(plan.get("annexI").get("forceStatus").asText()).isEqualTo("FUTURE");
    assertThat(plan.get("annexI").get("until").asText()).isEqualTo("2028-08-02");
    assertThat(plan.has("template")).isFalse();
    assertThat(plan.has("connector")).isFalse();
    String planText = plan.toString().toLowerCase(Locale.ROOT);
    assertThat(planText).doesNotContain("compliant");
    assertThat(planText).doesNotContain("certified");
    assertThat(planText).doesNotContain("conformity");

    mockMvc.perform(get("/api/v1/systems/{id}/release-gate", systemId).with(officer()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.decision").value("PASS"))
        .andExpect(jsonPath("$.controls[?(@.proposalId == '" + annexId + "')].mode").value("INFORMATIONAL"))
        .andExpect(jsonPath("$.controls[?(@.proposalId == '" + annexId + "')].forceFrom").value("2027-12-02"))
        .andExpect(jsonPath("$.blockers[?(@ =~ /.*[Aa]nnex.*/)]").isEmpty());
  }

  @Test
  void evalScoreSeventyAndContractBreachStillBlock() throws Exception {
    String systemId = createSystem(90);
    acceptAnnex(systemId);

    mockMvc.perform(patch("/api/v1/systems/{id}", systemId)
            .with(officer())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"evalScore\":70}"))
        .andExpect(status().isOk());
    mockMvc.perform(get("/api/v1/systems/{id}/release-gate", systemId).with(officer()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.decision").value("BLOCKED"))
        .andExpect(jsonPath("$.blockers[?(@ == 'Eval score is below hard release threshold')]").exists());

    String healthyId = createSystem(90);
    MvcResult contract = mockMvc.perform(post("/api/v1/data-contracts")
            .with(officer())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"systemId":"%s","name":"Claims input","owner":"Data","version":"2026-09","status":"HEALTHY","coverage":90}
                """.formatted(healthyId)))
        .andExpect(status().isCreated())
        .andReturn();
    String contractId = objectMapper.readTree(contract.getResponse().getContentAsString()).get("id").asText();
    mockMvc.perform(post("/api/v1/data-contracts/{contractId}/drift-events", contractId)
            .with(officer())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"severity":"BREACH","field":"claim_amount","description":"Null rate exceeded the contract"}
                """))
        .andExpect(status().isCreated());
    mockMvc.perform(get("/api/v1/systems/{id}/release-gate", healthyId).with(officer()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.decision").value("BLOCKED"))
        .andExpect(jsonPath("$.blockers[?(@ == 'Data contract breach is open')]").exists());
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
                {"name":"Monitoring plan sitting","owner":"Reviewer","purpose":"Score a claim",
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
