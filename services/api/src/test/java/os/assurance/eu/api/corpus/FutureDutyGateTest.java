package os.assurance.eu.api.corpus;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class FutureDutyGateTest {
  private static final LocalDate AS_OF = LocalDate.of(2026, 9, 23);

  @Test
  void annexIiiIsNotACurrentDutyWhenItsForceDateIsVisible() {
    List<FutureDutyGate.Row> rows = List.of(
        new FutureDutyGate.Row("annexIII", "", LocalDate.of(2027, 12, 2)));

    assertThat(FutureDutyGate.cites(
        "Art. 6 / Annex III (essential private services) — indicative",
        rows,
        AS_OF)).isTrue();
  }

  @Test
  void article50IsCurrentWhenItsForceDateHasPassed() {
    List<FutureDutyGate.Row> rows = List.of(
        new FutureDutyGate.Row("", "50", LocalDate.of(2026, 8, 2)));

    assertThat(FutureDutyGate.cites(
        "Art. 50 — indicative (in force 2 Aug 2026)",
        rows,
        AS_OF)).isFalse();
  }

  @Test
  void missingCorpusRowsLeaveTheDutyUnchanged() {
    assertThat(FutureDutyGate.cites(
        "Art. 6 / Annex III",
        List.of(),
        AS_OF)).isFalse();
  }
}
