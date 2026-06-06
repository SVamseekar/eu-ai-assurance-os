package os.assurance.eu.api.eval;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import os.assurance.eu.api.system.AiSystem;
import os.assurance.eu.api.system.AiSystemRepository;
import os.assurance.eu.api.system.ReleaseDecision;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
    "assurance.eval.worker.enabled=false",
    "assurance.eval.callback.secret=test-eval-callback-secret"
})
@ActiveProfiles("postgres")
@Tag("postgres")
class PostgresEvalRunConcurrencyTest {
  @Autowired
  private AiSystemRepository systems;

  @Autowired
  private EvalRunRepository evalRuns;

  @Test
  void concurrentWorkersClaimQueuedRunOnceWithSkipLocked() throws Exception {
    AiSystem system = systems.findAll().get(0);
    UUID runId = UUID.randomUUID();
    Instant now = Instant.now();
    evalRuns.save(new EvalRun(
        runId,
        system.id(),
        null,
        "queued",
        "golden-eu-claims-v4",
        "claims-triage-postgres-concurrency",
        "claims-routing-postgres-concurrency",
        0.85,
        Map.of(),
        ReleaseDecision.REVIEW,
        now,
        now,
        null,
        null,
        null,
        0,
        3,
        null));

    Callable<Boolean> claim = () -> evalRuns.claimNextDispatchable().isPresent();
    ExecutorService executor = Executors.newFixedThreadPool(4);
    try {
      List<Boolean> claims = executor.invokeAll(List.of(claim, claim, claim, claim)).stream()
          .map(future -> {
            try {
              return future.get();
            } catch (Exception exception) {
              throw new IllegalStateException(exception);
            }
          })
          .toList();

      assertThat(claims).containsExactlyInAnyOrder(true, false, false, false);
    } finally {
      executor.shutdownNow();
    }

    EvalRun claimed = evalRuns.findById(runId).orElseThrow();
    assertThat(claimed.status()).isEqualTo("running");
    assertThat(claimed.workerAttempts()).isEqualTo(1);
  }
}
