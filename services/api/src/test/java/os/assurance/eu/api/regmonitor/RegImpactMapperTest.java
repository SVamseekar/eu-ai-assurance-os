package os.assurance.eu.api.regmonitor;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RegImpactMapperTest {
  private final RegImpactMapper mapper = new RegImpactMapper();
  private final Instant now = Instant.parse("2026-07-20T12:00:00Z");
  private final UUID itemId = UUID.fromString("a1600002-0000-4000-8000-000000000001");

  @Test
  void mapsHumanOversightToUncertainHints() {
    List<RegImpactHint> hints = mapper.mapHints(
        itemId,
        "EU AI Act human oversight expectations",
        "Art. 14 style measures for high-risk systems",
        now);

    assertThat(hints).isNotEmpty();
    assertThat(hints).allMatch(h -> h.impactLevel() == ImpactLevel.UNCERTAIN);
    assertThat(hints).anyMatch(h -> "HUMAN_OVERSIGHT".equals(h.controlCode()));
    assertThat(hints).anyMatch(h -> "HUMAN_OVERSIGHT_HIGH_IMPACT".equals(h.obligationCode()));
  }

  @Test
  void mapsEmploymentKeywords() {
    List<RegImpactHint> hints = mapper.mapHints(
        itemId,
        "Annex III employment and worker management",
        "Recruitment screening systems",
        now);

    assertThat(hints).allMatch(h -> h.impactLevel() == ImpactLevel.UNCERTAIN);
    assertThat(hints).anyMatch(h -> "EMPLOYMENT_HR".equals(h.obligationCode()));
  }

  @Test
  void baselineWhenNoKeywords() {
    List<RegImpactHint> hints = mapper.mapHints(itemId, "Weather update", "Sunny skies", now);

    assertThat(hints).hasSize(1);
    assertThat(hints.get(0).impactLevel()).isEqualTo(ImpactLevel.UNCERTAIN);
    assertThat(hints.get(0).controlCode()).isEqualTo("RISK_MANAGEMENT");
    assertThat(hints.get(0).obligationCode()).isEqualTo("BASELINE_GOVERNANCE");
  }

  @Test
  void neverEmitsLikelyInV1() {
    List<RegImpactHint> hints = mapper.mapHints(
        itemId,
        "High-risk Annex III biometric identification cybersecurity",
        "Full firehose of keywords",
        now);

    assertThat(hints).isNotEmpty();
    assertThat(hints).noneMatch(h -> h.impactLevel() == ImpactLevel.LIKELY);
    assertThat(hints).noneMatch(h -> h.impactLevel() == ImpactLevel.POSSIBLE);
  }
}
