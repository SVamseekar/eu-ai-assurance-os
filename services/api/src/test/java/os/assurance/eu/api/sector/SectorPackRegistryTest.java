package os.assurance.eu.api.sector;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class SectorPackRegistryTest {

  @Test
  void enablesConfiguredPacksAndResolvesInsuranceSector() {
    SectorProperties properties = new SectorProperties();
    properties.setPacks("insurance,hr,finance");
    SectorPackRegistry registry = new SectorPackRegistry(
        List.of(new InsuranceSectorPack(), new HrSectorPack(), new FinanceSectorPack()),
        properties);

    assertThat(registry.enabledPacks()).extracting(SectorPack::id)
        .containsExactly("insurance", "hr", "finance");
    assertThat(registry.resolveForSector("INSURANCE")).map(SectorPack::id).contains("insurance");
    assertThat(registry.resolveForSector("claims")).map(SectorPack::id).contains("insurance");
    assertThat(registry.resolveForSector("employment")).map(SectorPack::id).contains("hr");
    assertThat(registry.resolveForSector("kyc")).map(SectorPack::id).contains("finance");
    assertThat(registry.resolveForSector("healthcare")).isEmpty();
  }

  @Test
  void canDisablePacksViaConfig() {
    SectorProperties properties = new SectorProperties();
    properties.setPacks("insurance");
    SectorPackRegistry registry = new SectorPackRegistry(
        List.of(new InsuranceSectorPack(), new HrSectorPack(), new FinanceSectorPack()),
        properties);

    assertThat(registry.enabledPacks()).extracting(SectorPack::id).containsExactly("insurance");
    assertThat(registry.resolveForSector("hr")).isEmpty();
  }

  @Test
  void insurancePackExposesClaimsControlsAndTemplates() {
    InsuranceSectorPack pack = new InsuranceSectorPack();
    assertThat(pack.extraControls()).extracting(SectorControlDef::code)
        .contains(
            "INS_CLAIMS_FAIRNESS",
            "INS_ADVERSE_DECISION_REVIEW",
            "INS_CLAIMS_EXPLAINABILITY",
            "INS_MODEL_CARD_CLAIMS");
    assertThat(pack.questionnaireDefaults()).containsEntry("sector", "insurance");
    assertThat(pack.sampleEvidenceTemplates()).isNotEmpty();
  }
}
