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
@Table(name = "reg_sources")
public class RegSourceEntity {
  @Id
  private UUID id;

  @Column(nullable = false, unique = true, length = 64)
  private String code;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, length = 2048)
  private String url;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private FeedType feedType;

  @Column(nullable = false)
  private int pollIntervalSeconds;

  @Column(nullable = false)
  private boolean enabled;

  private Instant lastPolledAt;

  @Column(length = 1024)
  private String notes;

  @Column(nullable = false)
  private Instant createdAt;

  protected RegSourceEntity() {
  }

  public RegSourceEntity(RegSource source) {
    this.id = source.id();
    this.code = source.code();
    this.name = source.name();
    this.url = source.url();
    this.feedType = source.feedType();
    this.pollIntervalSeconds = source.pollIntervalSeconds();
    this.enabled = source.enabled();
    this.lastPolledAt = source.lastPolledAt();
    this.notes = source.notes();
    this.createdAt = source.createdAt();
  }

  public UUID id() {
    return id;
  }

  public String code() {
    return code;
  }

  public String name() {
    return name;
  }

  public String url() {
    return url;
  }

  public FeedType feedType() {
    return feedType;
  }

  public int pollIntervalSeconds() {
    return pollIntervalSeconds;
  }

  public boolean enabled() {
    return enabled;
  }

  public Instant lastPolledAt() {
    return lastPolledAt;
  }

  public void markPolled(Instant at) {
    this.lastPolledAt = at;
  }

  public RegSource toDomain() {
    return new RegSource(
        id, code, name, url, feedType, pollIntervalSeconds, enabled, lastPolledAt, notes, createdAt);
  }
}
