package os.assurance.eu.api.publicclaims;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.UUID;
import os.assurance.eu.api.tenant.ApiKeyEntity;
import os.assurance.eu.api.tenant.ApiKeyHasher;
import os.assurance.eu.api.tenant.ApiKeyJpaRepository;
import os.assurance.eu.api.tenant.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@SpringBootTest(properties = {
    "assurance.eval.worker.enabled=false",
    "assurance.eval.callback.secret=test-eval-callback-secret",
    "assurance.demo.public-claims=true"
})
@AutoConfigureMockMvc
class PublicClaimsSeedingApiTest {
  private static final String DEFAULT_API_KEY = "00000000-0000-0000-0000-000000000a01";

  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;
  @Autowired ApiKeyJpaRepository apiKeyRepo;

  @BeforeEach
  void seedApiKey() {
    UUID apiKeyId = UUID.fromString(DEFAULT_API_KEY);
    apiKeyRepo.findById(apiKeyId)
        .orElseGet(() -> apiKeyRepo.save(new ApiKeyEntity(
            apiKeyId,
            ApiKeyHasher.sha256Hex(DEFAULT_API_KEY),
            TenantContext.DEFAULT_TENANT_ID,
            TenantContext.DEFAULT_USER_ID,
            Instant.now())));
  }

  @Test
  void seededTeasersAreBlockedAndExportHonestEvgraphFiles() throws Exception {
    MvcResult index = mockMvc.perform(get("/api/v1/public-claims").with(authenticated()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.seedingEnabled").value(true))
        .andExpect(jsonPath("$.systems.length()").value(4))
        .andReturn();
    JsonNode systems = objectMapper.readTree(index.getResponse().getContentAsString()).get("systems");
    String getsafeId = null;
    for (JsonNode system : systems) {
      org.assertj.core.api.Assertions.assertThat(system.get("registeredSystemId").isNull()).isFalse();
      org.assertj.core.api.Assertions.assertThat(system.get("releaseDecision").asText()).isEqualTo("BLOCKED");
      org.assertj.core.api.Assertions.assertThat(system.get("purpose").asText()).contains("NOT A CUSTOMER");
      if ("getsafe".equals(system.get("slug").asText())) {
        getsafeId = system.get("registeredSystemId").asText();
      }
    }
    org.assertj.core.api.Assertions.assertThat(getsafeId).isNotBlank();

    mockMvc.perform(get("/api/v1/systems").with(authenticated()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].name", hasItem("[Public claims] Getsafe claims agents")))
        .andExpect(jsonPath("$[*].modelName", hasItem("getsafe-claims-agents-public")));

    mockMvc.perform(get("/api/v1/systems/{id}/release-gate", getsafeId).with(authenticated()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.decision").value("BLOCKED"));

    mockMvc.perform(get("/api/v1/systems/{id}/evgraph-artifacts", getsafeId).with(authenticated()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.model_card.model_name").value("getsafe-claims-agents-public"))
        .andExpect(jsonPath("$.approval.approved_at").doesNotExist())
        .andExpect(jsonPath("$.disclaimer", containsString("not customers")));

    mockMvc.perform(get("/api/v1/evidence/systems/{id}/documents", getsafeId).with(authenticated()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].title", containsString("Getsafe")))
        .andExpect(jsonPath("$[0].type").value("PUBLIC_CLAIMS"));
  }

  private RequestPostProcessor authenticated() {
    return request -> {
      request.addHeader("X-Api-Key", DEFAULT_API_KEY);
      return request;
    };
  }
}
