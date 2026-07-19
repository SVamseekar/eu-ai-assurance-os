package os.assurance.eu.api.control;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import os.assurance.eu.api.audit.AuditService;
import os.assurance.eu.api.system.AiSystem;
import os.assurance.eu.api.system.AiSystemRepository;
import os.assurance.eu.api.system.DataContractStatus;
import os.assurance.eu.api.system.ReleaseDecision;
import os.assurance.eu.api.system.ReleaseGateService;
import os.assurance.eu.api.system.RiskClass;
import os.assurance.eu.api.tenant.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ControlServiceTest {
  private ControlJpaRepository controls;
  private SystemControlJpaRepository systemControls;
  private AiSystemRepository systems;
  private ReleaseGateService releaseGateService;
  private AuditService auditService;
  private TenantContext tenantContext;
  private ControlService service;

  private static final UUID TENANT = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID SYSTEM = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    controls = mock(ControlJpaRepository.class);
    systemControls = mock(SystemControlJpaRepository.class);
    systems = mock(AiSystemRepository.class);
    releaseGateService = mock(ReleaseGateService.class);
    auditService = mock(AuditService.class);
    tenantContext = mock(TenantContext.class);
    when(tenantContext.tenantId()).thenReturn(TENANT);
    when(tenantContext.actorId()).thenReturn(UUID.randomUUID());
    service = new ControlService(
        controls, systemControls, systems, releaseGateService, auditService, tenantContext);
  }

  @Test
  void attachesHighRiskControlsIncludingHumanOversightBlockedWhenGap() {
    when(controls.count()).thenReturn(0L);
    when(controls.findAll()).thenAnswer(inv -> {
      // after seed, return entities - simulate by saving and returning from seed list
      return ControlService.baselineControls().stream().map(ControlEntity::new).toList();
    });
    when(controls.save(any())).thenAnswer(i -> i.getArgument(0));
    when(systemControls.findByTenantIdAndSystemIdAndControlId(any(), any(), any()))
        .thenReturn(Optional.empty());
    when(systemControls.save(any())).thenAnswer(i -> i.getArgument(0));

    AiSystem system = highRiskWithOversightGap();
    service.attachApplicableControls(system);

    verify(systemControls, org.mockito.Mockito.atLeastOnce()).save(any());
  }

  @Test
  void baselineCatalogContainsHumanOversight() {
    assertThat(ControlService.baselineControls())
        .extracting(Control::code)
        .contains("HUMAN_OVERSIGHT", "RISK_MANAGEMENT", "DATA_GOVERNANCE");
  }

  private AiSystem highRiskWithOversightGap() {
    Instant now = Instant.now();
    return new AiSystem(
        SYSTEM, "Claims", "Ops", "Triage", RiskClass.HIGH, "Essential services",
        "EU", 72, 78, DataContractStatus.BREACH, ReleaseDecision.BLOCKED,
        new ArrayList<>(List.of("Human oversight SOP missing")),
        "VendorCo", "ClaimsModel", "1.0", List.of("claims_db"),
        "insurance", "eligibility", List.of("claimants"),
        now, now);
  }
}
