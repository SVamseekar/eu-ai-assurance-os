package os.assurance.eu.api.eval;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import os.assurance.eu.api.system.ReleaseDecision;

public record EvalRun(
    UUID runId,
    UUID systemId,
    UUID datasetId,
    String status,
    String dataset,
    String modelVersion,
    String promptVersion,
    double threshold,
    Map<String, Object> metrics,
    ReleaseDecision releaseDecision,
    Instant createdAt,
    Instant queuedAt,
    Instant startedAt,
    Instant completedAt,
    Instant failedAt,
    int workerAttempts,
    int maxAttempts,
    String failureReason) {
}
