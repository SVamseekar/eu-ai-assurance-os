package os.assurance.eu.api.corpus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "instruments")
public class InstrumentEntity {
  @Id
  private UUID id;

  @Column(nullable = false)
  private UUID corpusVersionId;

  @Column(nullable = false)
  private String seedCelex;

  private String consolidationCelex;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String language;

  @Column(nullable = false)
  private String textHash;

  private LocalDate consolidationDate;
  private LocalDate applicationFrom;

  protected InstrumentEntity() {
  }

  public InstrumentEntity(
      UUID id,
      UUID corpusVersionId,
      String seedCelex,
      String consolidationCelex,
      String title,
      String language,
      String textHash,
      LocalDate consolidationDate,
      LocalDate applicationFrom) {
    this.id = id;
    this.corpusVersionId = corpusVersionId;
    this.seedCelex = seedCelex;
    this.consolidationCelex = consolidationCelex;
    this.title = title;
    this.language = language;
    this.textHash = textHash;
    this.consolidationDate = consolidationDate;
    this.applicationFrom = applicationFrom;
  }

  public UUID id() {
    return id;
  }

  public UUID corpusVersionId() {
    return corpusVersionId;
  }

  public String seedCelex() {
    return seedCelex;
  }

  public String consolidationCelex() {
    return consolidationCelex;
  }

  public String title() {
    return title;
  }

  public String textHash() {
    return textHash;
  }

  public LocalDate consolidationDate() {
    return consolidationDate;
  }

  public LocalDate applicationFrom() {
    return applicationFrom;
  }
}
