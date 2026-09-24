package os.assurance.eu.api.corpus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "corpus_relationships")
public class CorpusRelationshipEntity {
  @Id
  private UUID id;

  @Column(nullable = false)
  private UUID corpusVersionId;

  @Column(nullable = false)
  private String relation;

  @Column(nullable = false)
  private String fromKind;

  @Column(nullable = false)
  private String fromKey;

  @Column(nullable = false)
  private String toKind;

  @Column(nullable = false)
  private String toKey;

  protected CorpusRelationshipEntity() {
  }

  public CorpusRelationshipEntity(
      UUID id,
      UUID corpusVersionId,
      String relation,
      String fromKind,
      String fromKey,
      String toKind,
      String toKey) {
    this.id = id;
    this.corpusVersionId = corpusVersionId;
    this.relation = relation;
    this.fromKind = fromKind;
    this.fromKey = fromKey;
    this.toKind = toKind;
    this.toKey = toKey;
  }

  public String relation() {
    return relation;
  }

  public String fromKey() {
    return fromKey;
  }

  public String toKey() {
    return toKey;
  }
}
