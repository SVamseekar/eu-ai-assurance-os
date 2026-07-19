package os.assurance.eu.api.regmonitor;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "assurance.reg-monitor")
public class RegMonitorProperties {
  /** Master switch for the scheduled poller. */
  private boolean enabled = true;

  /** Scheduler fixed delay between poll cycles (ms). */
  private long pollIntervalMs = 60_000L;

  /** When true, STATIC_FIXTURE sources are always ingested if empty / due. */
  private boolean bootstrapFixtures = true;

  /** Max characters retained from a remote feed body. */
  private int maxFetchCharacters = 2_000_000;

  /** Connect timeout seconds for SSRF-safe HTTPS fetch. */
  private int connectTimeoutSeconds = 10;

  /** Response timeout seconds for SSRF-safe HTTPS fetch. */
  private int responseTimeoutSeconds = 30;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public long getPollIntervalMs() {
    return pollIntervalMs;
  }

  public void setPollIntervalMs(long pollIntervalMs) {
    this.pollIntervalMs = pollIntervalMs;
  }

  public boolean isBootstrapFixtures() {
    return bootstrapFixtures;
  }

  public void setBootstrapFixtures(boolean bootstrapFixtures) {
    this.bootstrapFixtures = bootstrapFixtures;
  }

  public int getMaxFetchCharacters() {
    return maxFetchCharacters;
  }

  public void setMaxFetchCharacters(int maxFetchCharacters) {
    this.maxFetchCharacters = maxFetchCharacters;
  }

  public int getConnectTimeoutSeconds() {
    return connectTimeoutSeconds;
  }

  public void setConnectTimeoutSeconds(int connectTimeoutSeconds) {
    this.connectTimeoutSeconds = connectTimeoutSeconds;
  }

  public int getResponseTimeoutSeconds() {
    return responseTimeoutSeconds;
  }

  public void setResponseTimeoutSeconds(int responseTimeoutSeconds) {
    this.responseTimeoutSeconds = responseTimeoutSeconds;
  }
}
