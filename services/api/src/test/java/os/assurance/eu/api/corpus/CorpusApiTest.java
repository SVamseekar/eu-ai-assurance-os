package os.assurance.eu.api.corpus;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import os.assurance.eu.api.auth.JwtService;
import os.assurance.eu.api.tenant.TenantContext;
import os.assurance.eu.api.tenant.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@SpringBootTest(properties = {
    "assurance.eval.worker.enabled=false",
    "assurance.eval.callback.secret=test-eval-callback-secret",
    "assurance.corpus.as-of=2026-09-23"
})
@AutoConfigureMockMvc
class CorpusApiTest {
  @Autowired MockMvc mockMvc;
  @Autowired JwtService jwtService;

  @Test
  void englishFormexCorpusStoresForceDatesAndKeepsGuidanceSeparate() throws Exception {
    mockMvc.perform(get("/api/v1/corpus").with(compliance()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.corpusVersion").isString())
        .andExpect(jsonPath("$.corpusVersion").value(org.hamcrest.Matchers.matchesPattern("[0-9a-f]{64}")))
        .andExpect(jsonPath("$.instruments[?(@.seedCelex=='32024R1689')].consolidationCelex")
            .value(org.hamcrest.Matchers.hasItem("02024R1689-20260727")))
        .andExpect(jsonPath("$.instruments[?(@.seedCelex=='32026R1744')]").exists())
        .andExpect(jsonPath("$.instruments[?(@.seedCelex=='32016R0679')]").exists())
        .andExpect(jsonPath("$.instruments[?(@.seedCelex=='32022R2554')]").exists())
        .andExpect(jsonPath("$.instruments[?(@.seedCelex=='32022R2065')]").exists())
        .andExpect(jsonPath("$.instruments[?(@.seedCelex=='32023R2854')]").exists())
        .andExpect(jsonPath("$.instruments[?(@.seedCelex=='edpb-gdpr-guidelines')]").isEmpty())
        .andExpect(jsonPath("$.provisions[?(@.provisionKey=='02024R1689-20260727#annexIII::')].forceStatus")
            .value(org.hamcrest.Matchers.hasItem("FUTURE")))
        .andExpect(jsonPath("$.provisions[?(@.provisionKey=='02024R1689-20260727#annexIII::')].forceFrom")
            .value(org.hamcrest.Matchers.hasItem("2027-12-02")))
        .andExpect(jsonPath("$.provisions[?(@.provisionKey=='02023R2854-20231222#3:1:')].forceFrom")
            .value(org.hamcrest.Matchers.hasItem("2026-09-12")))
        .andExpect(jsonPath("$.provisions[?(@.provisionKey=='02023R2854-20231222#3:1:')].scopeNote")
            .value(org.hamcrest.Matchers.hasItem(
                org.hamcrest.Matchers.containsString("placed on the market after 12 Sep 2026"))))
        .andExpect(jsonPath("$.guidance[0].relation").value("interprets"))
        .andExpect(jsonPath("$.guidance[0].sourceKey").value("edpb-gdpr-guidelines"));
  }

  private RequestPostProcessor compliance() {
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
