package os.assurance.eu.api.system;

import java.util.Objects;
import os.assurance.eu.api.proposal.MappingProposalService;
import os.assurance.eu.api.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LinkedControlReopenService {
  private final SystemChangeScopeJpaRepository scopes;
  private final MappingProposalService proposals;
  private final TenantContext tenantContext;

  public LinkedControlReopenService(
      SystemChangeScopeJpaRepository scopes,
      MappingProposalService proposals,
      TenantContext tenantContext) {
    this.scopes = scopes;
    this.proposals = proposals;
    this.tenantContext = tenantContext;
  }

  @Transactional
  public void reopenIfSensitiveChange(AiSystem existing, UpdateAiSystemRequest request) {
    SystemChangeScopeEntity scope = scopes.findById(existing.id()).orElse(null);
    boolean changed = differs(request.modelName(), existing.modelName())
        || differs(request.modelVersion(), existing.modelVersion())
        || differs(request.purpose(), existing.purpose())
        || differs(request.deploymentRegion(), existing.deploymentRegion())
        || differs(request.prompt(), scope == null ? null : scope.prompt())
        || differs(request.retrievalCorpus(), scope == null ? null : scope.retrievalCorpus())
        || differs(request.retention(), scope == null ? null : scope.retention())
        || differs(request.humanReviewLogic(), scope == null ? null : scope.humanReviewLogic());
    if (request.prompt() != null || request.retrievalCorpus() != null
        || request.retention() != null || request.humanReviewLogic() != null) {
      SystemChangeScopeEntity row = scope == null
          ? new SystemChangeScopeEntity(existing.id(), tenantContext.tenantId())
          : scope;
      row.replace(
          request.prompt() == null ? row.prompt() : request.prompt(),
          request.retrievalCorpus() == null ? row.retrievalCorpus() : request.retrievalCorpus(),
          request.retention() == null ? row.retention() : request.retention(),
          request.humanReviewLogic() == null ? row.humanReviewLogic() : request.humanReviewLogic());
      scopes.save(row);
    }
    if (changed) {
      proposals.reopenAcceptedLinks(existing.id());
    }
  }

  private static boolean differs(String incoming, String current) {
    return incoming != null && !Objects.equals(incoming, current);
  }
}
