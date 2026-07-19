package os.assurance.eu.api.tenant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * FORBIDDEN-path coverage for the role matrix (Part 10 / docs/ROLE_MATRIX.md).
 * Uses the default MVP tenant with distinct API keys per role.
 */
@SpringBootTest(properties = {
    "assurance.eval.worker.enabled=false",
    "assurance.eval.callback.secret=test-eval-callback-secret",
    "spring.jackson.mapper.accept-case-insensitive-enums=true"
})
@AutoConfigureMockMvc
class RoleAuthorizationTest {
  private static final String TENANT_ID = TenantContext.DEFAULT_TENANT_ID.toString();
  private static final String COMPLIANCE_KEY = "00000000-0000-0000-0000-000000000a01";
  private static final String ENGINEERING_KEY = "00000000-0000-0000-0000-000000000a02";
  private static final String AUDITOR_KEY = "00000000-0000-0000-0000-000000000a03";
  private static final String LEGAL_KEY = "00000000-0000-0000-0000-000000000a04";
  private static final String ADMIN_KEY = "00000000-0000-0000-0000-000000000a05";

  private static final String ENGINEERING_ID = "00000000-0000-0000-0000-000000000102";
  private static final String AUDITOR_ID = "00000000-0000-0000-0000-000000000103";
  private static final String LEGAL_ID = "00000000-0000-0000-0000-000000000104";
  private static final String ADMIN_ID = "00000000-0000-0000-0000-000000000105";

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private UserJpaRepository users;
  @Autowired private ApiKeyJpaRepository apiKeys;

  @BeforeEach
  void seedRoles() {
    Instant now = Instant.now();
    seedUser(ENGINEERING_ID, "engineering@example.com", UserRole.AI_ENGINEERING_LEAD, now);
    seedUser(AUDITOR_ID, "auditor@example.com", UserRole.AUDITOR, now);
    seedUser(LEGAL_ID, "legal@example.com", UserRole.LEGAL_COUNSEL, now);
    seedUser(ADMIN_ID, "admin@example.com", UserRole.ADMIN, now);
    seedKey(COMPLIANCE_KEY, TenantContext.DEFAULT_USER_ID.toString(), now);
    seedKey(ENGINEERING_KEY, ENGINEERING_ID, now);
    seedKey(AUDITOR_KEY, AUDITOR_ID, now);
    seedKey(LEGAL_KEY, LEGAL_ID, now);
    seedKey(ADMIN_KEY, ADMIN_ID, now);
  }

  @Test
  void auditorCannotMutateRegistryControlsEvidenceContractsOrEval() throws Exception {
    String systemId = createSystemAsCompliance();

    mockMvc.perform(post("/api/v1/systems")
            .with(apiKey(AUDITOR_KEY))
            .contentType(MediaType.APPLICATION_JSON)
            .content(systemJson("Auditor Create Attempt")))
        .andExpect(status().isForbidden());

    mockMvc.perform(patch("/api/v1/systems/{id}", systemId)
            .with(apiKey(AUDITOR_KEY))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"evidenceCoverage": 10}
                """))
        .andExpect(status().isForbidden());

    mockMvc.perform(post("/api/v1/systems/{id}/risk-classification", systemId)
            .with(apiKey(AUDITOR_KEY))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "riskClass": "high",
                  "basis": "auditor should not reclassify",
                  "affectedUsers": ["x"],
                  "humanOversightRequired": true
                }
                """))
        .andExpect(status().isForbidden());

    mockMvc.perform(post("/api/v1/evidence/documents")
            .with(apiKey(AUDITOR_KEY))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "systemId": "%s",
                  "type": "POLICY",
                  "title": "Nope",
                  "sourceUri": "memory://nope",
                  "content": "auditor ingest blocked"
                }
                """.formatted(systemId)))
        .andExpect(status().isForbidden());

    mockMvc.perform(post("/api/v1/data-contracts")
            .with(apiKey(AUDITOR_KEY))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "systemId": "%s",
                  "name": "Auditor Contract",
                  "owner": "Audit",
                  "version": "1"
                }
                """.formatted(systemId)))
        .andExpect(status().isForbidden());

    mockMvc.perform(post("/api/v1/eval-runs")
            .with(apiKey(AUDITOR_KEY))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "systemId": "%s",
                  "dataset": "golden-eu-claims-v4",
                  "modelVersion": "x",
                  "promptVersion": "y",
                  "threshold": 0.85
                }
                """.formatted(systemId)))
        .andExpect(status().isForbidden());

    mockMvc.perform(post("/api/v1/eval-datasets")
            .with(apiKey(AUDITOR_KEY))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "name": "auditor-dataset",
                  "version": "1",
                  "sampleCount": 1,
                  "golden": false
                }
                """))
        .andExpect(status().isForbidden());

    mockMvc.perform(post("/api/v1/audit-events")
            .with(apiKey(AUDITOR_KEY))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "systemId": "%s",
                  "eventType": "manual.note",
                  "resourceType": "ai_system",
                  "resourceId": "%s",
                  "payload": {}
                }
                """.formatted(systemId, systemId)))
        .andExpect(status().isForbidden());

    // Auditor may still read registry and export evidence pack
    mockMvc.perform(get("/api/v1/systems/{id}", systemId).with(apiKey(AUDITOR_KEY)))
        .andExpect(status().isOk());
    mockMvc.perform(get("/api/v1/systems/{id}/evidence-pack", systemId).with(apiKey(AUDITOR_KEY)))
        .andExpect(status().isOk());
  }

  @Test
  void legalCannotCreateSystemsButCanClassifyRisk() throws Exception {
    String systemId = createSystemAsCompliance();

    mockMvc.perform(post("/api/v1/systems")
            .with(apiKey(LEGAL_KEY))
            .contentType(MediaType.APPLICATION_JSON)
            .content(systemJson("Legal Create Attempt")))
        .andExpect(status().isForbidden());

    mockMvc.perform(post("/api/v1/systems/{id}/risk-classification", systemId)
            .with(apiKey(LEGAL_KEY))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "riskClass": "high",
                  "basis": "Legal review of essential-service impact",
                  "affectedUsers": ["claimants"],
                  "humanOversightRequired": true
                }
                """))
        .andExpect(status().isOk());
  }

  @Test
  void complianceCannotCreateEvalDatasetsButEngineeringCan() throws Exception {
    mockMvc.perform(post("/api/v1/eval-datasets")
            .with(apiKey(COMPLIANCE_KEY))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "name": "compliance-owned-dataset",
                  "version": "1",
                  "sampleCount": 5,
                  "golden": false
                }
                """))
        .andExpect(status().isForbidden());

    mockMvc.perform(post("/api/v1/eval-datasets")
            .with(apiKey(ENGINEERING_KEY))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "name": "engineering-owned-dataset",
                  "version": "1",
                  "sampleCount": 5,
                  "golden": false
                }
                """))
        .andExpect(status().isCreated());
  }

  @Test
  void nonAdminCannotOverrideWorkflowStage() throws Exception {
    String systemId = createHighRiskSystem();
    MvcResult wf = mockMvc.perform(get("/api/v1/systems/{id}/workflows/active", systemId)
            .with(apiKey(COMPLIANCE_KEY)))
        .andExpect(status().isOk())
        .andReturn();
    JsonNode body = objectMapper.readTree(wf.getResponse().getContentAsString());
    String workflowId = body.get("id").asText();
    String stageId = body.get("stages").get(0).get("id").asText();

    mockMvc.perform(post("/api/v1/systems/{s}/workflows/{w}/stages/{st}/override",
                systemId, workflowId, stageId)
            .with(apiKey(ENGINEERING_KEY))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"rationale\": \"not an admin\"}"))
        .andExpect(status().isForbidden());

    mockMvc.perform(post("/api/v1/systems/{s}/workflows/{w}/stages/{st}/override",
                systemId, workflowId, stageId)
            .with(apiKey(ADMIN_KEY))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"rationale\": \"Admin emergency override with board approval\"}"))
        .andExpect(status().isOk());
  }

  private void seedUser(String id, String email, UserRole role, Instant now) {
    UUID userId = UUID.fromString(id);
    users.findById(userId)
        .orElseGet(() -> users.save(new UserEntity(userId, UUID.fromString(TENANT_ID), email, role, now)));
  }

  private void seedKey(String rawKey, String userId, Instant now) {
    UUID keyId = UUID.fromString(rawKey);
    apiKeys.findById(keyId)
        .orElseGet(() -> apiKeys.save(new ApiKeyEntity(
            keyId,
            ApiKeyHasher.sha256Hex(rawKey),
            UUID.fromString(TENANT_ID),
            UUID.fromString(userId),
            now)));
  }

  private RequestPostProcessor apiKey(String key) {
    return request -> {
      request.addHeader("X-Api-Key", key);
      return request;
    };
  }

  private String createSystemAsCompliance() throws Exception {
    MvcResult result = mockMvc.perform(post("/api/v1/systems")
            .with(apiKey(COMPLIANCE_KEY))
            .contentType(MediaType.APPLICATION_JSON)
            .content(systemJson("Role Matrix Fixture")))
        .andExpect(status().isCreated())
        .andReturn();
    return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
  }

  private String createHighRiskSystem() throws Exception {
    MvcResult result = mockMvc.perform(post("/api/v1/systems")
            .with(apiKey(COMPLIANCE_KEY))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "name": "High Risk Role Fixture",
                  "owner": "Risk",
                  "purpose": "role tests",
                  "riskClass": "high",
                  "riskBasis": "Annex III",
                  "deploymentRegion": "EU",
                  "evidenceCoverage": 40,
                  "evalScore": 40,
                  "dataContractStatus": "breach",
                  "openGaps": ["Human oversight SOP missing"]
                }
                """))
        .andExpect(status().isCreated())
        .andReturn();
    return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
  }

  private static String systemJson(String name) {
    return """
        {
          "name": "%s",
          "owner": "Ops",
          "purpose": "role matrix",
          "riskClass": "limited",
          "riskBasis": "fixture",
          "deploymentRegion": "EU",
          "evidenceCoverage": 80,
          "evalScore": 80,
          "dataContractStatus": "warning",
          "openGaps": []
        }
        """.formatted(name);
  }
}
