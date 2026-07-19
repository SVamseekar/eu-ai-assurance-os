package os.assurance.eu.api.regmonitor;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class RegFeedParserTest {
  private final RegFeedParser parser = new RegFeedParser(new ObjectMapper());

  @Test
  void parsesSampleRssFixture() throws Exception {
    byte[] bytes = new ClassPathResource("reg-monitor/sample-rss.xml").getInputStream().readAllBytes();
    String xml = new String(bytes, StandardCharsets.UTF_8);

    List<ParsedFeedItem> items = parser.parseRss(xml);

    assertThat(items).hasSize(2);
    assertThat(items.get(0).externalId()).isEqualTo("oj-item-logging-1");
    assertThat(items.get(0).title()).containsIgnoringCase("logging");
    assertThat(items.get(0).url()).contains("logging-1");
    assertThat(items.get(0).publishedAt()).isNotNull();
    assertThat(items.get(1).title()).containsIgnoringCase("Biometric");
  }

  @Test
  void parsesBootstrapJsonFromClasspath() throws Exception {
    List<ParsedFeedItem> items =
        parser.loadClasspathFixture("classpath:reg-monitor/bootstrap-feed.json");

    assertThat(items).hasSizeGreaterThanOrEqualTo(4);
    assertThat(items).allMatch(i -> i.title() != null && !i.title().isBlank());
    assertThat(items).allMatch(i -> i.externalId() != null && !i.externalId().isBlank());
    assertThat(items).anyMatch(i -> i.title().toLowerCase().contains("oversight"));
  }

  @Test
  void contentHashIsStable() throws Exception {
    List<ParsedFeedItem> items =
        parser.loadClasspathFixture("classpath:reg-monitor/bootstrap-feed.json");
    ParsedFeedItem first = items.get(0);
    String h1 = RegContentHasher.hash(first);
    String h2 = RegContentHasher.hash(first);
    assertThat(h1).isEqualTo(h2);
    assertThat(h1).hasSize(64);
  }
}
