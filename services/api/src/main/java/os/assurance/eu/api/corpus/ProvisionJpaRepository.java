package os.assurance.eu.api.corpus;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProvisionJpaRepository extends JpaRepository<ProvisionEntity, UUID> {
  List<ProvisionEntity> findAllByInstrumentIdInOrderByProvisionKeyAsc(Collection<UUID> instrumentIds);

  boolean existsByProvisionKey(String provisionKey);
}
