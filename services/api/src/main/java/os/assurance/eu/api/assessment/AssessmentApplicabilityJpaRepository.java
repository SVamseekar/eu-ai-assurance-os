package os.assurance.eu.api.assessment;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentApplicabilityJpaRepository
    extends JpaRepository<AssessmentApplicabilityEntity, UUID> {
  Optional<AssessmentApplicabilityEntity> findByTenantIdAndSystemIdAndProposalId(
      UUID tenantId, UUID systemId, UUID proposalId);
}
