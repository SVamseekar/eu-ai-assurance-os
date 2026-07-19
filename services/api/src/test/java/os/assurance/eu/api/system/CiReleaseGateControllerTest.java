package os.assurance.eu.api.system;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import os.assurance.eu.api.observability.AssuranceMetrics;
import os.assurance.eu.api.observability.NfrMetrics;

/**
 * Contract stability for the CI release-gate endpoint (Part 8).
 */
@SpringBootTest
@AutoConfigureMockMvc
class CiReleaseGateControllerTest {
  private static final String DEFAULT_API_KEY = "00000000-0000-0000-0000-000000000a01";

  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;
  @Autowired MeterRegistry meterRegistry;

  @Test
  void ciReleaseGateContractPassReviewBlockedAndExitCodes() throws Exception {
    String passId = createSystem(90, 90, "HEALTHY", "[]");
    mockMvc.perform(get("/api/v1/ci/release-gate")
            .param("systemId", passId)
            .header("X-Api-Key", DEFAULT_API_KEY))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.systemId").value(passId))
        .andExpect(jsonPath("$.systemName").isString())
        .andExpect(jsonPath("$.decision").value("PASS"))
        .andExpect(jsonPath("$.blockers", hasSize(0)))
        .andExpect(jsonPath("$.evalScore").value(90))
        .andExpect(jsonPath("$.evidenceCoverage").value(90))
        .andExpect(jsonPath("$.dataContractStatus").value("HEALTHY"))
        .andExpect(jsonPath("$.riskClass").value("LIMITED"))
        .andExpect(jsonPath("$.exitCode").value(0))
        .andExpect(jsonPath("$.content").isString());

    String reviewId = createSystem(80, 80, "HEALTHY", "[]");
    mockMvc.perform(get("/api/v1/ci/release-gate")
            .param("systemId", reviewId)
            .header("X-Api-Key", DEFAULT_API_KEY))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.decision").value("REVIEW"))
        .andExpect(jsonPath("$.exitCode").value(2));

    String blockedId = createSystem(70, 90, "BREACH", "[]");
    mockMvc.perform(get("/api/v1/ci/release-gate")
            .param("systemId", blockedId)
            .header("X-Api-Key", DEFAULT_API_KEY))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.decision").value("BLOCKED"))
        .andExpect(jsonPath("$.exitCode").value(1))
        .andExpect(jsonPath("$.blockers.length()", greaterThan(0)));

    assertThat(meterRegistry.find(AssuranceMetrics.RELEASE_GATE_DECISION)
            .tag("decision", "PASS")
            .counter())
        .isNotNull();
    assertThat(meterRegistry.find(AssuranceMetrics.RELEASE_GATE_DECISION)
            .tag("decision", "PASS")
            .counter()
            .count())
        .isGreaterThanOrEqualTo(1.0);
  }

  @Test
  void ciReleaseGateRequiresAuthAndRejectsMissingSystem() throws Exception {
    mockMvc.perform(get("/api/v1/ci/release-gate")
            .param("systemId", UUID.randomUUID().toString()))
        .andExpect(status().isUnauthorized());

    mockMvc.perform(get("/api/v1/ci/release-gate")
            .param("systemId", UUID.randomUUID().toString())
            .header("X-Api-Key", DEFAULT_API_KEY))
        .andExpect(status().isNotFound());
  }

  @Test
  void nfrLatencyTimersAndPart8CountersExistAfterTraffic() throws Exception {
    String systemId = createSystem(90, 90, "HEALTHY", "[]");

    mockMvc.perform(get("/api/v1/systems")
            .header("X-Api-Key", DEFAULT_API_KEY))
        .andExpect(status().isOk());
    mockMvc.perform(get("/api/v1/systems/{id}", systemId)
            .header("X-Api-Key", DEFAULT_API_KEY))
        .andExpect(status().isOk());
    mockMvc.perform(get("/api/v1/ci/release-gate")
            .param("systemId", systemId)
            .header("X-Api-Key", DEFAULT_API_KEY))
        .andExpect(status().isOk());

    // Timers/counters exist after traffic — no p95 SLO assertion (docs/NFR.md)
    assertThat(meterRegistry.find(NfrMetrics.REGISTRY_READ_TIMER).timer()).isNotNull();
    assertThat(meterRegistry.find(NfrMetrics.REGISTRY_READ_TIMER).timer().count())
        .isGreaterThanOrEqualTo(1L);
    assertThat(meterRegistry.find(AssuranceMetrics.RELEASE_GATE_DECISION).counter()).isNotNull();
    assertThat(meterRegistry.find(AssuranceMetrics.AUDIT_APPEND).counter()).isNotNull();
    assertThat(meterRegistry.find(AssuranceMetrics.AUDIT_APPEND).counter().count())
        .isGreaterThanOrEqualTo(1.0);
  }

  @Test
  void exitCodeMappingIsStable() {
    assertThat(CiReleaseGateResponse.exitCodeFor(ReleaseDecision.PASS)).isEqualTo(0);
    assertThat(CiReleaseGateResponse.exitCodeFor(ReleaseDecision.BLOCKED)).isEqualTo(1);
    assertThat(CiReleaseGateResponse.exitCodeFor(ReleaseDecision.REVIEW)).isEqualTo(2);
  }

  private String createSystem(
      int evalScore, int evidenceCoverage, String dataContractStatus, String openGapsJson)
      throws Exception {
    MvcResult result = mockMvc.perform(post("/api/v1/systems")
            .header("X-Api-Key", DEFAULT_API_KEY)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "name": "CI Gate System %s",
                  "owner": "CI",
                  "purpose": "Contract test system",
                  "riskClass": "LIMITED",
                  "riskBasis": "Customer-facing assistant",
                  "deploymentRegion": "EU",
                  "evidenceCoverage": %d,
                  "evalScore": %d,
                  "dataContractStatus": "%s",
                  "openGaps": %s
                }
                """.formatted(UUID.randomUUID(), evidenceCoverage, evalScore, dataContractStatus, openGapsJson)))
        .andExpect(status().isCreated())
        .andReturn();
    JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
    return body.get("id").asText();
  }
}
