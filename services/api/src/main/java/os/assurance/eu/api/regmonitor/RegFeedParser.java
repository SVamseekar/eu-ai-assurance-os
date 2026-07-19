package os.assurance.eu.api.regmonitor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * Parses RSS XML and curated JSON fixtures into {@link ParsedFeedItem}s.
 * HTML_LIST remote pages are not fully scraped in v1 — network sources that fail
 * fall back to bootstrap fixtures when configured.
 */
@Component
public class RegFeedParser {
  private static final Pattern ITEM_PATTERN =
      Pattern.compile("(?is)<item\\b[^>]*>(.*?)</item>");
  private static final Pattern TITLE_PATTERN =
      Pattern.compile("(?is)<title\\b[^>]*>(.*?)</title>");
  private static final Pattern LINK_PATTERN =
      Pattern.compile("(?is)<link\\b[^>]*>(.*?)</link>");
  private static final Pattern GUID_PATTERN =
      Pattern.compile("(?is)<guid\\b[^>]*>(.*?)</guid>");
  private static final Pattern DESC_PATTERN =
      Pattern.compile("(?is)<description\\b[^>]*>(.*?)</description>");
  private static final Pattern PUBDATE_PATTERN =
      Pattern.compile("(?is)<pubDate\\b[^>]*>(.*?)</pubDate>");

  private final ObjectMapper objectMapper;

  public RegFeedParser(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public List<ParsedFeedItem> parseRss(String xml) {
    List<ParsedFeedItem> items = new ArrayList<>();
    if (xml == null || xml.isBlank()) {
      return items;
    }
    Matcher itemMatcher = ITEM_PATTERN.matcher(xml);
    while (itemMatcher.find()) {
      String block = itemMatcher.group(1);
      String title = stripTags(firstMatch(TITLE_PATTERN, block));
      String link = stripTags(firstMatch(LINK_PATTERN, block));
      String guid = stripTags(firstMatch(GUID_PATTERN, block));
      String description = stripTags(firstMatch(DESC_PATTERN, block));
      String pubDateRaw = stripTags(firstMatch(PUBDATE_PATTERN, block));
      if (title == null || title.isBlank()) {
        continue;
      }
      String externalId = (guid != null && !guid.isBlank())
          ? guid
          : (link != null && !link.isBlank() ? link : title);
      String url = (link != null && !link.isBlank()) ? link : "about:blank";
      String summary = description == null || description.isBlank() ? title : description;
      items.add(new ParsedFeedItem(
          truncate(externalId, 512),
          truncate(title, 1024),
          truncate(summary, 4096),
          parsePubDate(pubDateRaw),
          truncate(url, 2048)));
    }
    return items;
  }

  public List<ParsedFeedItem> parseBootstrapJson(String json) throws IOException {
    JsonNode root = objectMapper.readTree(json);
    JsonNode arr = root.get("items");
    List<ParsedFeedItem> items = new ArrayList<>();
    if (arr == null || !arr.isArray()) {
      return items;
    }
    for (JsonNode node : arr) {
      String externalId = text(node, "externalId");
      String title = text(node, "title");
      String summary = text(node, "summary");
      String url = text(node, "url");
      Instant publishedAt = parseIso(text(node, "publishedAt"));
      if (title == null || title.isBlank() || externalId == null || externalId.isBlank()) {
        continue;
      }
      items.add(new ParsedFeedItem(
          truncate(externalId, 512),
          truncate(title, 1024),
          truncate(summary == null || summary.isBlank() ? title : summary, 4096),
          publishedAt,
          truncate(url == null || url.isBlank() ? "about:blank" : url, 2048)));
    }
    return items;
  }

  public List<ParsedFeedItem> loadClasspathFixture(String classpathLocation) throws IOException {
    String path = classpathLocation;
    if (path.startsWith("classpath:")) {
      path = path.substring("classpath:".length());
    }
    ClassPathResource resource = new ClassPathResource(path);
    try (InputStream in = resource.getInputStream()) {
      String body = new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
      if (path.endsWith(".json")) {
        return parseBootstrapJson(body);
      }
      return parseRss(body);
    }
  }

  private static String text(JsonNode node, String field) {
    JsonNode v = node.get(field);
    return v == null || v.isNull() ? null : v.asText();
  }

  private static String firstMatch(Pattern pattern, String block) {
    Matcher m = pattern.matcher(block);
    return m.find() ? m.group(1) : null;
  }

  private static String stripTags(String raw) {
    if (raw == null) {
      return null;
    }
    String unescaped = raw
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&amp;", "&")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace("<![CDATA[", "")
        .replace("]]>", "");
    return unescaped.replaceAll("(?is)<[^>]+>", " ").replaceAll("\\s+", " ").trim();
  }

  private static Instant parsePubDate(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }
    try {
      return Instant.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(raw.trim()));
    } catch (DateTimeParseException ignored) {
      // fall through
    }
    return parseIso(raw);
  }

  private static Instant parseIso(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }
    try {
      return Instant.parse(raw.trim());
    } catch (DateTimeParseException ignored) {
      // fall through
    }
    try {
      return OffsetDateTime.parse(raw.trim()).toInstant();
    } catch (DateTimeParseException ignored) {
      return null;
    }
  }

  private static String truncate(String value, int max) {
    if (value == null) {
      return null;
    }
    return value.length() <= max ? value : value.substring(0, max);
  }
}
