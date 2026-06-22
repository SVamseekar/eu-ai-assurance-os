package os.assurance.eu.api.auth;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SigningKeyJpaRepository extends JpaRepository<SigningKeyEntity, UUID> {
    Optional<SigningKeyEntity> findByActiveTrue();
}