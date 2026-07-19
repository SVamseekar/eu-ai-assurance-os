package os.assurance.eu.api.sector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
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
    "assurance.sector.packs=insurance,hr,finance",
    "spring.jackson.mapper.accept-case-insensitive-enums=true"
})
@AutoConfigureMockMvc
class SectorPackApiTest {
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
  void listsThreeSectorPacksWithHonestMetricsLabel() throws Exception {
    mockMvc.perform(get("/api/v1/sector-packs").with(authenticated()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.metricsLabel").value("3 sector packs + SPI"))
        .andExpect(jsonPath("$.notAllIndustriesNote", containsString("not")))
        .andExpect(jsonPath("$.disclaimer", containsString("not live production connectors")))
        .andExpect(jsonPath("$.packs.length()").value(3))
        .andExpect(jsonPath("$.packs[*].id", hasItem("insurance")))
        .andExpect(jsonPath("$.packs[*].id", hasItem("hr")))
        .andExpect(jsonPath("$.packs[*].id", hasItem("finance")));
  }

  @Test
  void insuranceSystemGetsExtraSectorControls() throws Exception {
    String systemId = createSystem("Claims Triage AI", "HIGH", "insurance");

    MvcResult result = mockMvc.perform(get("/api/v1/systems/{id}/controls", systemId)
            .with(authenticated()))
        .andExpect(status().isOk())
        .andReturn();

    JsonNode controls = objectMapper.readTree(result.getResponse().getContentAsString());
    assertThat(controls.isArray()).isTrue();
    assertThat(controlCodes(controls))
        .contains(
            "INS_CLAIMS_FAIRNESS",
            "INS_ADVERSE_DECISION_REVIEW",
            "INS_CLAIMS_EXPLAINABILITY",
            "INS_MODEL_CARD_CLAIMS")
        .contains("HUMAN_OVERSIGHT", "RISK_MANAGEMENT");
  }

  @Test
  void nonInsuranceSystemDoesNotGetInsuranceControls() throws Exception {
    String systemId = createSystem("Generic Helper", "HIGH", "other");

    MvcResult result = mockMvc.perform(get("/api/v1/systems/{id}/controls", systemId)
            .with(authenticated()))
        .andExpect(status().isOk())
        .andReturn();

    JsonNode controls = objectMapper.readTree(result.getResponse().getContentAsString());
    assertThat(controlCodes(controls))
        .doesNotContain(
            "INS_CLAIMS_FAIRNESS",
            "HR_HIRING_TRANSPARENCY",
            "FIN_KYC_LOGGING")
        .contains("HUMAN_OVERSIGHT");
  }

  @Test
  void hrSystemGetsHrPackControls() throws Exception {
    String systemId = createSystem("HR Candidate Screener", "HIGH", "hr");

    MvcResult result = mockMvc.perform(get("/api/v1/systems/{id}/controls", systemId)
            .with(authenticated()))
        .andExpect(status().isOk())
        .andReturn();

    assertThat(controlCodes(objectMapper.readTree(result.getResponse().getContentAsString())))
        .contains("HR_HIRING_TRANSPARENCY", "HR_HUMAN_OVERSIGHT_EMPLOYMENT", "HR_CANDIDATE_NOTICE")
        .doesNotContain("INS_CLAIMS_FAIRNESS");
  }

  @Test
  void financeSystemGetsFinancePackControls() throws Exception {
    String systemId = createSystem("KYC Copilot", "HIGH", "finance");

    MvcResult result = mockMvc.perform(get("/api/v1/systems/{id}/controls", systemId)
            .with(authenticated()))
        .andExpect(status().isOk())
        .andReturn();

    assertThat(controlCodes(objectMapper.readTree(result.getResponse().getContentAsString())))
        .contains("FIN_KYC_LOGGING", "FIN_FRAUD_FALSE_POSITIVE_REVIEW", "FIN_CREDIT_EXPLAINABILITY");
  }

  @Test
  void claimsModelRegisterStubCreatesInsuranceSystemWithPackControls() throws Exception {
    MvcResult create = mockMvc.perform(post("/api/v1/integrations/insurance/claims-model-register")
            .with(authenticated())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "externalModelId": "guidewire-claims-model-42",
                  "name": "External Claims Triage Model",
                  "owner": "Claims Ops",
                  "vendorName": "ExampleVendor",
                  "modelName": "ClaimsTriage",
                  "modelVersion": "2.1.0",
                  "dataSources": ["claims_db"],
                  "decisionImpact": "eligibility"
                }
                """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.sector").value("insurance"))
        .andExpect(jsonPath("$.packId").value("insurance"))
        .andExpect(jsonPath("$.externalModelId").value("guidewire-claims-model-42"))
        .andExpect(jsonPath("$.note", containsString("Stub")))
        .andExpect(jsonPath("$.note", not(containsString("all industries"))))
        .andReturn();

    String systemId = objectMapper.readTree(create.getResponse().getContentAsString())
        .path("systemId").asText();

    MvcResult controls = mockMvc.perform(get("/api/v1/systems/{id}/controls", systemId)
            .with(authenticated()))
        .andExpect(status().isOk())
        .andReturn();
    assertThat(controlCodes(objectMapper.readTree(controls.getResponse().getContentAsString())))
        .contains("INS_CLAIMS_FAIRNESS", "INS_ADVERSE_DECISION_REVIEW");
  }

  @Test
  void loadsInsuranceTemplateMarkdown() throws Exception {
    mockMvc.perform(get("/api/v1/sector-packs/insurance/templates/insurance-dpia")
            .with(authenticated()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.packId").value("insurance"))
        .andExpect(jsonPath("$.documentType").value("DPIA"))
        .andExpect(jsonPath("$.contentMarkdown", containsString("DPIA")))
        .andExpect(jsonPath("$.disclaimer", containsString("not live production connectors")));
  }

  @Test
  void connectorInventoryIsStubOnly() throws Exception {
    mockMvc.perform(get("/api/v1/integrations/connectors/model-inventory").with(authenticated()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.connectorMode").value("stub"))
        .andExpect(jsonPath("$.metricsLabel").value("3 sector packs + SPI"))
        .andExpect(jsonPath("$.models").isArray());
  }

  private String createSystem(String name, String riskClass, String sector) throws Exception {
    MvcResult result = mockMvc.perform(post("/api/v1/systems")
            .with(authenticated())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "name": "%s",
                  "owner": "Ops",
                  "purpose": "Test system for sector packs",
                  "riskClass": "%s",
                  "riskBasis": "Test basis",
                  "deploymentRegion": "EU",
                  "evidenceCoverage": 90,
                  "evalScore": 90,
                  "dataContractStatus": "HEALTHY",
                  "openGaps": [],
                  "sector": "%s",
                  "decisionImpact": "eligibility",
                  "affectedUsers": ["users"]
                }
                """.formatted(name, riskClass, sector)))
        .andExpect(status().isCreated())
        .andReturn();
    return objectMapper.readTree(result.getResponse().getContentAsString()).path("id").asText();
  }

  private static java.util.List<String> controlCodes(JsonNode controls) {
    java.util.List<String> codes = new java.util.ArrayList<>();
    for (JsonNode node : controls) {
      codes.add(node.path("controlCode").asText());
    }
    return codes;
  }

  private RequestPostProcessor authenticated() {
    return request -> {
      request.addHeader("X-Api-Key", DEFAULT_API_KEY);
      return request;
    };
  }
}
