package os.assurance.eu.api.proposal;

import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import os.assurance.eu.api.auth.JwtService;
import os.assurance.eu.api.tenant.TenantContext;
import os.assurance.eu.api.tenant.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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
class ProposalGovernanceAuditTest {
  private static final String PROVISION = "02016R0679-20160504#5:1:";

  @Autowired MockMvc mockMvc;
  @Autowired JwtService jwtService;
  @Autowired ObjectMapper objectMapper;

  @Test
  void decisionsAreAuditedAndBadInputsAreRejected() throws Exception {
    String systemId = createSystem();
    String proposalId = createProposal(systemId, """
        {"relation":"supports","provisionKey":"%s","excerpt":"Lawful processing."}
        """.formatted(PROVISION));
    mockMvc.perform(post("/api/v1/systems/{id}/proposals/{proposalId}/accept", systemId, proposalId)
            .with(officer()))
        .andExpect(status().isOk());
    mockMvc.perform(put("/api/v1/systems/{id}/proposals/{proposalId}/mode", systemId, proposalId)
            .with(officer())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"mode\":\"APPROVAL_REQUIRED\"}"))
        .andExpect(status().isOk());

    mockMvc.perform(get("/api/v1/audit-events").with(officer()).param("systemId", systemId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].eventType", hasItems(
            "mapping_proposal.created",
            "mapping_proposal.accepted",
            "mapping_proposal.mode_changed")))
        .andExpect(jsonPath("$[?(@.eventType=='mapping_proposal.mode_changed')].payload.from").isNotEmpty())
        .andExpect(jsonPath("$[?(@.eventType=='mapping_proposal.mode_changed')].payload.to").isNotEmpty());

    mockMvc.perform(post("/api/v1/systems/{id}/proposals", systemId)
            .with(officer())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"relation\":\"supports\",\"provisionKey\":\"not-in-corpus\",\"excerpt\":\"x\"}"))
        .andExpect(status().isBadRequest());

    mockMvc.perform(post("/api/v1/systems/{id}/proposals", systemId)
            .with(officer())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"relation\":\"supports\",\"provisionKey\":\"" + PROVISION + "\",\"excerpt\":\""
                + "x".repeat(5000) + "\"}"))
        .andExpect(status().isBadRequest());
  }

  private String createSystem() throws Exception {
    MvcResult created = mockMvc.perform(post("/api/v1/systems")
            .with(officer())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"name":"Governed system","owner":"Reviewer","purpose":"Audit decisions",
                 "riskClass":"HIGH","riskBasis":"Annex III","deploymentRegion":"EU"}
                """))
        .andExpect(status().isCreated())
        .andReturn();
    return objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText();
  }

  private String createProposal(String systemId, String body) throws Exception {
    MvcResult created = mockMvc.perform(post("/api/v1/systems/{id}/proposals", systemId)
            .with(officer())
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isCreated())
        .andReturn();
    return objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText();
  }

  private RequestPostProcessor officer() {
    String token = jwtService.issueAccessToken(
        TenantContext.DEFAULT_USER_ID, TenantContext.DEFAULT_TENANT_ID, UserRole.COMPLIANCE_OFFICER);
    return request -> {
      request.addHeader("Authorization", "Bearer " + token);
      return request;
    };
  }
}
