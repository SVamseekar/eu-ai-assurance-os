package os.assurance.eu.api.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class SlidingWindowRateLimiterTest {
  @Test
  void allowsUpToMaxThenBlocksUntilWindowPasses() {
    AtomicReference<Instant> now = new AtomicReference<>(Instant.parse("2026-10-01T00:00:00Z"));
    Clock clock = new Clock() {
      @Override public ZoneOffset getZone() { return ZoneOffset.UTC; }
      @Override public Clock withZone(java.time.ZoneId zone) { return this; }
      @Override public Instant instant() { return now.get(); }
    };
    SlidingWindowRateLimiter limiter = new SlidingWindowRateLimiter(3, Duration.ofMinutes(15), clock);
    assertThat(limiter.tryAcquire("k")).isTrue();
    assertThat(limiter.tryAcquire("k")).isTrue();
    assertThat(limiter.tryAcquire("k")).isTrue();
    assertThat(limiter.tryAcquire("k")).isFalse();
    assertThat(limiter.tryAcquire("other")).isTrue();
    now.set(now.get().plus(Duration.ofMinutes(16)));
    assertThat(limiter.tryAcquire("k")).isTrue();
  }
}
