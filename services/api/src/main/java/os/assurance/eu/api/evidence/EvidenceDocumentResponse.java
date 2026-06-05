package os.assurance.eu.api.evidence;

import java.time.Instant;
import java.util.UUID;

public record EvidenceDocumentResponse(
    UUID id,
    UUID systemId,
    String type,
    String title,
    String sourceUri,
    String checksum,
    int chunkCount,
    String ingestionStatus,
    Instant createdAt) {
}
