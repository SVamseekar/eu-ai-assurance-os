package os.assurance.eu.api.eval;

import java.util.Optional;
import os.assurance.eu.api.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@ConditionalOnProperty(name = "assurance.eval.worker.enabled", havingValue = "true", matchIfMissing = true)
public class EvalRunQueueWorker {
  private static final Logger LOGGER = LoggerFactory.getLogger(EvalRunQueueWorker.class);

  private final EvalRunRepository evalRuns;
  private final EvalRunWorkerService workerService;
  private final TenantContext tenantContext;

  public EvalRunQueueWorker(
      EvalRunRepository evalRuns,
      EvalRunWorkerService workerService,
      TenantContext tenantContext) {
    this.evalRuns = evalRuns;
    this.workerService = workerService;
    this.tenantContext = tenantContext;
  }

  @Scheduled(fixedDelayString = "${assurance.eval.worker.poll-interval-ms:5000}")
  public void dispatchNextQueuedRun() {
    Optional<EvalRunEntity> dispatchable = evalRuns.findNextDispatchable();
    if (dispatchable.isEmpty()) {
      return;
    }
    EvalRunEntity run = dispatchable.get();
    try {
      tenantContext.withTenant(run.tenantId(), () -> workerService.execute(run.id()));
    } catch (ResponseStatusException exception) {
      LOGGER.warn("Eval run {} dispatch failed with status {}", run.id(), exception.getStatusCode());
    } catch (RuntimeException exception) {
      LOGGER.warn("Eval run {} dispatch failed", run.id(), exception);
    }
  }
}
