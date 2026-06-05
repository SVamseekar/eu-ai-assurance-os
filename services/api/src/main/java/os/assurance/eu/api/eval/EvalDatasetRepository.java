package os.assurance.eu.api.eval;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import os.assurance.eu.api.tenant.TenantContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class EvalDatasetRepository {
  private final EvalDatasetJpaRepository repository;
  private final TenantContext tenantContext;

  public EvalDatasetRepository(EvalDatasetJpaRepository repository, TenantContext tenantContext) {
    this.repository = repository;
    this.tenantContext = tenantContext;
  }

  @Transactional(readOnly = true)
  public List<EvalDataset> findAll() {
    return repository.findAllByTenantIdOrderByCreatedAtAsc(tenantContext.tenantId()).stream()
        .map(EvalDatasetEntity::toDomain)
        .toList();
  }

  @Transactional(readOnly = true)
  public Optional<EvalDataset> findById(UUID id) {
    return repository.findByTenantIdAndId(tenantContext.tenantId(), id)
        .map(EvalDatasetEntity::toDomain);
  }

  @Transactional(readOnly = true)
  public Optional<EvalDataset> findByName(String name) {
    return repository.findFirstByTenantIdAndNameOrderByCreatedAtDesc(tenantContext.tenantId(), name)
        .map(EvalDatasetEntity::toDomain);
  }

  @Transactional(readOnly = true)
  public boolean existsByNameAndVersion(String name, String version) {
    return repository.existsByTenantIdAndNameAndVersion(tenantContext.tenantId(), name, version);
  }

  @Transactional
  public EvalDataset save(EvalDataset dataset) {
    return repository.save(new EvalDatasetEntity(tenantContext.tenantId(), dataset)).toDomain();
  }
}
