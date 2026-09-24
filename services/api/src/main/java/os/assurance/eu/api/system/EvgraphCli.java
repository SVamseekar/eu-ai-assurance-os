package os.assurance.eu.api.system;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Runs the evgraph CLI on accepted artifact files. The library stays outside the JVM.
 */
@Component
public class EvgraphCli {
  private final ObjectMapper objectMapper;
  private final String command;

  public EvgraphCli(
      ObjectMapper objectMapper,
      @Value("${assurance.evgraph.command:evgraph}") String command) {
    this.objectMapper = objectMapper;
    this.command = resolveCommand(command);
  }

  public List<Map<String, Object>> currentGaps(Map<String, Object> artifacts) {
    try {
      Path dir = Files.createTempDirectory("evgraph-pack");
      Path modelCard = dir.resolve("model_card.json");
      Path approval = dir.resolve("approval.json");
      Path deployment = dir.resolve("deployment.json");
      Path dataset = dir.resolve("dataset_manifest.csv");
      objectMapper.writeValue(modelCard.toFile(), artifacts.get("model_card"));
      objectMapper.writeValue(approval.toFile(), artifacts.get("approval"));
      objectMapper.writeValue(deployment.toFile(), artifacts.get("deployment"));
      Files.writeString(dataset, String.valueOf(artifacts.get("dataset_manifest_csv")));
      List<Map<String, Object>> gaps = new ArrayList<>();
      gaps.addAll(gapsFrom(command, "scan", modelCard.toString(), approval.toString(), deployment.toString()));
      gaps.addAll(gapsFrom(command, "scan-dataset-manifest", dataset.toString()));
      return gaps;
    } catch (Exception e) {
      throw new IllegalStateException("evgraph scan failed", e);
    }
  }

  static String resolveCommand(String configured) {
    String override = System.getenv("EVGRAPH_BIN");
    if (override != null && !override.isBlank()) {
      return override;
    }
    Path sibling = Path.of(System.getProperty("user.dir", "."))
        .resolve("../../../evgraph/.venv/bin/evgraph")
        .normalize();
    if (Files.isExecutable(sibling)) {
      return sibling.toString();
    }
    return configured == null || configured.isBlank() ? "evgraph" : configured;
  }

  private List<Map<String, Object>> gapsFrom(String command, String subcommand, String... paths) throws Exception {
    List<String> argv = new ArrayList<>();
    argv.add(command);
    argv.add(subcommand);
    argv.addAll(List.of(paths));
    argv.add("--format");
    argv.add("json");
    Process process = new ProcessBuilder(argv).start();
    String stdout = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    String stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
    int code = process.waitFor();
    if (code != 0) {
      throw new IllegalStateException(stderr.isBlank() ? "evgraph exit " + code : stderr);
    }
    JsonNode findings = objectMapper.readTree(stdout).get("findings");
    List<Map<String, Object>> gaps = new ArrayList<>();
    if (findings == null || !findings.isArray()) {
      return gaps;
    }
    for (JsonNode finding : findings) {
      String outcome = finding.path("outcome").asText();
      if ("EXPECTATION_MET".equals(outcome)) {
        continue;
      }
      Map<String, Object> gap = new LinkedHashMap<>();
      gap.put("rule_id", finding.path("rule_id").asText());
      gap.put("outcome", outcome);
      gaps.add(gap);
    }
    return gaps;
  }
}
