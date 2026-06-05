package os.assurance.eu.api.eval;

import java.util.List;

public record EvalRunOperationsView(
    long queued,
    long running,
    long failed,
    List<EvalRun> retryQueue,
    List<EvalRun> deadLetter) {
}
