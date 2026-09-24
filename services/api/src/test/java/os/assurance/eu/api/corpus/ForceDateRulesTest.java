package os.assurance.eu.api.corpus;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ForceDateRulesTest {
  private final ForceDateRules rules = new ForceDateRules();
  private final LocalDate asOf = LocalDate.of(2026, 9, 23);

  @Test
  void annexIiiHighRiskDateStaysFutureOn23Sep2026() {
    ForceAssignment assignment = rules.assign(
        "02024R1689-20260727",
        provision("annexIII", "", "", ""),
        asOf);

    assertThat(assignment.forceFrom()).isEqualTo(LocalDate.of(2027, 12, 2));
    assertThat(assignment.status()).isEqualTo(ForceStatus.FUTURE);
    assertThat(assignment.status()).isNotEqualTo(ForceStatus.IN_FORCE);
  }

  @Test
  void annexIIsFutureUntil2Aug2028AndArticle50IsInForce() {
    ForceAssignment annexI = rules.assign(
        "32024R1689",
        provision("annexI", "", "", ""),
        asOf);
    ForceAssignment article50 = rules.assign(
        "02024R1689-20260727",
        provision("", "50", "1", ""),
        asOf);

    assertThat(annexI.forceFrom()).isEqualTo(LocalDate.of(2028, 8, 2));
    assertThat(annexI.status()).isEqualTo(ForceStatus.FUTURE);
    assertThat(article50.forceFrom()).isEqualTo(LocalDate.of(2026, 8, 2));
    assertThat(article50.status()).isEqualTo(ForceStatus.IN_FORCE);
  }

  @Test
  void dataActArticle3Paragraph1AppliesToProductsPlacedAfter12Sep2026() {
    ForceAssignment assignment = rules.assign(
        "02023R2854-20231222",
        provision("", "3", "1", ""),
        asOf);

    assertThat(assignment.forceFrom()).isEqualTo(LocalDate.of(2026, 9, 12));
    assertThat(assignment.status()).isEqualTo(ForceStatus.IN_FORCE);
    assertThat(assignment.scopeNote()).contains("placed on the market after 12 Sep 2026");
  }

  @Test
  void dataActArticle3Paragraph1IsFutureBeforeItsPlacementDate() {
    ForceAssignment assignment = rules.assign(
        "32023R2854",
        provision("", "3", "1", ""),
        LocalDate.of(2026, 9, 1));

    assertThat(assignment.status()).isEqualTo(ForceStatus.FUTURE);
  }

  @Test
  void otherActsUseTheirApplicationDates() {
    assertThat(rules.assign("02016R0679-20160504", provision("", "5", "1", ""), asOf).forceFrom())
        .isEqualTo(LocalDate.of(2018, 5, 25));
    assertThat(rules.assign("32022R2554", provision("", "1", "", ""), asOf).forceFrom())
        .isEqualTo(LocalDate.of(2025, 1, 17));
    assertThat(rules.assign("02022R2065-20221027", provision("", "1", "", ""), asOf).forceFrom())
        .isEqualTo(LocalDate.of(2024, 2, 17));
    assertThat(rules.assign("02023R2854-20231222", provision("", "1", "", ""), asOf).forceFrom())
        .isEqualTo(LocalDate.of(2025, 9, 12));
    assertThat(rules.assign("32026R1744", provision("", "4", "", ""), asOf).forceFrom())
        .isEqualTo(LocalDate.of(2026, 7, 27));
    assertThat(rules.assign("32026R1744", provision("", "4", "", ""), asOf).status())
        .isEqualTo(ForceStatus.IN_FORCE);
  }

  private static ParsedProvision provision(String annex, String article, String paragraph, String point) {
    String slot = annex.isBlank() ? article : annex;
    return new ParsedProvision(
        "celex#" + slot + ":" + paragraph + ":" + point,
        article,
        paragraph,
        point,
        annex,
        "text");
  }
}
