package os.assurance.eu.api.corpus;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class CorpusVersionHasherTest {
  @Test
  void hashIsStableSha256OfCelexConsolidationTextAndApplication() {
    List<CorpusVersionHasher.InstrumentLine> instruments = List.of(
        new CorpusVersionHasher.InstrumentLine(
            "32016R0679", "2016-05-04", "bbb", "2018-05-25"),
        new CorpusVersionHasher.InstrumentLine(
            "32024R1689", "2026-07-27", "aaa", "2026-08-02"));
    List<CorpusVersionHasher.GuidanceLine> guidance = List.of(
        new CorpusVersionHasher.GuidanceLine("edpb-gdpr-guidelines", "ccc"));

    String hash = CorpusVersionHasher.hash(instruments, guidance);

    assertThat(hash).hasSize(64).matches("[0-9a-f]{64}");
    assertThat(hash).isEqualTo(CorpusVersionHasher.hash(instruments, guidance));
    assertThat(hash).isNotEqualTo(CorpusVersionHasher.hash(
        List.of(new CorpusVersionHasher.InstrumentLine(
            "32024R1689", "2026-07-27", "changed", "2026-08-02")),
        guidance));
  }
}
