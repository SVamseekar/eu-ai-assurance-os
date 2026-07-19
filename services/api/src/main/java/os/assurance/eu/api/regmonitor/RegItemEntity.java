package os.assurance.eu.api.regmonitor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "reg_items")
public class RegItemEntity {
  @Id
  private UUID id;

  @Column(nullable = false)
  private UUID sourceId;

  @Column(nullable = false, length = 512)
  private String externalId;

  @Column(nullable = false, length = 1024)
  private String title;

  @Column(nullable = false, length = 4096)
  private String summary;

  private Instant publishedAt;

  @Column(nullable = false, length = 2048)
  private String url;

  @Column(nullable = false, length = 64, unique = true)
  private String contentHash;

  @Column(nullable = false)
  private Instant fetchedAt;

  protected RegItemEntity() {
  }

  public RegItemEntity(
      UUID id,
      UUID sourceId,
      String externalId,
      String title,
      String summary,
      Instant publishedAt,
      String url,
      String contentHash,
      Instant fetchedAt) {
    this.id = id;
    this.sourceId = sourceId;
    this.externalId = externalId;
    this.title = title;
    this.summary = summary;
    this.publishedAt = publishedAt;
    this.url = url;
    this.contentHash = contentHash;
    this.fetchedAt = fetchedAt;
  }

  public UUID id() {
    return id;
  }

  public UUID sourceId() {
    return sourceId;
  }

  public String externalId() {
    return externalId;
  }

  public String title() {
    return title;
  }

  public String summary() {
    return summary;
  }

  public Instant publishedAt() {
    return publishedAt;
  }

  public String url() {
    return url;
  }

  public String contentHash() {
    return contentHash;
  }

  public Instant fetchedAt() {
    return fetchedAt;
  }

  public RegItem toDomain(List<RegImpactHint> hints) {
    return new RegItem(
        id, sourceId, externalId, title, summary, publishedAt, url, contentHash, fetchedAt, hints);
  }
}
