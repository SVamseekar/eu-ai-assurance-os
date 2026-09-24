package os.assurance.eu.api.corpus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "guidance_docs")
public class GuidanceDocEntity {
  @Id
  private UUID id;

  @Column(nullable = false)
  private UUID corpusVersionId;

  @Column(nullable = false)
  private String sourceKey;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String authorityRank;

  @Column(nullable = false)
  private String textHash;

  @Column(nullable = false, length = 4000)
  private String body;

  protected GuidanceDocEntity() {
  }

  public GuidanceDocEntity(
      UUID id,
      UUID corpusVersionId,
      String sourceKey,
      String title,
      String authorityRank,
      String textHash,
      String body) {
    this.id = id;
    this.corpusVersionId = corpusVersionId;
    this.sourceKey = sourceKey;
    this.title = title;
    this.authorityRank = authorityRank;
    this.textHash = textHash;
    this.body = body;
  }

  public String sourceKey() {
    return sourceKey;
  }

  public String title() {
    return title;
  }

  public String authorityRank() {
    return authorityRank;
  }

  public String body() {
    return body;
  }
}
