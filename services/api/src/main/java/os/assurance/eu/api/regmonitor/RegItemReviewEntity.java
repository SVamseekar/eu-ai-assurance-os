package os.assurance.eu.api.regmonitor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reg_item_reviews")
public class RegItemReviewEntity {
  @Id
  private UUID id;

  @Column(nullable = false)
  private UUID tenantId;

  @Column(nullable = false)
  private UUID regItemId;

  private UUID reviewedBy;

  @Column(nullable = false)
  private Instant reviewedAt;

  @Column(length = 2048)
  private String notes;

  protected RegItemReviewEntity() {
  }

  public RegItemReviewEntity(
      UUID id,
      UUID tenantId,
      UUID regItemId,
      UUID reviewedBy,
      Instant reviewedAt,
      String notes) {
    this.id = id;
    this.tenantId = tenantId;
    this.regItemId = regItemId;
    this.reviewedBy = reviewedBy;
    this.reviewedAt = reviewedAt;
    this.notes = notes;
  }

  public UUID id() {
    return id;
  }

  public UUID tenantId() {
    return tenantId;
  }

  public UUID regItemId() {
    return regItemId;
  }

  public void updateReview(UUID reviewedBy, Instant reviewedAt, String notes) {
    this.reviewedBy = reviewedBy;
    this.reviewedAt = reviewedAt;
    this.notes = notes;
  }

  public RegItemReview toDomain() {
    return new RegItemReview(id, regItemId, reviewedBy, reviewedAt, notes);
  }
}
