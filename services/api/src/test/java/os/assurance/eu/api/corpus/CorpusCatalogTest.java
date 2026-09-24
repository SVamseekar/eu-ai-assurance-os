package os.assurance.eu.api.corpus;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CorpusCatalogTest {
  @Test
  void fiveActsAndTheAmendmentAreStatutesAndGuidanceOnlyInterprets() {
    assertThat(CorpusCatalog.instruments())
        .extracting(InstrumentSeed::seedCelex)
        .containsExactly(
            "32024R1689",
            "32026R1744",
            "32016R0679",
            "32022R2554",
            "32022R2065",
            "32023R2854");
    assertThat(CorpusCatalog.instruments())
        .extracting(InstrumentSeed::consolidationCelex)
        .contains(
            "02024R1689-20260727",
            "02016R0679-20160504",
            "02022R2554-20221227",
            "02022R2065-20221027",
            "02023R2854-20231222");
    assertThat(CorpusCatalog.guidance()).isNotEmpty();
    assertThat(CorpusCatalog.guidance()).allMatch(item -> "interprets".equals(item.relation()));
    assertThat(CorpusCatalog.instruments())
        .extracting(InstrumentSeed::seedCelex)
        .doesNotContainAnyElementsOf(
            CorpusCatalog.guidance().stream().map(GuidanceSeed::sourceKey).toList());
  }
}
