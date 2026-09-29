package os.assurance.eu.api.auth;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** In-memory sliding-window limiter. One API instance only; Cloudflare adds an edge rule in production. */
public class SlidingWindowRateLimiter {
  private static final int MAX_KEYS = 100_000;
  private final int maxEvents;
  private final Duration window;
  private final Clock clock;
  private final Map<String, Deque<Instant>> events = new ConcurrentHashMap<>();

  public SlidingWindowRateLimiter(int maxEvents, Duration window, Clock clock) {
    this.maxEvents = maxEvents;
    this.window = window;
    this.clock = clock;
  }

  public boolean tryAcquire(String key) {
    if (key == null || key.isBlank()) {
      key = "unknown";
    }
    if (events.size() > MAX_KEYS) {
      evictExpired();
    }
    Instant now = clock.instant();
    Instant cutoff = now.minus(window);
    Deque<Instant> deque = events.computeIfAbsent(key, k -> new ArrayDeque<>());
    synchronized (deque) {
      while (!deque.isEmpty() && deque.peekFirst().isBefore(cutoff)) {
        deque.pollFirst();
      }
      if (deque.size() >= maxEvents) {
        return false;
      }
      deque.addLast(now);
      return true;
    }
  }

  private void evictExpired() {
    Instant cutoff = clock.instant().minus(window);
    events.entrySet().removeIf(entry -> {
      synchronized (entry.getValue()) {
        return entry.getValue().isEmpty() || entry.getValue().peekLast().isBefore(cutoff);
      }
    });
  }
}
