package os.assurance.eu.api.corpus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "corpus_versions")
public class CorpusVersionEntity {
  @Id
  private UUID id;

  @Column(nullable = false, unique = true)
  private String versionHash;

  @Column(nullable = false)
  private Instant builtAt;

  protected CorpusVersionEntity() {
  }

  public CorpusVersionEntity(UUID id, String versionHash, Instant builtAt) {
    this.id = id;
    this.versionHash = versionHash;
    this.builtAt = builtAt;
  }

  public UUID id() {
    return id;
  }

  public String versionHash() {
    return versionHash;
  }

  public Instant builtAt() {
    return builtAt;
  }
}
