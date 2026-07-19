package os.assurance.eu.api.regmonitor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import os.assurance.eu.api.audit.AuditService;
import os.assurance.eu.api.control.ControlService;
import os.assurance.eu.api.control.ControlStatus;
import os.assurance.eu.api.control.SystemControl;
import os.assurance.eu.api.system.AiSystem;
import os.assurance.eu.api.system.AiSystemRepository;
import os.assurance.eu.api.system.DataContractStatus;
import os.assurance.eu.api.system.ReleaseDecision;
import os.assurance.eu.api.system.RiskClass;
import os.assurance.eu.api.tenant.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RegMonitorServiceTest {
  private static final UUID TENANT = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID ACTOR = UUID.fromString("00000000-0000-0000-0000-000000000101");
  private static final UUID SYSTEM = UUID.fromString("00000000-0000-4000-8000-000000000099");
  private static final UUID ITEM = UUID.fromString("a1600002-0000-4000-8000-000000000010");
  private static final UUID SOURCE = UUID.fromString("a1600001-0000-4000-8000-000000000001");

  private RegItemJpaRepository items;
  private RegImpactHintJpaRepository hints;
  private RegItemReviewJpaRepository reviews;
  private RegSourceJpaRepository sources;
  private AiSystemRepository systems;
  private ControlService controlService;
  private TenantContext tenantContext;
  private AuditService auditService;
  private RegMonitorService service;
  private final Instant now = Instant.parse("2026-07-20T12:00:00Z");

  @BeforeEach
  void setUp() {
    items = mock(RegItemJpaRepository.class);
    hints = mock(RegImpactHintJpaRepository.class);
    reviews = mock(RegItemReviewJpaRepository.class);
    sources = mock(RegSourceJpaRepository.class);
    systems = mock(AiSystemRepository.class);
    controlService = mock(ControlService.class);
    tenantContext = mock(TenantContext.class);
    auditService = mock(AuditService.class);
    when(tenantContext.tenantId()).thenReturn(TENANT);
    when(tenantContext.actorId()).thenReturn(ACTOR);

    service = new RegMonitorService(
        items,
        hints,
        reviews,
        sources,
        systems,
        controlService,
        tenantContext,
        auditService,
        Clock.fixed(now, ZoneOffset.UTC));
  }

  @Test
  void markReviewedDoesNotTouchRiskOrControls() {
    RegItemEntity entity = new RegItemEntity(
        ITEM, SOURCE, "ext-1", "Title", "Summary", now, "https://example.invalid/1",
        "abc", now);
    when(items.findById(ITEM)).thenReturn(Optional.of(entity));
    when(reviews.findByTenantIdAndRegItemId(TENANT, ITEM)).thenReturn(Optional.empty());
    when(reviews.save(any())).thenAnswer(inv -> {
      RegItemReviewEntity saved = inv.getArgument(0);
      when(reviews.findAllByTenantIdAndRegItemIdIn(eq(TENANT), any()))
          .thenReturn(List.of(saved));
      return saved;
    });
    when(reviews.findAllByTenantIdAndRegItemIdIn(eq(TENANT), any())).thenReturn(List.of());
    when(hints.findAllByRegItemIdIn(any())).thenReturn(List.of());
    when(sources.findAll()).thenReturn(List.of());

    RegItemResponse response = service.markReviewed(ITEM, "Reviewed by counsel");

    assertThat(response.reviewed()).isTrue();
    verify(auditService).append(
        eq(null),
        eq("reg_item.reviewed"),
        eq("reg_item"),
        eq(ITEM.toString()),
        any());
    // Never touches systems or controls
    verify(systems, never()).save(any());
    verify(controlService, never()).openControlsForReview(any(), any());
  }

  @Test
  void relevantMatchesInsuranceSector() {
    when(systems.findById(SYSTEM)).thenReturn(Optional.of(system("Insurance Ops", "claims routing")));
    when(controlService.listForSystem(SYSTEM)).thenReturn(List.of(
        new SystemControl(
            UUID.randomUUID(), SYSTEM, UUID.randomUUID(), "HUMAN_OVERSIGHT", "Human oversight",
            "OVERSIGHT", ControlStatus.REVIEW, true, null, null, now)));

    RegItemEntity item = new RegItemEntity(
        ITEM, SOURCE, "ext-ins", "Insurance eligibility AI guidance",
        "High-risk essential private services themes", now,
        "https://example.invalid/ins", "hash1", now);
    when(items.findAllByOrderByFetchedAtDesc()).thenReturn(List.of(item));
    when(hints.findAllByRegItemIdIn(any())).thenReturn(List.of(
        new RegImpactHintEntity(new RegImpactHint(
            UUID.randomUUID(), ITEM, "RISK_MANAGEMENT", "ESSENTIAL_SERVICE_ACCESS",
            ImpactLevel.UNCERTAIN, "hint", now))));
    when(reviews.findAllByTenantIdAndRegItemIdIn(eq(TENANT), any())).thenReturn(List.of());
    when(sources.findAll()).thenReturn(List.of(new RegSourceEntity(new RegSource(
        SOURCE, "CURATED_BOOTSTRAP", "Bootstrap", "classpath:x", FeedType.STATIC_FIXTURE,
        3600, true, null, null, now))));

    RegMonitorFeedResponse feed = service.listRelevantForSystem(SYSTEM);
    assertThat(feed.items()).hasSize(1);
    assertThat(feed.items().get(0).relevanceReason()).isNotBlank();
    assertThat(feed.disclaimer()).contains("not an official");
  }

  private AiSystem system(String owner, String purpose) {
    return new AiSystem(
        SYSTEM,
        "Claims Triage AI",
        owner,
        purpose,
        RiskClass.HIGH,
        "Essential services",
        "EU",
        80,
        90,
        DataContractStatus.HEALTHY,
        ReleaseDecision.REVIEW,
        List.of(),
        null,
        null,
        null,
        List.of(),
        "insurance",
        "eligibility",
        List.of("claimants"),
        now,
        now);
  }
}
