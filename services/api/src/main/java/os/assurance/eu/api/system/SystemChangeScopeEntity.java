package os.assurance.eu.api.system;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "system_change_scope")
public class SystemChangeScopeEntity {
  @Id
  private UUID systemId;

  @Column(nullable = false)
  private UUID tenantId;

  @Column(length = 4000)
  private String prompt;

  @Column(length = 4000)
  private String retrievalCorpus;

  @Column(length = 4000)
  private String retention;

  @Column(length = 4000)
  private String humanReviewLogic;

  protected SystemChangeScopeEntity() {
  }

  public SystemChangeScopeEntity(UUID systemId, UUID tenantId) {
    this.systemId = systemId;
    this.tenantId = tenantId;
  }

  public String prompt() {
    return prompt;
  }

  public String retrievalCorpus() {
    return retrievalCorpus;
  }

  public String retention() {
    return retention;
  }

  public String humanReviewLogic() {
    return humanReviewLogic;
  }

  public void replace(String prompt, String retrievalCorpus, String retention, String humanReviewLogic) {
    this.prompt = prompt;
    this.retrievalCorpus = retrievalCorpus;
    this.retention = retention;
    this.humanReviewLogic = humanReviewLogic;
  }
}
