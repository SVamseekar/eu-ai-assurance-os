package os.assurance.eu.api.billing;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GateRunCounterJpaRepository extends JpaRepository<GateRunCounterEntity, GateRunCounterEntity.Key> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select c from GateRunCounterEntity c where c.tenantId = :tenantId and c.period = :period")
  Optional<GateRunCounterEntity> findForUpdate(@Param("tenantId") UUID tenantId, @Param("period") String period);

  @Query("select coalesce(sum(c.runs), 0) from GateRunCounterEntity c where c.tenantId = :tenantId and c.period = :period")
  int runsIn(@Param("tenantId") UUID tenantId, @Param("period") String period);
}
