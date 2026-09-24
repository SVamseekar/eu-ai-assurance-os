package os.assurance.eu.api.system;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class EvgraphCliInstallTest {
  @Test
  void pinnedCliReportsTheRealApprovalGap() throws Exception {
    Path root = Files.createTempDirectory("evgraph-pin");
    Path bin = EvgraphCli.installPinned(root);
    Path dir = root.resolve("artifacts");
    Files.createDirectories(dir);
    Path modelCard = dir.resolve("model_card.json");
    Path approval = dir.resolve("approval.json");
    Path deployment = dir.resolve("deployment.json");
    Files.writeString(modelCard, "{\"model_name\":\"Pack scan\",\"intended_use\":\"Export accepted files\"}");
    Files.writeString(approval, "{\"approver\":\"unassigned\"}");
    Files.writeString(deployment, "{\"deployed_at\":\"2026-09-23T00:00:00Z\"}");

    Process process = new ProcessBuilder(
        bin.toString(), "scan", modelCard.toString(), approval.toString(), deployment.toString(),
        "--format", "json")
        .redirectErrorStream(true)
        .start();
    String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    assertThat(process.waitFor()).as(output).isZero();
    JsonNode findings = new ObjectMapper().readTree(output).get("findings");
    List<String> gaps = new ArrayList<>();
    for (JsonNode finding : findings) {
      String outcome = finding.path("outcome").asText();
      if ("EXPECTATION_MET".equals(outcome)) {
        continue;
      }
      gaps.add(finding.path("rule_id").asText() + "|" + outcome);
    }
    assertThat(gaps).contains("approval-precedes-deployment|INCONCLUSIVE");
  }
}
