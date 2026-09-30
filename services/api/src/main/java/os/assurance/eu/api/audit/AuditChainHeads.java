package os.assurance.eu.api.audit;

import java.time.Instant;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Creates the per-tenant chain head. {@link #ensureExists} commits in its own transaction so a
 * concurrent insert race does not poison the caller's transaction. {@link #attachToNewTenant}
 * runs in the caller's transaction so the tenant row is visible to the foreign key.
 */
@Component
public class AuditChainHeads {
  private final AuditChainHeadJpaRepository heads;
  private final TransactionTemplate requiresNew;

  public AuditChainHeads(AuditChainHeadJpaRepository heads, PlatformTransactionManager transactions) {
    this.heads = heads;
    this.requiresNew = new TransactionTemplate(transactions);
    this.requiresNew.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
  }

  public void ensureExists(UUID tenantId) {
    if (heads.existsById(tenantId)) {
      return;
    }
    try {
      requiresNew.executeWithoutResult(status -> {
        if (!heads.existsById(tenantId)) {
          heads.saveAndFlush(new AuditChainHeadEntity(tenantId, null, Instant.now()));
        }
      });
    } catch (DataIntegrityViolationException raced) {
      // another request created it first — fine
    }
  }

  @Transactional
  public void attachToNewTenant(UUID tenantId) {
    if (!heads.existsById(tenantId)) {
      heads.save(new AuditChainHeadEntity(tenantId, null, Instant.now()));
    }
  }
}
