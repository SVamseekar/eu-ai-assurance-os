package os.assurance.eu.api.eval;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "eval_datasets")
public class EvalDatasetEntity {
  @Id
  private UUID id;

  @Column(nullable = false)
  private UUID tenantId;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String version;

  @Column(nullable = false)
  private int sampleCount;

  @Column(nullable = false)
  private boolean golden;

  @Column(nullable = false)
  private Instant createdAt;

  protected EvalDatasetEntity() {
  }

  public EvalDatasetEntity(UUID tenantId, EvalDataset dataset) {
    this.id = dataset.id();
    this.tenantId = tenantId;
    this.name = dataset.name();
    this.version = dataset.version();
    this.sampleCount = dataset.sampleCount();
    this.golden = dataset.golden();
    this.createdAt = dataset.createdAt();
  }

  public UUID tenantId() {
    return tenantId;
  }

  public EvalDataset toDomain() {
    return new EvalDataset(id, name, version, sampleCount, golden, createdAt);
  }
}
