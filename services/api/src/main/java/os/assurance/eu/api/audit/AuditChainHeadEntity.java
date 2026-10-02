package os.assurance.eu.api.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_chain_heads")
public class AuditChainHeadEntity {
  @Id
  @Column(name = "tenant_id")
  private UUID tenantId;

  @Column(name = "head_hash")
  private String headHash;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected AuditChainHeadEntity() {
  }

  public AuditChainHeadEntity(UUID tenantId, String headHash, Instant updatedAt) {
    this.tenantId = tenantId;
    this.headHash = headHash;
    this.updatedAt = updatedAt;
  }

  public UUID tenantId() { return tenantId; }
  public String headHash() { return headHash; }

  public void advance(String newHead, Instant at) {
    this.headHash = newHead;
    this.updatedAt = at;
  }
}
