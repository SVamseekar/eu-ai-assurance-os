package os.assurance.eu.api.sector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "assurance.sector")
public class SectorProperties {
  /**
   * Comma-separated pack ids to enable (default: insurance,hr,finance).
   * Unknown ids are ignored with a warn log in the registry.
   */
  private String packs = "insurance,hr,finance";

  public String getPacks() {
    return packs;
  }

  public void setPacks(String packs) {
    this.packs = packs;
  }

  public List<String> enabledPackIds() {
    if (packs == null || packs.isBlank()) {
      return List.of();
    }
    return Arrays.stream(packs.split("[,\\s]+"))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .map(s -> s.toLowerCase(Locale.ROOT))
        .distinct()
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
