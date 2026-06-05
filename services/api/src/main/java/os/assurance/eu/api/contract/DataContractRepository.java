package os.assurance.eu.api.contract;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import os.assurance.eu.api.tenant.TenantContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class DataContractRepository {
  private final DataContractJpaRepository repository;
  private final TenantContext tenantContext;

  public DataContractRepository(DataContractJpaRepository repository, TenantContext tenantContext) {
    this.repository = repository;
    this.tenantContext = tenantContext;
  }

  @Transactional(readOnly = true)
  public List<DataContract> findAll() {
    return repository.findAllByTenantIdOrderByCreatedAtAsc(tenantContext.tenantId()).stream()
        .map(DataContractEntity::toDomain)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<DataContract> findAllBySystemId(UUID systemId) {
    return repository.findAllByTenantIdAndSystemIdOrderByCreatedAtAsc(tenantContext.tenantId(), systemId).stream()
        .map(DataContractEntity::toDomain)
        .toList();
  }

  @Transactional(readOnly = true)
  public Optional<DataContract> findById(UUID id) {
    return repository.findByTenantIdAndId(tenantContext.tenantId(), id)
        .map(DataContractEntity::toDomain);
  }

  @Transactional(readOnly = true)
  public boolean existsBySystemNameAndVersion(UUID systemId, String name, String version) {
    return repository.existsByTenantIdAndSystemIdAndNameAndVersion(
        tenantContext.tenantId(), systemId, name, version);
  }

  @Transactional
  public DataContract save(DataContract contract) {
    return repository.save(new DataContractEntity(tenantContext.tenantId(), contract)).toDomain();
  }
}
