package os.assurance.eu.api.regmonitor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reg_impact_hints")
public class RegImpactHintEntity {
  @Id
  private UUID id;

  @Column(nullable = false)
  private UUID regItemId;

  @Column(length = 64)
  private String controlCode;

  @Column(length = 64)
  private String obligationCode;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private ImpactLevel impactLevel;

  @Column(nullable = false, length = 2048)
  private String impactNote;

  @Column(nullable = false)
  private Instant createdAt;

  protected RegImpactHintEntity() {
  }

  public RegImpactHintEntity(RegImpactHint hint) {
    this.id = hint.id();
    this.regItemId = hint.regItemId();
    this.controlCode = hint.controlCode();
    this.obligationCode = hint.obligationCode();
    this.impactLevel = hint.impactLevel();
    this.impactNote = hint.impactNote();
    this.createdAt = hint.createdAt();
  }

  public UUID id() {
    return id;
  }

  public UUID regItemId() {
    return regItemId;
  }

  public RegImpactHint toDomain() {
    return new RegImpactHint(
        id, regItemId, controlCode, obligationCode, impactLevel, impactNote, createdAt);
  }
}
