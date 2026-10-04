package os.assurance.eu.api.auth;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuthTokenJpaRepository extends JpaRepository<AuthTokenEntity, UUID> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select t from AuthTokenEntity t where t.tokenHash = :hash")
  Optional<AuthTokenEntity> findForUpdateByHash(@Param("hash") String hash);

  @Query("select count(t) from AuthTokenEntity t where t.userId = :userId and t.purpose = :purpose and t.createdAt > :since")
  long countIssuedSince(@Param("userId") UUID userId, @Param("purpose") AuthTokenPurpose purpose,
      @Param("since") Instant since);

  @Modifying(flushAutomatically = true)
  @Query("update AuthTokenEntity t set t.usedAt = :now where t.userId = :userId and t.purpose = :purpose and t.usedAt is null")
  int markOutstandingUsed(@Param("userId") UUID userId, @Param("purpose") AuthTokenPurpose purpose,
      @Param("now") Instant now);
}
