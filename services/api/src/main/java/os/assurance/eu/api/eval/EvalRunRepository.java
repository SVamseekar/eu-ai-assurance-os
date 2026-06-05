package os.assurance.eu.api.eval;

import java.util.Optional;
import java.util.UUID;
import os.assurance.eu.api.tenant.TenantContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class EvalRunRepository {
  private final EvalRunJpaRepository repository;
  private final TenantContext tenantContext;

  public EvalRunRepository(EvalRunJpaRepository repository, TenantContext tenantContext) {
    this.repository = repository;
    this.tenantContext = tenantContext;
  }

  @Transactional
  public EvalRun save(EvalRun evalRun) {
    return repository.save(new EvalRunEntity(tenantContext.tenantId(), evalRun)).toDomain();
  }

  @Transactional(readOnly = true)
  public Optional<EvalRun> findById(UUID runId) {
    return repository.findByTenantIdAndId(tenantContext.tenantId(), runId)
        .map(EvalRunEntity::toDomain);
  }
}
