package os.assurance.eu.api.proposal;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class RulesDocumentMapperTest {
  private static final LawProvision ANNEX_III = new LawProvision(
      "02024R1689-20260727#annexIII::",
      "High-risk AI systems pursuant to Article 6(2).",
      "FUTURE",
      LocalDate.of(2027, 12, 2));
  private static final LawProvision ART_50 = new LawProvision(
      "02024R1689-20260727#50:1:",
      "Providers shall ensure that natural persons are informed that they interact with an AI system.",
      "IN_FORCE",
      LocalDate.of(2026, 8, 2));
  private static final LawProvision GDPR_5 = new LawProvision(
      "02016R0679-20160504#5:1:",
      "Personal data shall be processed lawfully, fairly and in a transparent manner.",
      "IN_FORCE",
      LocalDate.of(2018, 5, 25));
  private static final LawProvision DORA_1 = new LawProvision(
      "02022R2554-20221227#1:1:",
      "This Regulation lays down uniform requirements concerning the security of network and information systems supporting the business processes of financial entities.",
      "IN_FORCE",
      LocalDate.of(2025, 1, 17));

  private final List<LawProvision> lawIndex = List.of(ANNEX_III, ART_50, GDPR_5, DORA_1);
  private final RulesDocumentMapper mapper = new RulesDocumentMapper();

  @Test
  void modelCardRetrievesFutureAnnexIii() {
    MappingDraft draft = mapper.map(
        "Model card",
        "Model card for a credit-scoring assistant. The system is a high-risk AI system listed in Annex III.",
        lawIndex);

    assertThat(draft.relation()).isEqualTo("supports");
    assertThat(draft.provisionKey()).isEqualTo(ANNEX_III.provisionKey());
    assertThat(draft.forceStatus()).isEqualTo("FUTURE");
    assertThat(draft.forceFrom()).isEqualTo(LocalDate.of(2027, 12, 2));
  }

  @Test
  void approvalNoteRetrievesInForceArticle50() {
    MappingDraft draft = mapper.map(
        "Approval note",
        "Approval note: natural persons are informed that they interact with an AI system before the release.",
        lawIndex);

    assertThat(draft.relation()).isEqualTo("supports");
    assertThat(draft.provisionKey()).isEqualTo(ART_50.provisionKey());
    assertThat(draft.forceStatus()).isEqualTo("IN_FORCE");
  }

  @Test
  void datasetNoteRetrievesInForceGdpr() {
    MappingDraft draft = mapper.map(
        "Dataset note",
        "Dataset note. Training rows are personal data and shall be processed lawfully, fairly and in a transparent manner.",
        lawIndex);

    assertThat(draft.provisionKey()).isEqualTo(GDPR_5.provisionKey());
    assertThat(draft.forceStatus()).isEqualTo("IN_FORCE");
  }

  @Test
  void evalSummaryRetrievesInForceDora() {
    MappingDraft draft = mapper.map(
        "Eval summary",
        "Eval summary for security of network and information systems supporting the business processes of financial entities. Score 91.",
        lawIndex);

    assertThat(draft.provisionKey()).isEqualTo(DORA_1.provisionKey());
    assertThat(draft.forceStatus()).isEqualTo("IN_FORCE");
  }

  @Test
  void retentionPolicyRetrievesInForceGdpr() {
    MappingDraft draft = mapper.map(
        "Retention policy",
        "Retention policy. Personal data shall be processed lawfully and kept only for the stated retention window.",
        lawIndex);

    assertThat(draft.provisionKey()).isEqualTo(GDPR_5.provisionKey());
    assertThat(draft.relation()).isEqualTo("supports");
  }

  @Test
  void noCandidateWritesAbstainAndCitesNothing() {
    MappingDraft draft = mapper.map(
        "Office lunch rota",
        "Sandwiches on Tuesday. No statute, no personal data, no model.",
        lawIndex);

    assertThat(draft.relation()).isEqualTo("abstain");
    assertThat(draft.provisionKey()).isNull();
  }

  @Test
  void retrievalIgnoresTextThatIsNotInTheLawIndex() {
    LawProvision onlyGdpr = GDPR_5;
    MappingDraft draft = mapper.map(
        "Model card",
        "High-risk Annex III credit scoring. Tenant evidence chunk: secret customer file.",
        List.of(onlyGdpr));

    assertThat(draft.relation()).isEqualTo("abstain");
    assertThat(draft.provisionKey()).isNull();
  }
}
