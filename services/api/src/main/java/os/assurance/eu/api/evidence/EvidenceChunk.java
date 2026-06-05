package os.assurance.eu.api.evidence;

import java.util.Map;
import java.util.UUID;

public record EvidenceChunk(
    UUID id,
    UUID documentId,
    int ordinal,
    String sectionRef,
    String content,
    String embedding,
    Map<String, Object> metadata) {
}
