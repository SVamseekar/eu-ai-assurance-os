package os.assurance.eu.api.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import os.assurance.eu.api.tenant.TenantJpaRepository;
import os.assurance.eu.api.tenant.UserJpaRepository;

@SpringBootTest(properties = {
    "assurance.eval.worker.enabled=false",
    "assurance.eval.callback.secret=test-eval-callback-secret",
    "assurance.app.base-url=https://app.test",
    "assurance.security.auth-rate.per-ip-per-15m=1000"
})
@AutoConfigureMockMvc
class UnverifiedAccountCleanupTest {
  @Autowired MockMvc mockMvc;
  @Autowired JdbcTemplate jdbc;
  @Autowired UserJpaRepository users;
  @Autowired TenantJpaRepository tenants;
  @Autowired UnverifiedAccountCleanupJob cleanup;

  private UUID signup(String email) throws Exception {
    mockMvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"" + email + "\",\"organisationName\":\"Pending Org\"}"))
        .andExpect(status().isAccepted());
    return users.findByEmailIgnoreCase(email).orElseThrow().tenantId();
  }

  private void backdate(String email, long hours) {
    jdbc.update("update users set created_at = ? where lower(email) = lower(?)",
        Timestamp.from(Instant.now().minus(hours, ChronoUnit.HOURS)), email);
  }

  @Test
  void staleUnverifiedSignupsAreRemovedWithTheirEmptyWorkspace() throws Exception {
    String stale = "stale-" + UUID.randomUUID() + "@squat.example";
    String fresh = "fresh-" + UUID.randomUUID() + "@squat.example";
    UUID staleTenant = signup(stale);
    UUID freshTenant = signup(fresh);
    backdate(stale, 72);

    cleanup.removeStale();

    assertThat(users.findByEmailIgnoreCase(stale)).isEmpty();
    assertThat(tenants.findById(staleTenant)).isEmpty();
    assertThat(users.findByEmailIgnoreCase(fresh)).isPresent();
    assertThat(tenants.findById(freshTenant)).isPresent();
  }

  @Test
  void verifiedAccountsAreNeverRemovedHoweverOld() throws Exception {
    String email = "old-" + UUID.randomUUID() + "@squat.example";
    UUID tenant = signup(email);
    jdbc.update("update users set email_verified_at = created_at where lower(email) = lower(?)", email);
    backdate(email, 24 * 90);

    cleanup.removeStale();

    assertThat(users.findByEmailIgnoreCase(email)).isPresent();
    assertThat(tenants.findById(tenant)).isPresent();
  }

  @Test
  void anAddressFreedByCleanupCanSignUpAgain() throws Exception {
    String email = "again-" + UUID.randomUUID() + "@squat.example";
    signup(email);
    backdate(email, 72);
    cleanup.removeStale();
    signup(email);
    assertThat(users.findByEmailIgnoreCase(email)).isPresent();
  }
}
