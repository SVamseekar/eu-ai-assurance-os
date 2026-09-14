package os.assurance.eu.api.publicclaims;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@SpringBootTest(properties = {
    "assurance.eval.worker.enabled=false",
    "assurance.eval.callback.secret=test-eval-callback-secret",
    "assurance.demo.public-claims=false"
})
@AutoConfigureMockMvc
class PublicClaimsApiTest {
  private static final String DEFAULT_API_KEY = "00000000-0000-0000-0000-000000000a01";

  @Autowired MockMvc mockMvc;
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
  void catalogListsFourTeasersWithoutSeedingNamedOrgsAsCustomers() throws Exception {
    mockMvc.perform(get("/api/v1/public-claims").with(authenticated()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.disclaimer", containsString("not customers")))
        .andExpect(jsonPath("$.seedingEnabled").value(false))
        .andExpect(jsonPath("$.library", containsString("evgraph")))
        .andExpect(jsonPath("$.howtoPromotion", containsString("--gate --strict")))
        .andExpect(jsonPath("$.systems.length()").value(4))
        .andExpect(jsonPath("$.systems[*].slug", hasItem("getsafe")))
        .andExpect(jsonPath("$.systems[*].slug", hasItem("auxmoney")))
        .andExpect(jsonPath("$.systems[*].slug", hasItem("softgarden")))
        .andExpect(jsonPath("$.systems[*].slug", hasItem("retorio")))
        .andExpect(jsonPath("$.systems[0].registeredSystemId", nullValue()));
  }

  @Test
  void evgraphArtifactsAreHonestPublicReconstructions() throws Exception {
    mockMvc.perform(get("/api/v1/public-claims/getsafe/evgraph").with(authenticated()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.model_card.model_name").value("getsafe-claims-agents-public"))
        .andExpect(jsonPath("$.approval.approver").value("not-published-in-public-materials"))
        .andExpect(jsonPath("$.approval.approved_at").doesNotExist())
        .andExpect(jsonPath("$.deployment.deployed_at").value("2025-06-05T00:00:00Z"))
        .andExpect(jsonPath("$.dataset_manifest_csv", containsString("customer-interactions-public-claim")))
        .andExpect(jsonPath("$.disclaimer", containsString("Not a legal finding")));

    mockMvc.perform(get("/api/v1/public-claims/softgarden/evgraph").with(authenticated()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.deployment.deployed_at").doesNotExist())
        .andExpect(jsonPath("$.approval.approved_at").doesNotExist());
  }

  @Test
  void unknownSlugIsNotFound() throws Exception {
    mockMvc.perform(get("/api/v1/public-claims/allianz").with(authenticated()))
        .andExpect(status().isNotFound());
  }

  @Test
  void unauthenticatedCatalogIsRejected() throws Exception {
    mockMvc.perform(get("/api/v1/public-claims"))
        .andExpect(status().isUnauthorized());
  }

  private RequestPostProcessor authenticated() {
    return request -> {
      request.addHeader("X-Api-Key", DEFAULT_API_KEY);
      return request;
    };
  }
}
