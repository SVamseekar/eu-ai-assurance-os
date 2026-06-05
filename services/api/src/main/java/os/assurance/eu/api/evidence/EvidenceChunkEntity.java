package os.assurance.eu.api.evidence;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Map;
import java.util.UUID;
import os.assurance.eu.api.persistence.JsonMapConverter;

@Entity
@Table(name = "evidence_chunks")
public class EvidenceChunkEntity {
  @Id
  private UUID id;

  @Column(nullable = false)
  private UUID documentId;

  @Column(nullable = false)
  private int ordinal;

  @Column
  private String sectionRef;

  @Column(nullable = false)
  private String content;

  @Column(nullable = false)
  private String embedding;

  @Convert(converter = JsonMapConverter.class)
  @Column(name = "metadata_json", nullable = false)
  private Map<String, Object> metadata;

  protected EvidenceChunkEntity() {
  }

  public EvidenceChunkEntity(EvidenceChunk chunk) {
    this.id = chunk.id();
    this.documentId = chunk.documentId();
    this.ordinal = chunk.ordinal();
    this.sectionRef = chunk.sectionRef();
    this.content = chunk.content();
    this.embedding = chunk.embedding();
    this.metadata = chunk.metadata();
  }

  public EvidenceChunk toDomain() {
    return new EvidenceChunk(id, documentId, ordinal, sectionRef, content, embedding, metadata);
  }
}
