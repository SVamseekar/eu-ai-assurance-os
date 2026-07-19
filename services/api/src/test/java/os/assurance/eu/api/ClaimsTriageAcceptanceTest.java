package os.assurance.eu.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * PRD §3 Claims Triage + §6 acceptance slice: create HIGH system, attach controls,
 * block on control, export evidence pack, assert audit trail.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ClaimsTriageAcceptanceTest {
  private static final String DEFAULT_API_KEY = "00000000-0000-0000-0000-000000000a01";

  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;

  @Test
  void claimsTriageHighRiskControlBlockAndEvidencePack() throws Exception {
    MvcResult create = mockMvc.perform(post("/api/v1/systems")
            .header("X-Api-Key", DEFAULT_API_KEY)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "name": "Claims Triage AI",
                  "owner": "Insurance Ops",
                  "purpose": "Prioritize and route insurance claims",
                  "riskClass": "HIGH",
                  "riskBasis": "Essential private services eligibility",
                  "deploymentRegion": "EU",
                  "evidenceCoverage": 90,
                  "evalScore": 90,
                  "dataContractStatus": "HEALTHY",
                  "openGaps": ["Human oversight SOP missing"],
                  "vendorName": "MSV AI Labs",
                  "modelName": "claims-triage",
                  "modelVersion": "4.1",
                  "dataSources": ["claims_core", "policy_db"],
                  "sector": "insurance",
                  "decisionImpact": "claims prioritization",
                  "affectedUsers": ["claimants"]
                }
                """))
        .andExpect(status().isCreated())
        .andReturn();
    UUID systemId = UUID.fromString(
        objectMapper.readTree(create.getResponse().getContentAsString()).get("id").asText());

    mockMvc.perform(get("/api/v1/systems/{id}/controls", systemId)
            .header("X-Api-Key", DEFAULT_API_KEY))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].controlCode").exists());

    MvcResult controls = mockMvc.perform(get("/api/v1/systems/{id}/controls", systemId)
            .header("X-Api-Key", DEFAULT_API_KEY))
        .andExpect(status().isOk())
        .andReturn();
    JsonNode list = objectMapper.readTree(controls.getResponse().getContentAsString());
    assertThat(list.isArray()).isTrue();
    assertThat(list.size()).isGreaterThan(0);

    String oversightControlId = null;
    for (JsonNode node : list) {
      if ("HUMAN_OVERSIGHT".equals(node.get("controlCode").asText())) {
        oversightControlId = node.get("controlId").asText();
        break;
      }
    }
    if (oversightControlId == null) {
      oversightControlId = list.get(0).get("controlId").asText();
    }

    mockMvc.perform(put("/api/v1/systems/{id}/controls/{controlId}", systemId, oversightControlId)
            .header("X-Api-Key", DEFAULT_API_KEY)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\":\"BLOCKED\",\"notes\":\"Missing Art.14 SOP\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("BLOCKED"));

    mockMvc.perform(get("/api/v1/systems/{id}/release-gate", systemId)
            .header("X-Api-Key", DEFAULT_API_KEY))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.decision").value("BLOCKED"))
        .andExpect(jsonPath("$.blockers").isArray());

    mockMvc.perform(get("/api/v1/systems/{id}/evidence-pack", systemId)
            .header("X-Api-Key", DEFAULT_API_KEY))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.riskClassification.vendorName").value("MSV AI Labs"))
        .andExpect(jsonPath("$.riskClassification.modelName").value("claims-triage"));

    mockMvc.perform(get("/api/v1/audit-events")
            .header("X-Api-Key", DEFAULT_API_KEY)
            .param("systemId", systemId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].eventType").exists());
  }
}
