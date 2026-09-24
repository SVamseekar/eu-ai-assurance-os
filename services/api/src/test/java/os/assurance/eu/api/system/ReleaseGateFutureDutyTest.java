package os.assurance.eu.api.system;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import os.assurance.eu.api.conformity.ConformityService;
import os.assurance.eu.api.control.Control;
import os.assurance.eu.api.control.ControlEntity;
import os.assurance.eu.api.control.ControlJpaRepository;
import os.assurance.eu.api.control.ControlStatus;
import os.assurance.eu.api.control.SystemControlEntity;
import os.assurance.eu.api.control.SystemControlJpaRepository;
import os.assurance.eu.api.corpus.CorpusForceLookup;
import os.assurance.eu.api.determination.Applicability;
import os.assurance.eu.api.determination.DeterminationObligationEntity;
import os.assurance.eu.api.determination.DeterminationObligationJpaRepository;
import os.assurance.eu.api.determination.DeterminationRunEntity;
import os.assurance.eu.api.determination.DeterminationRunJpaRepository;
import os.assurance.eu.api.tenant.TenantContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReleaseGateFutureDutyTest {
  private static final UUID TENANT = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID SYSTEM = UUID.fromString("00000000-0000-0000-0000-000000000201");
  private static final UUID CONTROL = UUID.fromString("00000000-0000-0000-0000-000000000301");
  private static final UUID RUN = UUID.fromString("00000000-0000-0000-0000-000000000401");

  @Mock private SystemControlJpaRepository systemControls;
  @Mock private ControlJpaRepository controls;
  @Mock private TenantContext tenantContext;
  @Mock private DeterminationRunJpaRepository determinationRuns;
  @Mock private DeterminationObligationJpaRepository determinationObligations;
  @Mock private ConformityService conformityService;

  private void stubRepos() {
    when(tenantContext.tenantId()).thenReturn(TENANT);
    when(controls.findAll()).thenReturn(List.of(new ControlEntity(new Control(
        CONTROL, "RISK_MANAGEMENT", "Risk", "Risk", "HIGH", "risk"))));
    when(systemControls.findAllByTenantIdAndSystemIdAndStatus(TENANT, SYSTEM, ControlStatus.BLOCKED))
        .thenReturn(List.of());
    when(systemControls.findAllByTenantIdAndSystemIdOrderByUpdatedAtDesc(TENANT, SYSTEM))
        .thenReturn(List.of(new SystemControlEntity(
            TENANT, UUID.randomUUID(), SYSTEM, CONTROL, ControlStatus.REVIEW, true, null, null, Instant.now())));
    when(determinationRuns.findFirstByTenantIdAndSystemIdOrderByCreatedAtDesc(TENANT, SYSTEM))
        .thenReturn(Optional.of(new DeterminationRunEntity(
            RUN, TENANT, SYSTEM, Map.of(), Map.of(), "COMPLETED", "v2", null, Instant.now())));
  }

  @Test
  void futureDatedAnnexIiiDutyIsNotACurrentBlocker() {
    stubRepos();
    stubObligation("ESSENTIAL_SERVICE_ACCESS", "Art. 6 / Annex III (essential private services) — indicative");
    ReleaseGateService service = service(refs -> refs.contains("Annex III"));

    ReleaseGateResponse response = service.calculate(system());

    assertThat(response.decision()).isEqualTo(ReleaseDecision.PASS);
    assertThat(response.blockers()).noneMatch(blocker -> blocker.startsWith("OBLIGATION_UNMET"));
  }

  @Test
  void inForceDutyStaysABlockerWhenItsDateIsVisible() {
    stubRepos();
    stubObligation("TRANSPARENCY_NATURAL_PERSONS", "Art. 50 — indicative (in force 2 Aug 2026)");
    ReleaseGateService service = service(refs -> false);

    ReleaseGateResponse response = service.calculate(system());

    assertThat(response.decision()).isEqualTo(ReleaseDecision.BLOCKED);
    assertThat(response.blockers()).anyMatch(blocker -> blocker.startsWith("OBLIGATION_UNMET:TRANSPARENCY_NATURAL_PERSONS"));
  }

  @Test
  void hiddenForceDateDoesNotChangeTheExistingBlocker() {
    stubRepos();
    stubObligation("ESSENTIAL_SERVICE_ACCESS", "Art. 6 / Annex III (essential private services) — indicative");
    ReleaseGateService service = service(refs -> false);

    ReleaseGateResponse response = service.calculate(system());

    assertThat(response.blockers()).anyMatch(blocker -> blocker.contains("ESSENTIAL_SERVICE_ACCESS"));
  }

  @Test
  void evalBreachStillBlocksWhenADutyIsFuture() {
    ReleaseGateService service = new ReleaseGateService(
        null, null, null, null, null, null, refs -> true);
    AiSystem belowEval = new AiSystem(
        SYSTEM, "Test", "Owner", "Purpose", RiskClass.LIMITED, "basis", "EU",
        90, 70, DataContractStatus.HEALTHY, ReleaseDecision.REVIEW, List.of(),
        null, null, null, List.of(), null, null, List.of(), Instant.now(), Instant.now());

    ReleaseGateResponse response = service.calculate(belowEval);

    assertThat(response.decision()).isEqualTo(ReleaseDecision.BLOCKED);
    assertThat(response.blockers()).contains("Eval score is below hard release threshold");
  }

  private void stubObligation(String code, String legalRefs) {
    when(determinationObligations.findAllByRunIdOrderByRuleCodeAsc(RUN)).thenReturn(List.of(
        new DeterminationObligationEntity(
            UUID.randomUUID(),
            RUN,
            code,
            Applicability.APPLICABLE,
            "mapped",
            List.of("RISK_MANAGEMENT"),
            legalRefs,
            code,
            "HIGH")));
  }

  private ReleaseGateService service(CorpusForceLookup lookup) {
    return new ReleaseGateService(
        systemControls,
        controls,
        tenantContext,
        determinationRuns,
        determinationObligations,
        conformityService,
        lookup);
  }

  private static AiSystem system() {
    Instant now = Instant.now();
    return new AiSystem(
        SYSTEM, "Test", "Owner", "Purpose", RiskClass.LIMITED, "basis", "EU",
        90, 90, DataContractStatus.HEALTHY, ReleaseDecision.REVIEW, List.of(),
        null, null, null, List.of(), null, null, List.of(), now, now);
  }
}
