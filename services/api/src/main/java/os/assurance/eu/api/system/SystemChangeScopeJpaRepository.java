package os.assurance.eu.api.system;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemChangeScopeJpaRepository extends JpaRepository<SystemChangeScopeEntity, UUID> {
}
