package os.assurance.eu.api.corpus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "provisions")
public class ProvisionEntity {
  @Id
  private UUID id;

  @Column(nullable = false)
  private UUID instrumentId;

  @Column(nullable = false)
  private String provisionKey;

  private String article;
  private String paragraph;
  private String point;
  private String annex;

  @Column(nullable = false, length = 4000)
  private String textExcerpt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ForceStatus forceStatus;

  @Column(nullable = false)
  private LocalDate forceFrom;

  private String scopeNote;

  protected ProvisionEntity() {
  }

  public ProvisionEntity(
      UUID id,
      UUID instrumentId,
      String provisionKey,
      String article,
      String paragraph,
      String point,
      String annex,
      String textExcerpt,
      ForceStatus forceStatus,
      LocalDate forceFrom,
      String scopeNote) {
    this.id = id;
    this.instrumentId = instrumentId;
    this.provisionKey = provisionKey;
    this.article = article;
    this.paragraph = paragraph;
    this.point = point;
    this.annex = annex;
    this.textExcerpt = textExcerpt;
    this.forceStatus = forceStatus;
    this.forceFrom = forceFrom;
    this.scopeNote = scopeNote;
  }

  public String provisionKey() {
    return provisionKey;
  }

  public String article() {
    return article;
  }

  public String paragraph() {
    return paragraph;
  }

  public String point() {
    return point;
  }

  public String annex() {
    return annex;
  }

  public String textExcerpt() {
    return textExcerpt;
  }

  public ForceStatus forceStatus() {
    return forceStatus;
  }

  public LocalDate forceFrom() {
    return forceFrom;
  }

  public String scopeNote() {
    return scopeNote;
  }
}
