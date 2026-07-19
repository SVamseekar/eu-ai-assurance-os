package os.assurance.eu.api.regmonitor;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.UnknownHostException;
import org.junit.jupiter.api.Test;

class SsrfSafeHttpFetcherTest {
  private final SsrfSafeHttpFetcher.SsrfSafeDnsResolver resolver =
      new SsrfSafeHttpFetcher.SsrfSafeDnsResolver();

  @Test
  void rejectsLoopback() {
    assertThatThrownBy(() -> resolver.resolve("localhost"))
        .isInstanceOf(UnknownHostException.class)
        .hasMessageContaining("private or reserved");
  }

  @Test
  void rejectsLiteralLoopbackIp() {
    assertThatThrownBy(() -> resolver.resolve("127.0.0.1"))
        .isInstanceOf(UnknownHostException.class);
  }

  @Test
  void rejectsNonHttpsScheme() {
    RegMonitorProperties props = new RegMonitorProperties();
    SsrfSafeHttpFetcher fetcher = new SsrfSafeHttpFetcher(props);
    assertThatThrownBy(() -> fetcher.fetchHttps("http://example.com/feed"))
        .isInstanceOf(SsrfSafeHttpFetcher.SsrfRejectedException.class)
        .hasMessageContaining("https");
  }
}
