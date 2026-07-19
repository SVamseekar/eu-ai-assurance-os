package os.assurance.eu.api.determination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.UUID;
import os.assurance.eu.api.tenant.ApiKeyEntity;
import os.assurance.eu.api.tenant.ApiKeyJpaRepository;
import os.assurance.eu.api.tenant.ApiKeyHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@SpringBootTest(properties = {
    "assurance.eval.worker.enabled=false",
    "assurance.eval.callback.secret=test-eval-callback-secret",
    "spring.jackson.mapper.accept-case-insensitive-enums=true"
})
@AutoConfigureMockMvc
class DeterminationApiTest {
  private static final String DEFAULT_API_KEY = "00000000-0000-0000-0000-000000000a01";
  private static final String DEFAULT_TENANT_ID = "00000000-0000-0000-0000-000000000001";
  private static final String DEFAULT_ACTOR_ID = "00000000-0000-0000-0000-000000000101";

  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;
  @Autowired ApiKeyJpaRepository apiKeyRepo;

  @BeforeEach
  void seedApiKey() {
    Instant now = Instant.now();
    UUID apiKeyId = UUID.fromString(DEFAULT_API_KEY);
    apiKeyRepo.findById(apiKeyId)
        .orElseGet(() -> apiKeyRepo.save(new ApiKeyEntity(
            apiKeyId,
            ApiKeyHasher.sha256Hex(DEFAULT_API_KEY),
            UUID.fromString(DEFAULT_TENANT_ID),
            UUID.fromString(DEFAULT_ACTOR_ID),
            now)));
  }

  @Test
  void questionnaireIncludesDisclaimerAndVersionedQuestions() throws Exception {
    mockMvc.perform(get("/api/v1/determination/questionnaire").with(authenticated()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.rulesetVersion").value("v1"))
        .andExpect(jsonPath("$.disclaimer", containsString("not legal advice")))
        .andExpect(jsonPath("$.disclaimer", containsString("human legal reviewer")))
        .andExpect(jsonPath("$.disclaimer", not(containsString("You are compliant"))))
        .andExpect(jsonPath("$.productLabel", containsString("Assisted obligation determination")))
        .andExpect(jsonPath("$.questions.length()", greaterThanOrEqualTo(8)));
  }

  @Test
  void insuranceHighPathProducesApplicableHighObligationsAndAudit() throws Exception {
    String systemId = createSystem("Claims Triage AI", "limited");

    MvcResult runResult = mockMvc.perform(post("/api/v1/systems/{id}/determination/runs", systemId)
            .with(authenticated())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "answers": {
                    "sector": "insurance",
                    "users_affected": "many",
                    "decision_impact": "eligibility",
                    "biometric": false,
                    "employment": false,
                    "essential_private_service": true,
                    "human_in_loop": true,
                    "interacts_with_natural_persons": true,
                    "profiling": true,
                    "high_risk_self_assessment": true
                  }
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.disclaimer", containsString("not legal advice")))
        .andExpect(jsonPath("$.rulesetVersion").value("v1"))
        .andExpect(jsonPath("$.result.riskSuggestion.autoApplied").value(false))
        .andExpect(jsonPath("$.result.riskSuggestion.requiresHumanConfirm").value(true))
        .andExpect(jsonPath("$.result.riskSuggestion.suggestedRiskClass").value("HIGH"))
        .andExpect(jsonPath("$.result.applicableRuleCodes", hasItem("ESSENTIAL_SERVICE_ACCESS")))
        .andExpect(jsonPath("$.result.applicableRuleCodes", hasItem("HIGH_RISK_BUNDLE_SELF_ASSESSED")))
        .andReturn();

    JsonNode run = objectMapper.readTree(runResult.getResponse().getContentAsString());
    String runId = run.get("id").asText();
    assertThat(run.get("obligations").isArray()).isTrue();

    // Risk class must remain unchanged without human confirm
    mockMvc.perform(get("/api/v1/systems/{id}", systemId).with(authenticated()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.riskClass").value("LIMITED"));

    mockMvc.perform(get("/api/v1/systems/{id}/determination/runs/{runId}", systemId, runId)
            .with(authenticated()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(runId))
        .andExpect(jsonPath("$.disclaimer", containsString("Assisted obligation determination")));

    mockMvc.perform(get("/api/v1/audit-events").param("systemId", systemId).with(authenticated()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].eventType", hasItem("determination.run.completed")));

    mockMvc.perform(get("/api/v1/systems/{id}/evidence-pack", systemId).with(authenticated()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.determination.disclaimer", containsString("not legal advice")))
        .andExpect(jsonPath("$.determination.runId").value(runId))
        .andExpect(jsonPath("$.determination.productLabel", containsString("Assisted")));
  }

  @Test
  void minimalChatbotPathHasReducedApplicableSet() throws Exception {
    String systemId = createSystem("Support FAQ Bot", "minimal");

    mockMvc.perform(post("/api/v1/systems/{id}/determination/runs", systemId)
            .with(authenticated())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "answers": {
                    "sector": "other",
                    "users_affected": "few",
                    "decision_impact": "informational",
                    "biometric": false,
                    "employment": false,
                    "essential_private_service": false,
                    "human_in_loop": false,
                    "interacts_with_natural_persons": true,
                    "profiling": false,
                    "high_risk_self_assessment": false
                  }
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result.applicableRuleCodes", hasItem("TRANSPARENCY_NATURAL_PERSONS")))
        .andExpect(jsonPath("$.result.applicableRuleCodes", hasItem("BASELINE_GOVERNANCE")))
        .andExpect(jsonPath("$.result.applicableRuleCodes", not(hasItem("ESSENTIAL_SERVICE_ACCESS"))))
        .andExpect(jsonPath("$.result.applicableRuleCodes", not(hasItem("BIOMETRIC_IDENTIFICATION"))))
        .andExpect(jsonPath("$.result.riskSuggestion.suggestedRiskClass").value("LIMITED"))
        .andExpect(jsonPath("$.result.riskSuggestion.autoApplied").value(false))
        .andExpect(jsonPath("$.disclaimer", containsString("not an official conformity assessment")));
  }

  private String createSystem(String name, String riskClass) throws Exception {
    MvcResult result = mockMvc.perform(post("/api/v1/systems")
            .with(authenticated())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "name": "%s",
                  "owner": "Compliance",
                  "purpose": "Determination engine test system",
                  "riskClass": "%s",
                  "riskBasis": "Test basis",
                  "deploymentRegion": "EU",
                  "evidenceCoverage": 50,
                  "evalScore": 70,
                  "dataContractStatus": "warning",
                  "openGaps": []
                }
                """.formatted(name, riskClass)))
        .andExpect(status().isCreated())
        .andReturn();
    return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
  }

  private RequestPostProcessor authenticated() {
    return request -> {
      request.addHeader("X-Api-Key", DEFAULT_API_KEY);
      return request;
    };
  }
}
