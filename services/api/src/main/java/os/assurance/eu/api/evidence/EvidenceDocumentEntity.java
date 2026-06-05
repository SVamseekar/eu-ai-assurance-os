package os.assurance.eu.api.evidence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "evidence_documents")
public class EvidenceDocumentEntity {
  @Id
  private UUID id;

  @Column(nullable = false)
  private UUID tenantId;

  @Column(nullable = false)
  private UUID systemId;

  @Column(nullable = false)
  private String type;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String sourceUri;

  @Column(nullable = false)
  private String checksum;

  @Column(nullable = false)
  private int chunkCount;

  @Column(nullable = false)
  private String ingestionStatus;

  @Column(nullable = false)
  private Instant createdAt;

  protected EvidenceDocumentEntity() {
  }

  public EvidenceDocumentEntity(UUID tenantId, EvidenceDocument document) {
    this.id = document.id();
    this.tenantId = tenantId;
    this.systemId = document.systemId();
    this.type = document.type();
    this.title = document.title();
    this.sourceUri = document.sourceUri();
    this.checksum = document.checksum();
    this.chunkCount = document.chunkCount();
    this.ingestionStatus = document.ingestionStatus();
    this.createdAt = document.createdAt();
  }

  public UUID id() {
    return id;
  }

  public UUID systemId() {
    return systemId;
  }

  public EvidenceDocument toDomain() {
    return new EvidenceDocument(
        id,
        systemId,
        type,
        title,
        sourceUri,
        checksum,
        chunkCount,
        ingestionStatus,
        createdAt);
  }
}
