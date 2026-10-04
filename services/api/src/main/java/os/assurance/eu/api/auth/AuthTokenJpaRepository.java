package os.assurance.eu.api.auth;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuthTokenJpaRepository extends JpaRepository<AuthTokenEntity, UUID> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select t from AuthTokenEntity t where t.tokenHash = :hash")
  Optional<AuthTokenEntity> findForUpdateByHash(@Param("hash") String hash);
}
