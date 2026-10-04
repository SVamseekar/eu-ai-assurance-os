package os.assurance.eu.api.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import os.assurance.eu.api.email.EmailMessage;
import os.assurance.eu.api.email.EmailSender;
import os.assurance.eu.api.tenant.TenantJpaRepository;
import os.assurance.eu.api.tenant.UserJpaRepository;

@SpringBootTest(properties = {
    "assurance.eval.worker.enabled=false",
    "assurance.eval.callback.secret=test-eval-callback-secret",
    "assurance.app.base-url=https://app.test",
    "assurance.security.auth-rate.per-ip-per-15m=1000",
    "assurance.auth.email.cooldown-seconds=0",
    "spring.jackson.mapper.accept-case-insensitive-enums=true"
})
@AutoConfigureMockMvc
class WorkspaceLifecycleApiTest {
  private static final String PASSWORD = "correct-horse-battery";

  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper json;
  @Autowired JdbcTemplate jdbc;
  @Autowired DataSource dataSource;
  @Autowired TenantJpaRepository tenants;
  @Autowired UserJpaRepository users;
  @Autowired WorkspacePurgeJob purgeJob;
  @Autowired UnverifiedAccountCleanupJob cleanupJob;
  @MockitoSpyBean EmailSender emailSender;
  @MockitoSpyBean os.assurance.eu.api.evidence.FileStorageService storage;

  record Workspace(String email, String org, String bearer, UUID tenantId, String systemId) {}

  private Workspace newWorkspace(String label) throws Exception {
    String email = label + "-" + UUID.randomUUID() + "@acme.example";
    String orgName = "Org " + label + " " + UUID.randomUUID().toString().substring(0, 8);
    mockMvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"" + email + "\",\"organisationName\":\"" + orgName + "\"}"))
        .andExpect(status().isAccepted());
    ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
    verify(emailSender, org.mockito.Mockito.atLeastOnce()).send(captor.capture());
    String body = captor.getAllValues().stream().filter(m -> email.equals(m.to()))
        .reduce((a, b) -> b).orElseThrow().textBody();
    String token = body.substring(body.indexOf("token=") + 6).split("\\s")[0];
    String verified = mockMvc.perform(post("/auth/verify-email").contentType(MediaType.APPLICATION_JSON)
            .content("{\"token\":\"" + token + "\",\"password\":\"" + PASSWORD + "\"}"))
        .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    String bearer = "Bearer " + json.readTree(verified).get("accessToken").asText();
    UUID tenantId = users.findByEmailIgnoreCase(email).orElseThrow().tenantId();
    String system = mockMvc.perform(post("/api/v1/systems").header("Authorization", bearer)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"name":"System of %s","owner":"Ops","purpose":"lifecycle test","riskClass":"limited",
                 "riskBasis":"fixture","deploymentRegion":"EU","evidenceCoverage":80,"evalScore":80,
                 "dataContractStatus":"warning","openGaps":[]}""".formatted(orgName)))
        .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
    return new Workspace(email, orgName, bearer, tenantId, json.readTree(system).get("id").asText());
  }

  private String createKey(Workspace w) throws Exception {
    String body = mockMvc.perform(post("/api/v1/api-keys").header("Authorization", w.bearer())
            .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"CI\"}"))
        .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
    return json.readTree(body).get("key").asText();
  }

  private Map<String, String> unzip(byte[] zip) throws Exception {
    Map<String, String> files = new HashMap<>();
    try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(zip))) {
      for (ZipEntry e = in.getNextEntry(); e != null; e = in.getNextEntry()) {
        files.put(e.getName(), new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
      }
    }
    return files;
  }

  private long count(String table, String column, Object value) {
    Long n = jdbc.queryForObject("select count(*) from " + table + " where " + column + " = ?", Long.class, value);
    return n == null ? 0 : n;
  }

  @Test
  void exportIsAZipOfThisWorkspaceOnlyWithoutSecrets() throws Exception {
    Workspace mine = newWorkspace("export");
    Workspace other = newWorkspace("other");
    createKey(mine);

    var result = mockMvc.perform(get("/api/v1/account/export").header("Authorization", mine.bearer()))
        .andExpect(status().isOk()).andReturn().getResponse();
    assertThat(result.getContentType()).isEqualTo("application/zip");
    Map<String, String> files = unzip(result.getContentAsByteArray());

    assertThat(files).containsKeys("README.txt", "ai_systems.json", "audit_events.json", "users.json", "api_keys.json");
    assertThat(files.get("ai_systems.json")).contains("System of " + mine.org()).doesNotContain(other.org());
    assertThat(files.get("users.json")).contains(mine.email()).doesNotContain(other.email());
    String everything = String.join("\n", files.values());
    assertThat(everything).doesNotContain("password_hash").doesNotContain("passwordHash")
        .doesNotContain("key_hash").doesNotContain("token_hash").doesNotContain("$2a$");
  }

  @Test
  void deletionNeedsTheExactOrganisationName() throws Exception {
    Workspace w = newWorkspace("confirm");
    mockMvc.perform(delete("/api/v1/account").header("Authorization", w.bearer())
            .contentType(MediaType.APPLICATION_JSON).content("{\"confirmOrganisationName\":\"nope\"}"))
        .andExpect(status().isBadRequest());
    mockMvc.perform(delete("/api/v1/account").header("Authorization", w.bearer())
            .contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest());
    assertThat(tenants.findById(w.tenantId()).orElseThrow().active()).isTrue();
  }

  @Test
  void scheduledDeletionCutsEveryCredentialAndEmailsTheAdmins() throws Exception {
    Workspace w = newWorkspace("delete");
    String key = createKey(w);
    String refresh = json.readTree(mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"" + w.email() + "\",\"password\":\"" + PASSWORD + "\"}"))
        .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("refreshToken").asText();

    mockMvc.perform(delete("/api/v1/account").header("Authorization", w.bearer())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"confirmOrganisationName\":\"" + w.org() + "\"}"))
        .andExpect(status().isAccepted());

    var tenant = tenants.findById(w.tenantId()).orElseThrow();
    assertThat(tenant.status()).isEqualTo("DELETION_PENDING");
    assertThat(tenant.purgeAfter()).isAfter(Instant.now().plusSeconds(29L * 24 * 3600));

    mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"" + w.email() + "\",\"password\":\"" + PASSWORD + "\"}"))
        .andExpect(status().isForbidden()).andExpect(jsonPath("$.error").value("workspace_deleted"));
    mockMvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON)
            .content("{\"refreshToken\":\"" + refresh + "\"}"))
        .andExpect(status().is4xxClientError());
    mockMvc.perform(get("/api/v1/ci/release-gate").param("systemId", w.systemId()).header("X-Api-Key", key))
        .andExpect(status().isUnauthorized());
    mockMvc.perform(get("/api/v1/systems").header("Authorization", w.bearer()))
        .andExpect(status().isUnauthorized());

    verify(emailSender).send(argThat(m -> w.email().equals(m.to()) && m.subject().contains("scheduled for deletion")
        && m.textBody().contains(w.org())));
  }

  @Test
  void anInviteIntoAWorkspaceScheduledForDeletionCannotBeAccepted() throws Exception {
    Workspace w = newWorkspace("invdel");
    String invitee = "late-" + UUID.randomUUID() + "@acme.example";
    mockMvc.perform(post("/api/v1/admin/users/invites").header("Authorization", w.bearer())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"" + invitee + "\",\"role\":\"AUDITOR\"}"))
        .andExpect(status().isCreated());
    ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
    verify(emailSender, org.mockito.Mockito.atLeastOnce()).send(captor.capture());
    String body = captor.getAllValues().stream().filter(m -> invitee.equals(m.to()))
        .reduce((a, b) -> b).orElseThrow().textBody();
    String token = body.substring(body.indexOf("token=") + 6).split("\\s")[0];
    mockMvc.perform(delete("/api/v1/account").header("Authorization", w.bearer())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"confirmOrganisationName\":\"" + w.org() + "\"}"))
        .andExpect(status().isAccepted());

    mockMvc.perform(post("/auth/accept-invite").contentType(MediaType.APPLICATION_JSON)
            .content("{\"token\":\"" + token + "\",\"password\":\"" + PASSWORD + "\"}"))
        .andExpect(status().isGone());
    assertThat(users.findByEmailIgnoreCase(invitee)).isEmpty();
  }

  @Test
  void cleanupNeverRemovesAWorkspaceThatHoldsData() throws Exception {
    Workspace w = newWorkspace("hasdata");
    jdbc.update("update users set email_verified_at = null, created_at = ? where tenant_id = ?",
        java.sql.Timestamp.from(Instant.now().minusSeconds(3600L * 24 * 30)), w.tenantId());
    jdbc.update("delete from auth_tokens where user_id in (select id from users where tenant_id = ?)", w.tenantId());

    cleanupJob.removeStale();

    assertThat(tenants.findById(w.tenantId())).isPresent();
    assertThat(count("ai_systems", "tenant_id", w.tenantId())).isEqualTo(1);
  }

  @Test
  void purgeRemovesTheWorkspaceEverywhereAndLeavesOthersAlone() throws Exception {
    Workspace gone = newWorkspace("purge");
    Workspace kept = newWorkspace("kept");
    createKey(gone);
    mockMvc.perform(delete("/api/v1/account").header("Authorization", gone.bearer())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"confirmOrganisationName\":\"" + gone.org() + "\"}"))
        .andExpect(status().isAccepted());

    purgeJob.purgeDue();
    assertThat(tenants.findById(gone.tenantId())).isPresent(); // still inside the 30 day window

    jdbc.update("update tenants set purge_after = ? where id = ?",
        java.sql.Timestamp.from(Instant.now().minusSeconds(60)), gone.tenantId());
    purgeJob.purgeDue();

    assertThat(tenants.findById(gone.tenantId())).isEmpty();
    assertThat(users.findByEmailIgnoreCase(gone.email())).isEmpty();
    assertThat(count("ai_systems", "tenant_id", gone.tenantId())).isZero();
    assertThat(count("audit_events", "tenant_id", gone.tenantId())).isZero();
    assertThat(count("api_keys", "tenant_id", gone.tenantId())).isZero();
    assertThat(count("refresh_tokens", "tenant_id", gone.tenantId())).isZero();
    assertThat(count("audit_chain_heads", "tenant_id", gone.tenantId())).isZero();

    assertThat(tenants.findById(kept.tenantId())).isPresent();
    assertThat(count("ai_systems", "tenant_id", kept.tenantId())).isEqualTo(1);
    assertThat(count("audit_events", "tenant_id", kept.tenantId())).isPositive();
    mockMvc.perform(get("/api/v1/systems").header("Authorization", kept.bearer())).andExpect(status().isOk());
  }

  @Test
  void filesAreDeletedOnlyAfterTheDatabasePurgeHasCommitted() throws Exception {
    Workspace w = newWorkspace("order");
    mockMvc.perform(delete("/api/v1/account").header("Authorization", w.bearer())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"confirmOrganisationName\":\"" + w.org() + "\"}"))
        .andExpect(status().isAccepted());
    jdbc.update("update tenants set purge_after = ? where id = ?",
        java.sql.Timestamp.from(Instant.now().minusSeconds(60)), w.tenantId());
    java.util.concurrent.atomic.AtomicBoolean tenantStillThere = new java.util.concurrent.atomic.AtomicBoolean(true);
    org.mockito.Mockito.doAnswer(inv -> {
      tenantStillThere.set(tenants.findById(w.tenantId()).isPresent());
      return null;
    }).when(storage).deletePrefix(org.mockito.ArgumentMatchers.contains(w.tenantId().toString()));
    purgeJob.purgeDue();
    assertThat(tenantStillThere).isFalse();
    org.mockito.Mockito.verify(storage).deletePrefix(org.mockito.ArgumentMatchers.contains(w.tenantId().toString()));
  }

  @Test
  void anAddressCanSignUpAgainAfterItsWorkspaceIsPurged() throws Exception {
    Workspace w = newWorkspace("again");
    mockMvc.perform(delete("/api/v1/account").header("Authorization", w.bearer())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"confirmOrganisationName\":\"" + w.org() + "\"}"))
        .andExpect(status().isAccepted());
    jdbc.update("update tenants set purge_after = ? where id = ?",
        java.sql.Timestamp.from(Instant.now().minusSeconds(60)), w.tenantId());
    purgeJob.purgeDue();
    mockMvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"" + w.email() + "\",\"organisationName\":\"Fresh start\"}"))
        .andExpect(status().isAccepted());
    assertThat(users.findByEmailIgnoreCase(w.email())).isPresent();
  }

  @Test
  void onlyAnAdminWithASessionMayExportOrDelete() throws Exception {
    Workspace w = newWorkspace("guard");
    String key = createKey(w);
    // an API key (even an admin's) is refused: a leaked CI key must not be able to exfiltrate or destroy
    mockMvc.perform(get("/api/v1/account/export").header("X-Api-Key", key)).andExpect(status().isForbidden());
    mockMvc.perform(delete("/api/v1/account").header("X-Api-Key", key)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"confirmOrganisationName\":\"" + w.org() + "\"}"))
        .andExpect(status().isForbidden());
    assertThat(tenants.findById(w.tenantId()).orElseThrow().active()).isTrue();
  }

  /**
   * Schema guard: every table that holds tenant data (a tenant_id column, or a foreign key into such a table)
   * must be purged, children before parents. Fails when a new migration adds a table the purge forgot.
   */
  @Test
  void purgeListCoversEveryTenantOwnedTableInForeignKeyOrder() throws Exception {
    List<String> order = new ArrayList<>();
    for (String sql : WorkspacePurgeJob.PURGE_SQL) {
      java.util.regex.Matcher m = java.util.regex.Pattern.compile("^delete from ([a-z_]+) ").matcher(sql);
      assertThat(m.find()).as(sql).isTrue();
      order.add(m.group(1));
    }
    Set<String> tables = new HashSet<>();
    Map<String, Set<String>> parentsOf = new HashMap<>();
    try (var conn = dataSource.getConnection()) {
      DatabaseMetaData meta = conn.getMetaData();
      try (ResultSet rs = meta.getTables(null, null, "%", new String[] {"TABLE"})) {
        while (rs.next()) {
          String schema = rs.getString("TABLE_SCHEM");
          if (schema != null && (schema.equalsIgnoreCase("INFORMATION_SCHEMA") || schema.equalsIgnoreCase("pg_catalog"))) continue;
          tables.add(rs.getString("TABLE_NAME").toLowerCase());
        }
      }
      Set<String> owned = new HashSet<>();
      for (String t : tables) {
        try (ResultSet cols = meta.getColumns(null, null, t, "tenant_id");
            ResultSet colsUpper = meta.getColumns(null, null, t.toUpperCase(), "TENANT_ID")) {
          if (cols.next() || colsUpper.next()) owned.add(t);
        }
      }
      owned.add("tenants");
      for (String t : tables) {
        for (String name : new String[] {t, t.toUpperCase()}) {
          try (ResultSet fks = meta.getImportedKeys(null, null, name)) {
            while (fks.next()) {
              parentsOf.computeIfAbsent(t, k -> new HashSet<>()).add(fks.getString("PKTABLE_NAME").toLowerCase());
            }
          }
        }
      }
      boolean grew = true;
      while (grew) {
        grew = false;
        for (String t : tables) {
          if (!owned.contains(t) && parentsOf.getOrDefault(t, Set.of()).stream().anyMatch(owned::contains)) {
            owned.add(t);
            grew = true;
          }
        }
      }
      owned.remove("flyway_schema_history");
      assertThat(order).as("tables holding tenant data").containsAll(owned);
      for (String child : owned) {
        for (String parent : parentsOf.getOrDefault(child, Set.of())) {
          if (owned.contains(parent) && !parent.equals(child)) {
            assertThat(order.indexOf(child)).as(child + " must be purged before " + parent)
                .isLessThan(order.indexOf(parent));
          }
        }
      }
    }
  }
}
