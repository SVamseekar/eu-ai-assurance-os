package os.assurance.eu.api.eval;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class EvalRunMetrics {
  private final MeterRegistry meterRegistry;

  public EvalRunMetrics(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
  }

  public void queued(String source) {
    increment("assurance.eval.run.queued", "source", source);
  }

  public void claimed(String source) {
    increment("assurance.eval.run.claimed", "source", source);
  }

  public void completed(String source) {
    increment("assurance.eval.run.completed", "source", source);
  }

  public void failed(String reason) {
    increment("assurance.eval.run.failed", "reason", reason);
  }

  public void retried() {
    increment("assurance.eval.run.retried");
  }

  public void callbackSignatureRejected(String reason) {
    increment("assurance.eval.callback.signature.rejected", "reason", reason);
  }

  private void increment(String name) {
    meterRegistry.counter(name).increment();
  }

  private void increment(String name, String tag, String value) {
    meterRegistry.counter(name, tag, value).increment();
  }
}
