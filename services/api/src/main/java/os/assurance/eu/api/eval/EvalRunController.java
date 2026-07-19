package os.assurance.eu.api.eval;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import os.assurance.eu.api.audit.AuditService;
import os.assurance.eu.api.system.AiSystemRepository;
import os.assurance.eu.api.system.ReleaseDecision;
import os.assurance.eu.api.tenant.TenantAuthorizationService;
import os.assurance.eu.api.tenant.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/eval-runs")
public class EvalRunController {
  private final AiSystemRepository systems;
  private final EvalDatasetRepository datasets;
  private final EvalRunRepository evalRuns;
  private final EvalRunCompletionService completionService;
  private final EvalRunWorkerService workerService;
  private final EvalRunOperationsService operationsService;
  private final EvalCallbackSignatureVerifier signatureVerifier;
  private final EvalRunMetrics metrics;
  private final AuditService auditService;
  private final TenantAuthorizationService authorizationService;
  private final ObjectMapper objectMapper;

  public EvalRunController(
      AiSystemRepository systems,
      EvalDatasetRepository datasets,
      EvalRunRepository evalRuns,
      EvalRunCompletionService completionService,
      EvalRunWorkerService workerService,
      EvalRunOperationsService operationsService,
      EvalCallbackSignatureVerifier signatureVerifier,
      EvalRunMetrics metrics,
      AuditService auditService,
      TenantAuthorizationService authorizationService,
      ObjectMapper objectMapper) {
    this.systems = systems;
    this.datasets = datasets;
    this.evalRuns = evalRuns;
    this.completionService = completionService;
    this.workerService = workerService;
    this.operationsService = operationsService;
    this.signatureVerifier = signatureVerifier;
    this.metrics = metrics;
    this.auditService = auditService;
    this.authorizationService = authorizationService;
    this.objectMapper = objectMapper;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.ACCEPTED)
  public EvalRunResponse createEvalRun(@Valid @RequestBody CreateEvalRunRequest request) {
    authorizationService.requireAnyRole(
        UserRole.ADMIN, UserRole.AI_ENGINEERING_LEAD, UserRole.COMPLIANCE_OFFICER);
    systems.findById(request.systemId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "AI system not found"));
    EvalDataset dataset = datasets.findByName(request.dataset())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Eval dataset is not registered"));
    UUID runId = UUID.randomUUID();
    Instant now = Instant.now();
    EvalRun evalRun = evalRuns.save(new EvalRun(
        runId,
        request.systemId(),
        dataset.id(),
        "queued",
        dataset.name(),
        request.modelVersion(),
        request.promptVersion(),
        request.threshold(),
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
    auditService.append(
        request.systemId(),
        "eval_run.queued",
        "eval_run",
        runId.toString(),
        Map.of("dataset", dataset.name(), "datasetVersion", dataset.version(), "threshold", request.threshold()));
    metrics.queued("api");
    return new EvalRunResponse(evalRun.runId(), evalRun.status());
  }

  @GetMapping("/{runId}")
  public EvalRun getEvalRun(@PathVariable UUID runId) {
    return evalRuns.findById(runId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Eval run not found"));
  }

  @GetMapping("/operations")
  public EvalRunOperationsView getEvalRunOperations() {
    authorizationService.requireAnyRole(UserRole.ADMIN, UserRole.AI_ENGINEERING_LEAD, UserRole.COMPLIANCE_OFFICER);
    return operationsService.operationsView();
  }

  @PatchMapping("/{runId}/result")
  public EvalRun completeEvalRun(
      @PathVariable UUID runId,
      @RequestHeader(value = EvalCallbackSignatureVerifier.TIMESTAMP_HEADER, required = false) String timestamp,
      @RequestHeader(value = EvalCallbackSignatureVerifier.SIGNATURE_HEADER, required = false) String signature,
      @RequestBody String rawBody) {
    authorizationService.requireAnyRole(UserRole.ADMIN, UserRole.AI_ENGINEERING_LEAD);
    signatureVerifier.verify(rawBody, timestamp, signature);
    CompleteEvalRunRequest request = readCompletionRequest(rawBody);
    return completionService.complete(runId, request.metrics(), "callback");
  }

  @PostMapping("/{runId}/execute")
  public EvalRun executeEvalRun(@PathVariable UUID runId) {
    authorizationService.requireAnyRole(UserRole.ADMIN, UserRole.AI_ENGINEERING_LEAD);
    return workerService.execute(runId);
  }

  @PostMapping("/{runId}/retry")
  public EvalRun retryEvalRun(@PathVariable UUID runId) {
    authorizationService.requireAnyRole(UserRole.ADMIN, UserRole.AI_ENGINEERING_LEAD);
    return operationsService.retryFailed(runId);
  }

  private CompleteEvalRunRequest readCompletionRequest(String rawBody) {
    try {
      CompleteEvalRunRequest request = objectMapper.readValue(rawBody, CompleteEvalRunRequest.class);
      if (request.metrics() == null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Eval metrics are required");
      }
      return request;
    } catch (JsonProcessingException exception) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid eval callback payload");
    }
  }
}
