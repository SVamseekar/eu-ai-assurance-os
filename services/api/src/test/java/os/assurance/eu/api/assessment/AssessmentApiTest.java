package os.assurance.eu.api.assessment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Locale;
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
class AssessmentApiTest {
  private static final UUID ENGINEER = UUID.fromString("00000000-0000-0000-0000-000000000102");

  @Autowired MockMvc mockMvc;
  @Autowired JwtService jwtService;
  @Autowired ObjectMapper objectMapper;

  @Test
  void assessmentCountsMatchThePackForTheSameCorpusVersion() throws Exception {
    String systemId = createSystem(90);
    indexEvidence(systemId);
    String gdpr = proposal(systemId, "supports", "02016R0679-20160504#5:1:");
    accept(systemId, gdpr);
    String abstain = proposal(systemId, "abstain", null);
    String pending = proposal(systemId, "supports", "02016R0679-20160504#6:1:");
    String rejected = proposal(systemId, "supports", "02022R2554-20221227#5:1:");
    reject(systemId, rejected);

    JsonNode assessment = assessment(systemId);
    JsonNode pack = pack(systemId);
    assertThat(assessment.get("corpusVersion").asText()).isEqualTo(pack.get("corpusVersion").asText());
    for (String key : new String[] {
        "satisfied", "insufficient", "missing", "needsHumanReview", "notApplicable", "acceptedException"}) {
      assertThat(pack.get("evidenceCounts").get(key).asInt())
          .isEqualTo(assessment.get("counts").get(key).asInt());
    }
    assertThat(statusOf(assessment, gdpr)).isEqualTo("SATISFIED");
    assertThat(statusOf(assessment, abstain)).isEqualTo("NEEDS_HUMAN_REVIEW");
    assertThat(statusOf(assessment, pending)).isEqualTo("MISSING");
    assertThat(statusOf(assessment, rejected)).isEqualTo("MISSING");
    String packBody = pack.toString().toLowerCase(Locale.ROOT);
    assertThat(packBody).doesNotContain("compliant");
    assertThat(packBody).doesNotContain("certified");
    assertThat(packBody).doesNotContain("conformity");

    JsonNode queue = pack.get("queue");
    assertThat(labels(queue)).contains("PENDING", "REJECTED", "abstain");
  }

  @Test
  void notApplicableCannotBecomeSatisfied() throws Exception {
    String systemId = createSystem(90);
    indexEvidence(systemId);
    String id = proposal(systemId, "supports", "02016R0679-20160504#5:1:");
    accept(systemId, id);
    mockMvc.perform(put("/api/v1/systems/{id}/assessment/{proposalId}", systemId, id)
            .with(officer())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"applicability":"NOT_APPLICABLE","reviewerId":"%s"}
                """.formatted(TenantContext.DEFAULT_USER_ID)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.applicability").value("NOT_APPLICABLE"))
        .andExpect(jsonPath("$.derivedStatus").value("NOT_APPLICABLE"));

    mockMvc.perform(post("/api/v1/systems/{id}/assessment/{proposalId}/satisfied", systemId, id)
            .with(officer()))
        .andExpect(status().isConflict());
    assertThat(statusOf(assessment(systemId), id)).isEqualTo("NOT_APPLICABLE");
  }

  @Test
  void engineeringLeadCannotWriteAnException() throws Exception {
    String systemId = createSystem(90);
    String id = proposal(systemId, "supports", "02016R0679-20160504#5:1:");
    mockMvc.perform(post("/api/v1/systems/{id}/assessment/exceptions", systemId)
            .with(engineer())
            .contentType(MediaType.APPLICATION_JSON)
            .content(exceptionBody(id, LocalDate.now(ZoneOffset.UTC).plusDays(2))))
        .andExpect(status().isForbidden());
  }

  @Test
  void exceptionIsMissingTheDayAfterExpiry() throws Exception {
    String systemId = createSystem(90);
    String id = proposal(systemId, "supports", "02016R0679-20160504#5:1:");
    LocalDate yesterday = LocalDate.now(ZoneOffset.UTC).minusDays(1);
    mockMvc.perform(post("/api/v1/systems/{id}/assessment/exceptions", systemId)
            .with(officer())
            .contentType(MediaType.APPLICATION_JSON)
            .content(exceptionBody(id, yesterday)))
        .andExpect(status().isCreated());
    assertThat(statusOf(assessment(systemId), id)).isEqualTo("MISSING");

    String openId = proposal(systemId, "supports", "02016R0679-20160504#6:1:");
    mockMvc.perform(post("/api/v1/systems/{id}/assessment/exceptions", systemId)
            .with(officer())
            .contentType(MediaType.APPLICATION_JSON)
            .content(exceptionBody(openId, LocalDate.now(ZoneOffset.UTC))))
        .andExpect(status().isCreated());
    assertThat(statusOf(assessment(systemId), openId)).isEqualTo("ACCEPTED_EXCEPTION");
  }

  @Test
  void acceptedAnnexIiiStaysFutureAndDoesNotBlockACleanGate() throws Exception {
    String systemId = createSystem(90);
    mockMvc.perform(post("/api/v1/systems/{id}/proposals/map", systemId)
            .with(officer())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"documents":[{"title":"Model card","text":"Model card for a credit-scoring assistant. The system is a high-risk AI system listed in Annex III."}]}
                """))
        .andExpect(status().isCreated());
    String annexId = null;
    for (JsonNode item : proposals(systemId)) {
      if ("FUTURE".equals(text(item, "forceStatus")) && "2027-12-02".equals(text(item, "forceFrom"))) {
        annexId = item.get("id").asText();
      }
    }
    assertThat(annexId).isNotBlank();
    accept(systemId, annexId);

    JsonNode row = item(assessment(systemId), annexId);
    assertThat(text(row, "forceStatus")).isEqualTo("FUTURE");
    assertThat(text(row, "derivedStatus")).isNotEqualTo("SATISFIED");
    assertThat(text(row, "derivedStatus")).isNotEqualTo("MISSING");
    assertThat(assessment(systemId).get("counts").get("missing").asInt()).isZero();

    mockMvc.perform(get("/api/v1/systems/{id}/release-gate", systemId).with(officer()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.decision").value("PASS"))
        .andExpect(jsonPath("$.controls[?(@.proposalId == '%s')].mode".formatted(annexId)).value("INFORMATIONAL"));

    mockMvc.perform(put("/api/v1/systems/{id}/proposals/{proposalId}/mode", systemId, annexId)
            .with(officer())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"mode\":\"BLOCKING\"}"))
        .andExpect(status().isConflict());

    mockMvc.perform(patch("/api/v1/systems/{id}", systemId)
            .with(officer())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"evalScore\":70}"))
        .andExpect(status().isOk());
    mockMvc.perform(get("/api/v1/systems/{id}/release-gate", systemId).with(officer()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.decision").value("BLOCKED"));
  }

  private String exceptionBody(String proposalId, LocalDate expiresOn) {
    return """
        {"proposalId":"%s","rationale":"Recorded gap with an owner.","expiresOn":"%s"}
        """.formatted(proposalId, expiresOn);
  }

  private void indexEvidence(String systemId) throws Exception {
    mockMvc.perform(post("/api/v1/evidence/documents")
            .with(officer())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"systemId":"%s","type":"policy","title":"Control note","sourceUri":"memory://control-note","content":"Indexed control evidence for this system."}
                """.formatted(systemId)))
        .andExpect(status().isCreated());
  }

  private String proposal(String systemId, String relation, String provisionKey) throws Exception {
    String provision = provisionKey == null ? "" : ",\"provisionKey\":\"" + provisionKey + "\"";
    MvcResult created = mockMvc.perform(post("/api/v1/systems/{id}/proposals", systemId)
            .with(officer())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"relation\":\"" + relation + "\",\"excerpt\":\"Excerpt.\"" + provision + "}"))
        .andExpect(status().isCreated())
        .andReturn();
    return objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText();
  }

  private void accept(String systemId, String proposalId) throws Exception {
    mockMvc.perform(post("/api/v1/systems/{id}/proposals/{proposalId}/accept", systemId, proposalId)
            .with(officer()))
        .andExpect(status().isOk());
  }

  private void reject(String systemId, String proposalId) throws Exception {
    mockMvc.perform(post("/api/v1/systems/{id}/proposals/{proposalId}/reject", systemId, proposalId)
            .with(officer()))
        .andExpect(status().isOk());
  }

  private JsonNode assessment(String systemId) throws Exception {
    MvcResult result = mockMvc.perform(get("/api/v1/systems/{id}/assessment", systemId).with(officer()))
        .andExpect(status().isOk())
        .andReturn();
    return objectMapper.readTree(result.getResponse().getContentAsString());
  }

  private JsonNode pack(String systemId) throws Exception {
    MvcResult result = mockMvc.perform(get("/api/v1/systems/{id}/evidence-pack", systemId).with(officer()))
        .andExpect(status().isOk())
        .andReturn();
    return objectMapper.readTree(result.getResponse().getContentAsString());
  }

  private JsonNode proposals(String systemId) throws Exception {
    MvcResult listed = mockMvc.perform(get("/api/v1/systems/{id}/proposals", systemId).with(officer()))
        .andReturn();
    return objectMapper.readTree(listed.getResponse().getContentAsString()).get("items");
  }

  private static String statusOf(JsonNode assessment, String proposalId) {
    return text(item(assessment, proposalId), "derivedStatus");
  }

  private static JsonNode item(JsonNode assessment, String proposalId) {
    for (JsonNode row : assessment.get("items")) {
      if (proposalId.equals(row.get("proposalId").asText())) {
        return row;
      }
    }
    throw new AssertionError("missing " + proposalId);
  }

  private static java.util.List<String> labels(JsonNode queue) {
    java.util.List<String> labels = new java.util.ArrayList<>();
    for (JsonNode row : queue) {
      labels.add(row.get("label").asText());
    }
    return labels;
  }

  private static String text(JsonNode node, String field) {
    JsonNode value = node.get(field);
    return value == null || value.isNull() ? "" : value.asText();
  }

  private String createSystem(int evalScore) throws Exception {
    MvcResult created = mockMvc.perform(post("/api/v1/systems")
            .with(officer())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"name":"Assessment sitting","owner":"Reviewer","purpose":"Count evidence",
                 "riskClass":"LIMITED","riskBasis":"Limited transparency duties",
                 "deploymentRegion":"EU","evidenceCoverage":90,"evalScore":%d,
                 "dataContractStatus":"HEALTHY","openGaps":[]}
                """.formatted(evalScore)))
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

  private RequestPostProcessor bearer(UUID userId, UserRole role) {
    String token = jwtService.issueAccessToken(userId, TenantContext.DEFAULT_TENANT_ID, role);
    return request -> {
      request.addHeader("Authorization", "Bearer " + token);
      return request;
    };
  }
}
