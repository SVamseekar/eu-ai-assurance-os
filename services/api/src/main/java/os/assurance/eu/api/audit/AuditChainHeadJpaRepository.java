package os.assurance.eu.api.audit;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditChainHeadJpaRepository extends JpaRepository<AuditChainHeadEntity, UUID> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select h from AuditChainHeadEntity h where h.tenantId = :tenantId")
  Optional<AuditChainHeadEntity> findForUpdate(@Param("tenantId") UUID tenantId);
}
