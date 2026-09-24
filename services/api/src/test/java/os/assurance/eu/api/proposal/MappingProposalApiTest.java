package os.assurance.eu.api.proposal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import os.assurance.eu.api.auth.JwtService;
import os.assurance.eu.api.tenant.TenantContext;
import os.assurance.eu.api.tenant.UserRole;
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
    "assurance.corpus.as-of=2026-09-23"
})
@AutoConfigureMockMvc
class MappingProposalApiTest {
  private static final UUID ENGINEER = UUID.fromString("00000000-0000-0000-0000-000000000102");
  private static final UUID ADMIN = UUID.fromString("00000000-0000-0000-0000-000000000105");

  @Autowired MockMvc mockMvc;
  @Autowired JwtService jwtService;
  @Autowired ObjectMapper objectMapper;

  @Test
  void officerAcceptsAndRejectsIntoTheHashChainAndEngineerIsForbidden() throws Exception {
    String systemId = firstSystemId();
    MvcResult empty = mockMvc.perform(get("/api/v1/systems/{id}/proposals", systemId).with(officer()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isArray())
        .andReturn();
    assertThat(objectMapper.readTree(empty.getResponse().getContentAsString()).get("items")).isEmpty();

    String supports = create(systemId, """
        {"relation":"supports","provisionKey":"02016R0679-20160504#5:1:","excerpt":"Lawful processing."}
        """);
    String abstain = create(systemId, """
        {"relation":"abstain","excerpt":"No link proposed."}
        """);
    String mismatch = create(systemId, """
        {"relation":"supports","provisionKey":"02016R0679-20160504#5:1:","excerpt":"Stale pin.","corpusVersion":"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"}
        """);

    mockMvc.perform(get("/api/v1/systems/{id}/proposals", systemId).with(officer()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items[?(@.id=='%s')].displayState".formatted(abstain))
            .value(org.hamcrest.Matchers.hasItem("abstain")))
        .andExpect(jsonPath("$.items[?(@.id=='%s')].displayState".formatted(mismatch))
            .value(org.hamcrest.Matchers.hasItem("corpus_mismatch")))
        .andExpect(jsonPath("$.items[?(@.id=='%s')].pinnedCorpusVersion".formatted(mismatch)).exists())
        .andExpect(jsonPath("$.currentCorpusVersion").isString());

    mockMvc.perform(post("/api/v1/systems/{id}/proposals/{proposalId}/accept", systemId, mismatch)
            .with(officer()))
        .andExpect(status().isConflict());
    mockMvc.perform(post("/api/v1/systems/{id}/proposals/{proposalId}/accept", systemId, abstain)
            .with(officer()))
        .andExpect(status().isConflict());
    mockMvc.perform(post("/api/v1/systems/{id}/proposals/{proposalId}/accept", systemId, supports)
            .with(engineer()))
        .andExpect(status().isForbidden());

    mockMvc.perform(post("/api/v1/systems/{id}/proposals/{proposalId}/accept", systemId, supports)
            .with(officer()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("ACCEPTED"));
    mockMvc.perform(post("/api/v1/systems/{id}/proposals/{proposalId}/reject", systemId, abstain)
            .with(admin()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("REJECTED"));

    mockMvc.perform(get("/api/v1/audit-events").with(officer()).param("systemId", systemId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.eventType=='mapping_proposal.accepted')].payload.corpusVersion").isNotEmpty())
        .andExpect(jsonPath("$[?(@.eventType=='mapping_proposal.rejected')].payload.corpusVersion").isNotEmpty());
    mockMvc.perform(get("/api/v1/audit-events/verify-chain").with(officer()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.valid").value(true));
  }

  private String firstSystemId() throws Exception {
    MvcResult listed = mockMvc.perform(get("/api/v1/systems").with(officer()))
        .andExpect(status().isOk())
        .andReturn();
    JsonNode systems = objectMapper.readTree(listed.getResponse().getContentAsString());
    assertThat(systems.isArray()).isTrue();
    assertThat(systems.size()).isPositive();
    return systems.get(0).get("id").asText();
  }

  private String create(String systemId, String body) throws Exception {
    MvcResult created = mockMvc.perform(post("/api/v1/systems/{id}/proposals", systemId)
            .with(officer())
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isCreated())
        .andReturn();
    return objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText();
  }

  private RequestPostProcessor officer() {
    return bearer(TenantContext.DEFAULT_USER_ID, UserRole.COMPLIANCE_OFFICER);
  }

  private RequestPostProcessor engineer() {
    return bearer(ENGINEER, UserRole.AI_ENGINEERING_LEAD);
  }

  private RequestPostProcessor admin() {
    return bearer(ADMIN, UserRole.ADMIN);
  }

  private RequestPostProcessor bearer(UUID userId, UserRole role) {
    String token = jwtService.issueAccessToken(userId, TenantContext.DEFAULT_TENANT_ID, role);
    return request -> {
      request.addHeader("Authorization", "Bearer " + token);
      return request;
    };
  }
}
