package os.assurance.eu.api.tenant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tenants")
public class TenantEntity {
  @Id
  private UUID id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String plan;

  @Column(nullable = false)
  private String dataRegion;

  @Column(nullable = false)
  private Instant createdAt;

  protected TenantEntity() {
  }

  public TenantEntity(UUID id, String name, String plan, String dataRegion, Instant createdAt) {
    this.id = id;
    this.name = name;
    this.plan = plan;
    this.dataRegion = dataRegion;
    this.createdAt = createdAt;
  }
}
