package os.assurance.eu.api.observability;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

/**
 * Latency measurement hooks for PRD §7 NFR targets.
 *
 * <p>Timers record observed durations only. They do not assert SLO compliance — see {@code docs/NFR.md}.
 */
@Component
public class NfrMetrics {
  public static final String REGISTRY_READ_TIMER = "assurance.api.registry.read";
  public static final String EVIDENCE_QUERY_TIMER = "assurance.api.evidence.query";

  private final MeterRegistry meterRegistry;

  public NfrMetrics(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
  }

  public <T> T recordRegistryRead(String operation, Supplier<T> work) {
    return record(REGISTRY_READ_TIMER, operation, work);
  }

  public <T> T recordEvidenceQuery(Supplier<T> work) {
    return record(EVIDENCE_QUERY_TIMER, "answer", work);
  }

  private <T> T record(String name, String operation, Supplier<T> work) {
    Timer.Sample sample = Timer.start(meterRegistry);
    try {
      return work.get();
    } finally {
      sample.stop(Timer.builder(name)
          .description("Observed API latency for NFR measurement (not certified compliance)")
          .tag("operation", operation)
          .publishPercentileHistogram()
          .register(meterRegistry));
    }
  }

  /** Test helper: record a known duration without executing work. */
  void recordRegistryReadNanos(String operation, long nanos) {
    Timer.builder(REGISTRY_READ_TIMER)
        .tag("operation", operation)
        .publishPercentileHistogram()
        .register(meterRegistry)
        .record(nanos, TimeUnit.NANOSECONDS);
  }
}
