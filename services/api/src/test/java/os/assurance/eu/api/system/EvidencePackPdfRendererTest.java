package os.assurance.eu.api.system;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EvidencePackPdfRendererTest {

  @Test
  void renderProducesPdfMagicBytes() {
    Instant generatedAt = Instant.parse("2026-07-20T12:00:00Z");
    UUID systemId = UUID.fromString("33333333-3333-3333-3333-333333333333");
    EvidencePackResponse pack = new EvidencePackResponse(
        systemId,
        generatedAt,
        ReleaseDecision.REVIEW,
        Map.of("riskClass", "LIMITED", "basis", "test"),
        List.of(Map.of("coverage", 50, "openGaps", List.of())),
        List.of(Map.of("latestScore", 80, "releaseDecision", "REVIEW")),
        List.of(),
        List.of(),
        List.of(),
        Map.of(
            "disclaimer",
            "Assisted determination (not legal advice). Suggested applicability / obligation map only.",
            "status",
            "NONE"),
        "1.0",
        "a".repeat(64),
        "eu-ai-assurance-api/0.1.0",
        "chain-head-example");

    byte[] pdf = EvidencePackPdfRenderer.render(pack, "Claims Triage AI");

    assertThat(pdf).isNotEmpty();
    String header = new String(pdf, 0, Math.min(5, pdf.length), StandardCharsets.ISO_8859_1);
    assertThat(header).startsWith("%PDF");
    assertThat(EvidencePackPdfRenderer.filename(systemId, generatedAt))
        .isEqualTo("evidence-pack-33333333-3333-3333-3333-333333333333-2026-07-20.pdf");
  }
}
