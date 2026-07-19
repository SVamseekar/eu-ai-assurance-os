package os.assurance.eu.api.tenant;

import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import os.assurance.eu.api.eval.EvalCallbackSignatureVerifier;

/**
 * Cross-tenant isolation regression suite (Part 10).
 *
 * <p>Tenant A must never receive tenant B resource bodies. Cross-tenant ID access returns 404
 * (or 403 when role-denied first); never 200 with B data.
 */
@SpringBootTest(properties = {
    "assurance.evidence.max-content-characters=256",
    "assurance.evidence.max-question-characters=96",
    "assurance.eval.worker.enabled=false",
    "assurance.eval.callback.secret=test-eval-callback-secret",
    "management.endpoints.web.exposure.include=health,info,metrics",
    "spring.jackson.mapper.accept-case-insensitive-enums=true"
})
@AutoConfigureMockMvc
class TenantIsolationTest {
  private static final String CALLBACK_SECRET = "test-eval-callback-secret";

  private static final String TENANT_A_ID = "00000000-0000-0000-0000-00000000a001";
  private static final String TENANT_B_ID = "00000000-0000-0000-0000-00000000b001";
  private static final String USER_A_ID = "00000000-0000-0000-0000-00000000a101";
  private static final String USER_B_ID = "00000000-0000-0000-0000-00000000b101";
  private static final String USER_B_ENG_ID = "00000000-0000-0000-0000-00000000b102";
  private static final String KEY_A = "00000000-0000-0000-0000-00000000aa01";
  private static final String KEY_B = "00000000-0000-0000-0000-00000000bb01";
  private static final String KEY_B_ENG = "00000000-0000-0000-0000-00000000bb02";

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private TenantJpaRepository tenants;
  @Autowired private UserJpaRepository users;
  @Autowired private ApiKeyJpaRepository apiKeys;

  @BeforeEach
  void seedTenants() {
    Instant now = Instant.now();
    seedTenant(TENANT_A_ID, "Isolation Tenant A", now);
    seedTenant(TENANT_B_ID, "Isolation Tenant B", now);
    seedUser(USER_A_ID, TENANT_A_ID, "iso-a@example.com", UserRole.ADMIN, now);
    seedUser(USER_B_ID, TENANT_B_ID, "iso-b@example.com", UserRole.ADMIN, now);
    seedUser(USER_B_ENG_ID, TENANT_B_ID, "iso-b-eng@example.com", UserRole.AI_ENGINEERING_LEAD, now);
    seedApiKey(KEY_A, TENANT_A_ID, USER_A_ID, now);
    seedApiKey(KEY_B, TENANT_B_ID, USER_B_ID, now);
    seedApiKey(KEY_B_ENG, TENANT_B_ID, USER_B_ENG_ID, now);
  }

  @Test
  void systemsAreIsolatedForListGetUpdateAndEvidencePack() throws Exception {
    String systemB = createSystem(KEY_B, "Tenant B Claims AI");

    mockMvc.perform(get("/api/v1/systems").with(apiKey(KEY_A)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(systemB), hasSize(0)));

    mockMvc.perform(get("/api/v1/systems/{id}", systemB).with(apiKey(KEY_A)))
        .andExpect(status().isNotFound());

    mockMvc.perform(patch("/api/v1/systems/{id}", systemB)
            .with(apiKey(KEY_A))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"evidenceCoverage": 99, "evalScore": 99, "dataContractStatus": "healthy", "openGaps": []}
                """))
        .andExpect(status().isNotFound());

    mockMvc.perform(get("/api/v1/systems/{id}/release-gate", systemB).with(apiKey(KEY_A)))
        .andExpect(status().isNotFound());

    mockMvc.perform(get("/api/v1/systems/{id}/evidence-pack", systemB).with(apiKey(KEY_A)))
        .andExpect(status().isNotFound());

    mockMvc.perform(post("/api/v1/systems/{id}/risk-classification", systemB)
            .with(apiKey(KEY_A))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "riskClass": "high",
                  "basis": "cross-tenant attempt",
                  "affectedUsers": ["x"],
                  "humanOversightRequired": true
                }
                """))
        .andExpect(status().isNotFound());

    // Owner tenant still sees its system
    mockMvc.perform(get("/api/v1/systems/{id}", systemB).with(apiKey(KEY_B)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(systemB))
        .andExpect(jsonPath("$.name").value("Tenant B Claims AI"));
  }

  @Test
  void evidenceDocumentsAndQueriesAreIsolated() throws Exception {
    String systemB = createSystem(KEY_B, "B Evidence System");

    mockMvc.perform(post("/api/v1/evidence/documents")
            .with(apiKey(KEY_B))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "systemId": "%s",
                  "type": "POLICY",
                  "title": "B Secret Policy",
                  "sourceUri": "memory://b-secret",
                  "content": "Tenant B confidential human oversight SOP for essential services."
                }
                """.formatted(systemB)))
        .andExpect(status().isCreated());

    mockMvc.perform(get("/api/v1/evidence/systems/{systemId}/documents", systemB).with(apiKey(KEY_A)))
        .andExpect(status().isNotFound());

    mockMvc.perform(post("/api/v1/evidence/documents")
            .with(apiKey(KEY_A))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "systemId": "%s",
                  "type": "POLICY",
                  "title": "A tries B system",
                  "sourceUri": "memory://a-on-b",
                  "content": "Should not attach to foreign system."
                }
                """.formatted(systemB)))
        .andExpect(status().isNotFound());

    mockMvc.perform(post("/api/v1/evidence/query")
            .with(apiKey(KEY_A))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"systemId": "%s", "question": "What is the oversight SOP?"}
                """.formatted(systemB)))
        .andExpect(status().isNotFound());
  }

  @Test
  void dataContractsAndDriftAreIsolated() throws Exception {
    String systemB = createSystem(KEY_B, "B Contract System");
    String contractB = createContract(KEY_B, systemB, "B Input Schema", "2026-07");

    mockMvc.perform(get("/api/v1/data-contracts/{id}", contractB).with(apiKey(KEY_A)))
        .andExpect(status().isNotFound());

    // requireSystem is tenant-scoped → foreign systemId is 404 (not empty list)
    mockMvc.perform(get("/api/v1/data-contracts").param("systemId", systemB).with(apiKey(KEY_A)))
        .andExpect(status().isNotFound());

    mockMvc.perform(patch("/api/v1/data-contracts/{id}", contractB)
            .with(apiKey(KEY_A))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"status": "breach", "coverage": 10}
                """))
        .andExpect(status().isNotFound());

    mockMvc.perform(post("/api/v1/data-contracts/{id}/drift-events", contractB)
            .with(apiKey(KEY_A))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"severity": "breach", "field": "amount", "description": "cross-tenant"}
                """))
        .andExpect(status().isNotFound());

    mockMvc.perform(get("/api/v1/data-contracts/{id}/drift-events", contractB).with(apiKey(KEY_A)))
        .andExpect(status().isNotFound());
  }

  @Test
  void controlsWorkflowsAndAuditAreIsolated() throws Exception {
    String systemB = createSystem(KEY_B, "B Workflow System");

    mockMvc.perform(get("/api/v1/systems/{id}/controls", systemB).with(apiKey(KEY_A)))
        .andExpect(status().isNotFound());

    mockMvc.perform(put("/api/v1/systems/{id}/controls/{controlId}", systemB, UUID.randomUUID())
            .with(apiKey(KEY_A))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"status": "PASS", "notes": "nope"}
                """))
        .andExpect(status().isNotFound());

    mockMvc.perform(get("/api/v1/systems/{id}/workflows", systemB).with(apiKey(KEY_A)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));

    mockMvc.perform(get("/api/v1/systems/{id}/workflows/active", systemB).with(apiKey(KEY_A)))
        .andExpect(status().isNotFound());

    mockMvc.perform(get("/api/v1/audit-events").param("systemId", systemB).with(apiKey(KEY_A)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));

    mockMvc.perform(post("/api/v1/audit-events")
            .with(apiKey(KEY_A))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "systemId": "%s",
                  "eventType": "cross.tenant.attempt",
                  "resourceType": "ai_system",
                  "resourceId": "%s",
                  "payload": {}
                }
                """.formatted(systemB, systemB)))
        .andExpect(status().isNotFound());
  }

  @Test
  void apiKeyForACannotReadBAndEvalCallbackCannotForgeCrossTenantRun() throws Exception {
    String systemB = createSystem(KEY_B, "B Eval System");

    // Ensure B has the golden dataset name used by bootstrap on default tenant only —
    // create a dataset for tenant B first.
    mockMvc.perform(post("/api/v1/eval-datasets")
            .with(apiKey(KEY_B_ENG))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "name": "golden-eu-claims-v4",
                  "version": "iso-b",
                  "sampleCount": 10,
                  "golden": true
                }
                """))
        .andExpect(status().isCreated());

    MvcResult runResult = mockMvc.perform(post("/api/v1/eval-runs")
            .with(apiKey(KEY_B))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "systemId": "%s",
                  "dataset": "golden-eu-claims-v4",
                  "modelVersion": "b-model",
                  "promptVersion": "b-prompt",
                  "threshold": 0.85
                }
                """.formatted(systemB)))
        .andExpect(status().isAccepted())
        .andReturn();
    String runId = read(runResult).get("runId").asText();

    mockMvc.perform(get("/api/v1/eval-runs/{runId}", runId).with(apiKey(KEY_A)))
        .andExpect(status().isNotFound());

    mockMvc.perform(post("/api/v1/eval-runs/{runId}/execute", runId).with(apiKey(KEY_A)))
        .andExpect(status().isNotFound());

    mockMvc.perform(signedCallback(runId, validCallbackBody(0.91), KEY_A))
        .andExpect(status().isNotFound());

    // B can still read its own run
    mockMvc.perform(get("/api/v1/eval-runs/{runId}", runId).with(apiKey(KEY_B)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.runId").value(runId))
        .andExpect(jsonPath("$.status").value("queued"));
  }

  @Test
  void tenantAListDoesNotLeakTenantBNamesInSystemsOrContracts() throws Exception {
    createSystem(KEY_A, "Only Tenant A System");
    createSystem(KEY_B, "SECRET Tenant B System");
    String systemB = createSystem(KEY_B, "SECRET B Second");
    createContract(KEY_B, systemB, "SECRET B Contract", "1");

    MvcResult systemsA = mockMvc.perform(get("/api/v1/systems").with(apiKey(KEY_A)))
        .andExpect(status().isOk())
        .andReturn();
    String body = systemsA.getResponse().getContentAsString();
    org.assertj.core.api.Assertions.assertThat(body).doesNotContain("SECRET");
    org.assertj.core.api.Assertions.assertThat(body).contains("Only Tenant A System");

    MvcResult contractsA = mockMvc.perform(get("/api/v1/data-contracts").with(apiKey(KEY_A)))
        .andExpect(status().isOk())
        .andReturn();
    org.assertj.core.api.Assertions.assertThat(contractsA.getResponse().getContentAsString())
        .doesNotContain("SECRET");
  }

  private void seedTenant(String id, String name, Instant now) {
    UUID tenantId = UUID.fromString(id);
    tenants.findById(tenantId)
        .orElseGet(() -> tenants.save(new TenantEntity(tenantId, name, "starter", "EU", now)));
  }

  private void seedUser(String id, String tenantId, String email, UserRole role, Instant now) {
    UUID userId = UUID.fromString(id);
    users.findById(userId)
        .orElseGet(() -> users.save(new UserEntity(userId, UUID.fromString(tenantId), email, role, now)));
  }

  private void seedApiKey(String rawKey, String tenantId, String userId, Instant now) {
    UUID apiKeyId = UUID.fromString(rawKey);
    apiKeys.findById(apiKeyId)
        .orElseGet(() -> apiKeys.save(new ApiKeyEntity(
            apiKeyId,
            ApiKeyHasher.sha256Hex(rawKey),
            UUID.fromString(tenantId),
            UUID.fromString(userId),
            now)));
  }

  private RequestPostProcessor apiKey(String key) {
    return request -> {
      request.addHeader("X-Api-Key", key);
      return request;
    };
  }

  private String createSystem(String key, String name) throws Exception {
    MvcResult result = mockMvc.perform(post("/api/v1/systems")
            .with(apiKey(key))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "name": "%s",
                  "owner": "Isolation Owner",
                  "purpose": "Cross-tenant isolation fixture",
                  "riskClass": "limited",
                  "riskBasis": "Test fixture",
                  "deploymentRegion": "EU",
                  "evidenceCoverage": 80,
                  "evalScore": 80,
                  "dataContractStatus": "warning",
                  "openGaps": []
                }
                """.formatted(name)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", not(blankOrNullString())))
        .andReturn();
    return read(result).get("id").asText();
  }

  private String createContract(String key, String systemId, String name, String version) throws Exception {
    MvcResult result = mockMvc.perform(post("/api/v1/data-contracts")
            .with(apiKey(key))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "systemId": "%s",
                  "name": "%s",
                  "owner": "Data Platform",
                  "version": "%s"
                }
                """.formatted(systemId, name, version)))
        .andExpect(status().isCreated())
        .andReturn();
    return read(result).get("id").asText();
  }

  private MockHttpServletRequestBuilder signedCallback(String runId, String body, String apiKey)
      throws Exception {
    String timestamp = String.valueOf(Instant.now().getEpochSecond());
    String signature = "v1=" + hmacHex(timestamp + "." + body);
    return patch("/api/v1/eval-runs/{runId}/result", runId)
        .header("X-Api-Key", apiKey)
        .header(EvalCallbackSignatureVerifier.TIMESTAMP_HEADER, timestamp)
        .header(EvalCallbackSignatureVerifier.SIGNATURE_HEADER, signature)
        .contentType(MediaType.APPLICATION_JSON)
        .content(body);
  }

  private static String validCallbackBody(double faithfulness) {
    return """
        {
          "metrics": {
            "faithfulness": %s,
            "relevance": 0.92,
            "safetyRefusal": 0.93,
            "biasSlicePassRate": 0.94
          }
        }
        """.formatted(faithfulness);
  }

  private static String hmacHex(String payload) throws Exception {
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(CALLBACK_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
    byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
    StringBuilder hex = new StringBuilder(digest.length * 2);
    for (byte b : digest) {
      hex.append(String.format("%02x", b));
    }
    return hex.toString();
  }

  private JsonNode read(MvcResult result) throws Exception {
    return objectMapper.readTree(result.getResponse().getContentAsString());
  }
}
