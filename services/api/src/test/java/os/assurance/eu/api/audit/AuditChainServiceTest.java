package os.assurance.eu.api.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import os.assurance.eu.api.observability.AssuranceMetrics;
import os.assurance.eu.api.tenant.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AuditChainServiceTest {
  private AuditEventJpaRepository repository;
  private TenantContext tenantContext;
  private AuditChainHasher hasher;
  private AuditService service;

  private static final UUID TENANT = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID ACTOR = UUID.fromString("00000000-0000-0000-0000-000000000101");

  @BeforeEach
  void setUp() {
    repository = mock(AuditEventJpaRepository.class);
    tenantContext = mock(TenantContext.class);
    when(tenantContext.tenantId()).thenReturn(TENANT);
    when(tenantContext.actorId()).thenReturn(ACTOR);
    hasher = new AuditChainHasher("test-audit-chain-secret");
    service = new AuditService(repository, tenantContext, hasher, mock(AssuranceMetrics.class), 7);
  }

  @Test
  void appendChainsHashesAndSetsSevenYearRetention() {
    when(repository.findLatestByTenantId(TENANT)).thenReturn(Optional.empty());
    ArgumentCaptor<AuditEventEntity> captor = ArgumentCaptor.forClass(AuditEventEntity.class);
    when(repository.save(captor.capture())).thenAnswer(i -> i.getArgument(0));

    AuditEvent first = service.append(null, "test.a", "x", "1", Map.of("k", "v"));
    assertThat(first.prevEventHash()).isNull();
    assertThat(first.eventHash()).isNotBlank();
    assertThat(first.retainUntil()).isAfter(Instant.now().plusSeconds(3600L * 24 * 365 * 6));

    when(repository.findLatestByTenantId(TENANT)).thenReturn(Optional.of(captor.getValue()));
    AuditEvent second = service.append(null, "test.b", "x", "2", Map.of("k", "v2"));
    assertThat(second.prevEventHash()).isEqualTo(first.eventHash());
    assertThat(second.eventHash()).isNotEqualTo(first.eventHash());
  }

  @Test
  void verifyDetectsTamperedPayload() {
    AuditChainHasher local = new AuditChainHasher("test-audit-chain-secret");
    UUID id1 = UUID.randomUUID();
    Instant t1 = Instant.parse("2026-01-01T00:00:00Z");
    String h1 = local.hash(TENANT, id1, null, ACTOR, "a", "r", "1", Map.of("x", 1), t1);
    AuditEventEntity e1 = new AuditEventEntity(
        id1, TENANT, null, ACTOR, "a", "r", "1", Map.of("x", 1), t1, null, h1,
        t1.plusSeconds(3600L * 24 * 365 * 7));

    UUID id2 = UUID.randomUUID();
    Instant t2 = Instant.parse("2026-01-01T00:00:01Z");
    String h2 = local.hash(TENANT, id2, h1, ACTOR, "b", "r", "2", Map.of("x", 2), t2);
    // tamper payload while keeping stored hash
    AuditEventEntity e2 = new AuditEventEntity(
        id2, TENANT, null, ACTOR, "b", "r", "2", Map.of("x", 999), t2, h1, h2,
        t2.plusSeconds(3600L * 24 * 365 * 7));

    when(repository.findAllByTenantIdOrderByCreatedAtAsc(TENANT)).thenReturn(List.of(e1, e2));
    AuditChainVerifyResponse result = service.verifyChain();
    assertThat(result.valid()).isFalse();
    assertThat(result.firstBreakId()).isEqualTo(id2);
  }
}
