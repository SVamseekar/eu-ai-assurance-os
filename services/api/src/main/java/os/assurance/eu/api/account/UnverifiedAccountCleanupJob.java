package os.assurance.eu.api.account;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Removes self-serve signups nobody confirmed. Without this an unverified row keeps an address
 * (and its empty workspace) reserved for ever, so anyone could squat on a colleague's address.
 * The cut-off is twice the verification link lifetime; a still-valid link keeps an account alive, but only
 * for seven days from signup.
 * Only workspaces in which no user ever verified an address, and that hold no systems, documents or
 * API keys, are touched.
 */
@Component
public class UnverifiedAccountCleanupJob {
  private static final Logger log = LoggerFactory.getLogger(UnverifiedAccountCleanupJob.class);
  static final Duration MAX_AGE = Duration.ofHours(48);
  /** Even a live link cannot keep an unconfirmed signup beyond this: repeated resends must not extend a squat. */
  static final Duration ABSOLUTE_MAX_AGE = Duration.ofDays(7);

  private final JdbcTemplate jdbc;
  private final WorkspacePurgeJob purger;
  private final Clock clock;

  public UnverifiedAccountCleanupJob(JdbcTemplate jdbc, WorkspacePurgeJob purger, Clock clock) {
    this.jdbc = jdbc;
    this.purger = purger;
    this.clock = clock;
  }

  @Scheduled(cron = "0 45 3 * * *", zone = "UTC")
  public void removeStale() {
    Timestamp cutoff = Timestamp.from(clock.instant().minus(MAX_AGE));
    Timestamp absoluteCutoff = Timestamp.from(clock.instant().minus(ABSOLUTE_MAX_AGE));
    List<UUID> stale = jdbc.query(
        "select t.id from tenants t where t.status = 'ACTIVE' "
            + "and exists (select 1 from users u where u.tenant_id = t.id) "
            + "and not exists (select 1 from users u where u.tenant_id = t.id "
            + "  and (u.email_verified_at is not null or u.created_at >= ?"
            + "    or (u.created_at >= ? and exists (select 1 from auth_tokens a where a.user_id = u.id"
            + "      and a.purpose = 'VERIFY_EMAIL' and a.used_at is null and a.expires_at > ?)))) "
            // Never touch a workspace that holds any work, whatever the verification state.
            + "and not exists (select 1 from ai_systems s where s.tenant_id = t.id) "
            + "and not exists (select 1 from evidence_documents d where d.tenant_id = t.id) "
            + "and not exists (select 1 from api_keys k where k.tenant_id = t.id)",
        (rs, i) -> rs.getObject(1, UUID.class), cutoff, absoluteCutoff, Timestamp.from(clock.instant()));
    for (UUID tenantId : stale) {
      try {
        purger.purge(tenantId);
        log.info("Removed unconfirmed signup workspace {}", tenantId);
      } catch (RuntimeException e) {
        log.error("Could not remove unconfirmed workspace {}", tenantId, e);
      }
    }
  }
}
