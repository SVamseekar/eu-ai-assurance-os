package os.assurance.eu.api.evidence;

import java.util.Map;
import java.util.UUID;

public record EvidenceChunk(
    UUID id,
    UUID documentId,
    int ordinal,
    String sectionRef,
    String content,
    String contentSha256,
    String embedding,
    String embeddingProvider,
    Map<String, Object> metadata) {
}
