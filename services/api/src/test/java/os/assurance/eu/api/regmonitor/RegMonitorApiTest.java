package os.assurance.eu.api.regmonitor;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    "assurance.reg-monitor.enabled=false",
    "assurance.reg-monitor.bootstrap-fixtures=true",
    "assurance.eval.callback.secret=test-eval-callback-secret",
    "spring.jackson.mapper.accept-case-insensitive-enums=true"
})
@AutoConfigureMockMvc
class RegMonitorApiTest {
  private static final String DEFAULT_API_KEY = "00000000-0000-0000-0000-000000000a01";
  private static final String DEFAULT_TENANT_ID = "00000000-0000-0000-0000-000000000001";
  private static final String DEFAULT_ACTOR_ID = "00000000-0000-0000-0000-000000000101";

  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;
  @Autowired ApiKeyJpaRepository apiKeyRepo;
  @Autowired RegMonitorIngestionService ingestionService;
  @Autowired RegItemJpaRepository items;

  @BeforeEach
  void seedApiKeyAndFixtures() {
    Instant now = Instant.now();
    UUID apiKeyId = UUID.fromString(DEFAULT_API_KEY);
    apiKeyRepo.findById(apiKeyId)
        .orElseGet(() -> apiKeyRepo.save(new ApiKeyEntity(
            apiKeyId,
            ApiKeyHasher.sha256Hex(DEFAULT_API_KEY),
            UUID.fromString(DEFAULT_TENANT_ID),
            UUID.fromString(DEFAULT_ACTOR_ID),
            now)));
    if (items.count() == 0) {
      ingestionService.ensureBootstrapFixtures();
    }
  }

  @Test
  void listItemsIncludesDisclaimerAndUncertainHints() throws Exception {
    mockMvc.perform(get("/api/v1/reg-monitor/items").with(authenticated()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.productLabel", containsString("Regulatory change")))
        .andExpect(jsonPath("$.disclaimer", containsString("not an official")))
        .andExpect(jsonPath("$.latencyNote", containsString("poll interval")))
        .andExpect(jsonPath("$.items.length()", greaterThanOrEqualTo(1)))
        .andExpect(jsonPath("$.items[0].impactHints[0].impactLevel").value("UNCERTAIN"));
  }

  @Test
  void markReviewedIsTenantScopedAndDoesNotClaimCertification() throws Exception {
    MvcResult list = mockMvc.perform(get("/api/v1/reg-monitor/items").with(authenticated()))
        .andExpect(status().isOk())
        .andReturn();
    JsonNode root = objectMapper.readTree(list.getResponse().getContentAsString());
    String itemId = root.path("items").get(0).path("id").asText();

    mockMvc.perform(post("/api/v1/reg-monitor/items/{id}/review", itemId)
            .with(authenticated())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"notes\":\"Human triage only\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.reviewed").value(true))
        .andExpect(jsonPath("$.reviewNotes").value("Human triage only"))
        .andExpect(jsonPath("$.disclaimer", containsString("Not an official")));
  }

  @Test
  void relevantEndpointForSystemReturnsFeedShape() throws Exception {
    MvcResult created = mockMvc.perform(post("/api/v1/systems")
            .with(authenticated())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "name": "Claims Triage Reg Test",
                  "owner": "Insurance Ops",
                  "purpose": "Route insurance claims",
                  "riskClass": "high",
                  "riskBasis": "Essential private services",
                  "deploymentRegion": "EU",
                  "sector": "insurance",
                  "decisionImpact": "eligibility"
                }
                """))
        .andExpect(status().isCreated())
        .andReturn();
    String systemId = objectMapper.readTree(created.getResponse().getContentAsString())
        .path("id").asText();

    mockMvc.perform(get("/api/v1/systems/{id}/reg-monitor/relevant", systemId)
            .with(authenticated()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.productLabel", containsString("Regulatory")))
        .andExpect(jsonPath("$.disclaimer", containsString("not an official")))
        .andExpect(jsonPath("$.items").isArray());
  }

  private RequestPostProcessor authenticated() {
    return request -> {
      request.addHeader("X-Api-Key", DEFAULT_API_KEY);
      request.addHeader("X-Tenant-Id", DEFAULT_TENANT_ID);
      request.addHeader("X-Actor-Id", DEFAULT_ACTOR_ID);
      return request;
    };
  }
}
