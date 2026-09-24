package os.assurance.eu.api.system;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
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
 * When no local binary is present, the scan installs the pinned PyPI release into
 * {@code target/evgraph-0.1.2} and runs that CLI. It does not invent gap rows.
 */
@Component
public class EvgraphCli {
  static final String PINNED_VERSION = "0.1.2";
  private static final String PINNED_PACKAGE = "evgraph-cli==" + PINNED_VERSION;
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
    if (executable(override)) {
      return override;
    }
    Path sibling = Path.of(System.getProperty("user.dir", "."))
        .resolve("../../../evgraph/.venv/bin/evgraph")
        .normalize();
    if (Files.isExecutable(sibling)) {
      return sibling.toString();
    }
    String fallback = configured == null || configured.isBlank() ? "evgraph" : configured;
    if (onPath(fallback)) {
      return fallback;
    }
    if (!"evgraph".equals(fallback)) {
      return fallback;
    }
    return pinnedCli().toString();
  }

  static Path installPinned(Path root) throws IOException, InterruptedException {
    Path bin = root.resolve("bin/evgraph");
    if (Files.isExecutable(bin)) {
      return bin;
    }
    synchronized (EvgraphCli.class) {
      if (Files.isExecutable(bin)) {
        return bin;
      }
      Files.createDirectories(root.getParent() == null ? Path.of(".") : root.getParent());
      run("python3", "-m", "venv", root.toString());
      run(root.resolve("bin/pip").toString(), "install", "--disable-pip-version-check", PINNED_PACKAGE);
    }
    if (!Files.isExecutable(bin)) {
      throw new IOException("pinned evgraph " + PINNED_VERSION + " did not install " + bin);
    }
    return bin;
  }

  private static Path pinnedCli() {
    Path root = Path.of(System.getProperty("user.dir", ".")).resolve("target/evgraph-" + PINNED_VERSION);
    try {
      return installPinned(root);
    } catch (Exception e) {
      throw new IllegalStateException("evgraph " + PINNED_VERSION + " is not installed", e);
    }
  }

  private static boolean executable(String path) {
    return path != null && !path.isBlank() && Files.isExecutable(Path.of(path));
  }

  private static boolean onPath(String command) {
    if (command.contains("/") || command.contains("\\")) {
      return Files.isExecutable(Path.of(command));
    }
    String path = System.getenv("PATH");
    if (path == null || path.isBlank()) {
      return false;
    }
    for (String dir : path.split(java.io.File.pathSeparator)) {
      if (Files.isExecutable(Path.of(dir).resolve(command))) {
        return true;
      }
    }
    return false;
  }

  private static void run(String... argv) throws IOException, InterruptedException {
    Process process = new ProcessBuilder(argv).redirectErrorStream(true).start();
    String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    int code = process.waitFor();
    if (code != 0) {
      throw new IOException(output.isBlank() ? "exit " + code : output.trim());
    }
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
