package os.assurance.eu.api.system;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import os.assurance.eu.api.tenant.TenantContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class AiSystemRepository {
  private final AiSystemJpaRepository repository;
  private final TenantContext tenantContext;

  public AiSystemRepository(AiSystemJpaRepository repository, TenantContext tenantContext) {
    this.repository = repository;
    this.tenantContext = tenantContext;
  }

  @Transactional(readOnly = true)
  public List<AiSystem> findAll() {
    return repository.findAllByTenantIdOrderByCreatedAtAsc(tenantContext.tenantId()).stream()
        .map(AiSystemEntity::toDomain)
        .toList();
  }

  @Transactional(readOnly = true)
  public Optional<AiSystem> findById(UUID id) {
    return repository.findByTenantIdAndId(tenantContext.tenantId(), id)
        .map(AiSystemEntity::toDomain);
  }

  @Transactional
  public AiSystem save(AiSystem system) {
    return repository.save(new AiSystemEntity(tenantContext.tenantId(), system)).toDomain();
  }
}
