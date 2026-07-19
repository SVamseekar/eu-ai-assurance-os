package os.assurance.eu.api.readiness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.UUID;
import os.assurance.eu.api.tenant.ApiKeyEntity;
import os.assurance.eu.api.tenant.ApiKeyHasher;
import os.assurance.eu.api.tenant.ApiKeyJpaRepository;
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
class CertificationReadinessApiTest {
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
  void blockedSystemIsNotReadyWithGaps() throws Exception {
    String systemId = createSystem(
        "Blocked Claims AI",
        "high",
        40,
        50,
        "BREACH",
        "[\"Human oversight SOP missing\"]");

    mockMvc.perform(get("/api/v1/systems/{id}/certification-readiness", systemId)
            .with(authenticated()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.systemId").value(systemId))
        .andExpect(jsonPath("$.readinessStatus").value("NOT_READY"))
        .andExpect(jsonPath("$.score").isNumber())
        .andExpect(jsonPath("$.productLabel", containsString("Certification readiness")))
        .andExpect(jsonPath("$.disclaimer", containsString("does not issue certificates")))
        .andExpect(jsonPath("$.disclaimer", containsString("not legal advice")))
        .andExpect(jsonPath("$.gaps").isArray())
        .andExpect(jsonPath("$.gaps.length()", greaterThanOrEqualTo(1)))
        .andExpect(jsonPath("$.dimensions.length()", greaterThanOrEqualTo(9)))
        .andExpect(jsonPath("$.certified").doesNotExist())
        .andExpect(jsonPath("$", not(org.hamcrest.Matchers.hasKey("certified"))));

    MvcResult result = mockMvc.perform(get("/api/v1/systems/{id}/certification-readiness", systemId)
            .with(authenticated()))
        .andExpect(status().isOk())
        .andReturn();
    String body = result.getResponse().getContentAsString();
    assertThat(body).doesNotContain("\"certified\":true");
    assertThat(body).doesNotContain("you are certified");
    assertThat(body.toLowerCase()).doesNotContain("you are certified");
  }

  @Test
  void happyPathIsReadyForReview() throws Exception {
    String systemId = createSystem(
        "Ready FAQ Bot",
        "limited",
        95,
        92,
        "HEALTHY",
        "[]");

    // Index evidence document
    mockMvc.perform(post("/api/v1/evidence/documents")
            .with(authenticated())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "systemId": "%s",
                  "type": "POLICY",
                  "title": "Transparency SOP",
                  "sourceUri": "memory://transparency-sop",
                  "content": "Users interacting with the FAQ bot are disclosed AI use. Human escalation path documented for edge cases."
                }
                """.formatted(systemId)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.ingestionStatus").value("indexed"));

    // Pass all attached controls
    passAllControls(systemId);

    // Assisted determination run present
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
        .andExpect(status().isOk());

    // Determination may open additional controls in REVIEW — pass them all
    passAllControls(systemId);

    MvcResult readinessResult = mockMvc.perform(get("/api/v1/systems/{id}/certification-readiness", systemId)
            .with(authenticated()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.score", greaterThanOrEqualTo(90)))
        .andExpect(jsonPath("$.productLabel", containsString("Certification readiness")))
        .andExpect(jsonPath("$.disclaimer", containsString("does not issue certificates")))
        .andExpect(jsonPath("$.releaseDecision").value("PASS"))
        .andExpect(jsonPath("$.certified").doesNotExist())
        .andReturn();

    JsonNode readiness = objectMapper.readTree(readinessResult.getResponse().getContentAsString());
    String readinessStatus = readiness.get("readinessStatus").asText();
    // Happy path must reach READY_FOR_REVIEW when the audit chain verifies.
    // Audit Instant millis truncation makes chain verification portable across H2/CI.
    assertThat(readinessStatus)
        .as("readinessStatus body=%s", readinessResult.getResponse().getContentAsString())
        .isEqualTo("READY_FOR_REVIEW");

    // Export JSON report
    mockMvc.perform(post("/api/v1/systems/{id}/certification-readiness/export", systemId)
            .with(authenticated())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"format\":\"json\"}"))
        .andExpect(status().isOk())
        .andExpect(header().string("X-Readiness-Status", "READY_FOR_REVIEW"))
        .andExpect(jsonPath("$.readinessStatus").value("READY_FOR_REVIEW"))
        .andExpect(jsonPath("$.certified").doesNotExist());

    // Export PDF report
    mockMvc.perform(post("/api/v1/systems/{id}/certification-readiness/export", systemId)
            .with(authenticated())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"format\":\"pdf\"}"))
        .andExpect(status().isOk())
        .andExpect(header().string("Content-Type", containsString("application/pdf")))
        .andExpect(header().exists("Content-Disposition"));

    mockMvc.perform(get("/api/v1/audit-events").param("systemId", systemId).with(authenticated()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].eventType", hasItem("certification_readiness.assessed")))
        .andExpect(jsonPath("$[*].eventType", hasItem("certification_readiness.exported")));
  }

  private void passAllControls(String systemId) throws Exception {
    MvcResult controlsResult = mockMvc.perform(get("/api/v1/systems/{id}/controls", systemId)
            .with(authenticated()))
        .andExpect(status().isOk())
        .andReturn();
    JsonNode controls = objectMapper.readTree(controlsResult.getResponse().getContentAsString());
    for (JsonNode control : controls) {
      String controlId = control.get("controlId").asText();
      mockMvc.perform(put("/api/v1/systems/{id}/controls/{controlId}", systemId, controlId)
              .with(authenticated())
              .contentType(MediaType.APPLICATION_JSON)
              .content("{\"status\":\"PASS\",\"notes\":\"Readiness test evidence reviewed\"}"))
          .andExpect(status().isOk());
    }
  }

  private String createSystem(
      String name,
      String riskClass,
      int evidenceCoverage,
      int evalScore,
      String dataContractStatus,
      String openGapsJson) throws Exception {
    MvcResult result = mockMvc.perform(post("/api/v1/systems")
            .with(authenticated())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "name": "%s",
                  "owner": "Compliance",
                  "purpose": "Certification readiness test system",
                  "riskClass": "%s",
                  "riskBasis": "Test basis for readiness scoring",
                  "deploymentRegion": "EU",
                  "evidenceCoverage": %d,
                  "evalScore": %d,
                  "dataContractStatus": "%s",
                  "openGaps": %s
                }
                """.formatted(name, riskClass, evidenceCoverage, evalScore, dataContractStatus, openGapsJson)))
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
