package os.assurance.eu.api.demo;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Visitors' questions are stored in the one shared demo workspace, so everyone who opens the demo can read
 * them. They are removed after an hour so abusive or personal text does not stay on display.
 */
@Component
public class DemoQuestionCleanupJob {
  private static final Logger log = LoggerFactory.getLogger(DemoQuestionCleanupJob.class);
  static final Duration KEEP = Duration.ofHours(1);

  private final JdbcTemplate jdbc;
  private final Clock clock;

  public DemoQuestionCleanupJob(JdbcTemplate jdbc, Clock clock) {
    this.jdbc = jdbc;
    this.clock = clock;
  }

  @Scheduled(fixedDelay = 600_000, initialDelay = 600_000)
  public void clearOldQuestions() {
    int removed = jdbc.update("delete from evidence_queries where tenant_id = ? and created_at < ?",
        DemoProperties.DEMO_TENANT_ID, Timestamp.from(clock.instant().minus(KEEP)));
    if (removed > 0) {
      log.info("Removed {} demo questions", removed);
    }
  }
}
