package os.assurance.eu.api.evidence;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.UUID;
import os.assurance.eu.api.audit.AuditService;
import os.assurance.eu.api.evidence.EvidenceChunker.ChunkDraft;
import os.assurance.eu.api.evidence.PromptInjectionGuard.SanitizedText;
import os.assurance.eu.api.system.AiSystem;
import os.assurance.eu.api.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EvidenceService {
  private static final double MIN_RETRIEVAL_SCORE = 0.20;

  private final EvidenceRepository repository;
  private final EvidenceQueryJpaRepository queries;
  private final TextExtractionService textExtractionService;
  private final PromptInjectionGuard promptInjectionGuard;
  private final EvidenceChunker chunker;
  private final EvidenceEmbeddingService embeddingService;
  private final AuditService auditService;
  private final TenantContext tenantContext;

  public EvidenceService(
      EvidenceRepository repository,
      EvidenceQueryJpaRepository queries,
      TextExtractionService textExtractionService,
      PromptInjectionGuard promptInjectionGuard,
      EvidenceChunker chunker,
      EvidenceEmbeddingService embeddingService,
      AuditService auditService,
      TenantContext tenantContext) {
    this.repository = repository;
    this.queries = queries;
    this.textExtractionService = textExtractionService;
    this.promptInjectionGuard = promptInjectionGuard;
    this.chunker = chunker;
    this.embeddingService = embeddingService;
    this.auditService = auditService;
    this.tenantContext = tenantContext;
  }

  @Transactional
  public EvidenceDocumentResponse ingest(CreateEvidenceDocumentRequest request) {
    String extracted = textExtractionService.extract(request);
    SanitizedText sanitized = promptInjectionGuard.sanitizeDocumentText(extracted);
    List<ChunkDraft> chunkDrafts = chunker.chunk(sanitized.text());
    Instant now = Instant.now();
    UUID documentId = UUID.randomUUID();
    String checksum = request.checksum() == null || request.checksum().isBlank()
        ? checksum(sanitized.text())
        : request.checksum();
    EvidenceDocument saved = repository.saveDocument(new EvidenceDocument(
        documentId,
        request.systemId(),
        request.type(),
        request.title(),
        request.sourceUri(),
        checksum,
        chunkDrafts.size(),
        sanitized.removedLines().isEmpty() ? "indexed" : "indexed_with_warnings",
        now));
    repository.saveChunks(toChunks(saved, chunkDrafts, request.metadata(), sanitized.removedLines()));
    auditService.append(
        saved.systemId(),
        "evidence.document_indexed",
        "evidence_document",
        saved.id().toString(),
        Map.of(
            "title", saved.title(),
            "type", saved.type(),
            "chunkCount", saved.chunkCount(),
            "removedPromptInjectionLines", sanitized.removedLines().size()));
    return saved.toResponse();
  }

  @Transactional(readOnly = true)
  public List<EvidenceDocumentResponse> listDocuments(UUID systemId) {
    return repository.findDocumentsBySystemId(systemId).stream()
        .map(EvidenceDocument::toResponse)
        .toList();
  }

  @Transactional
  public EvidenceQueryResponse answer(AiSystem system, String question) {
    String sanitizedQuestion = promptInjectionGuard.sanitizeQuestion(question);
    String questionEmbedding = embeddingService.embed(sanitizedQuestion);
    List<EvidenceDocument> documents = repository.findDocumentsBySystemId(system.id());
    Map<UUID, EvidenceDocument> documentsById = documents.stream()
        .collect(Collectors.toMap(EvidenceDocument::id, Function.identity()));
    List<ScoredChunk> scoredChunks = repository.findChunksForDocuments(documents.stream().map(EvidenceDocument::id).toList())
        .stream()
        .map(chunk -> new ScoredChunk(
            chunk,
            documentsById.get(chunk.documentId()),
            embeddingService.similarity(questionEmbedding, chunk.embedding())))
        .filter(scoredChunk -> scoredChunk.document() != null)
        .filter(scoredChunk -> scoredChunk.score() >= MIN_RETRIEVAL_SCORE)
        .sorted(Comparator.comparingDouble(ScoredChunk::score).reversed())
        .limit(3)
        .toList();

    EvidenceQueryResponse response = scoredChunks.isEmpty()
        ? fallbackAnswer(system)
        : citedAnswer(system, sanitizedQuestion, scoredChunks);
    queries.save(new EvidenceQueryEntity(
        UUID.randomUUID(),
        tenantContext.tenantId(),
        system.id(),
        sanitizedQuestion,
        response.answer(),
        response.confidence(),
        response.citations(),
        tenantContext.actorId(),
        Instant.now()));
    auditService.append(
        system.id(),
        "evidence.query_answered",
        "ai_system",
        system.id().toString(),
        Map.of(
            "question", sanitizedQuestion,
            "confidence", response.confidence(),
            "citationCount", response.citations().size()));
    return response;
  }

  private List<EvidenceChunk> toChunks(
      EvidenceDocument document,
      List<ChunkDraft> chunkDrafts,
      Map<String, Object> requestMetadata,
      List<String> removedLines) {
    Map<String, Object> metadata = requestMetadata == null ? Map.of() : requestMetadata;
    return java.util.stream.IntStream.range(0, chunkDrafts.size())
        .mapToObj(index -> {
          ChunkDraft draft = chunkDrafts.get(index);
          return new EvidenceChunk(
              UUID.randomUUID(),
              document.id(),
              index,
              draft.sectionRef(),
              draft.content(),
              embeddingService.embed(draft.content()),
              metadataWithGuard(metadata, removedLines));
        })
        .toList();
  }

  private Map<String, Object> metadataWithGuard(Map<String, Object> metadata, List<String> removedLines) {
    java.util.LinkedHashMap<String, Object> guarded = new java.util.LinkedHashMap<>(metadata);
    if (removedLines.isEmpty()) {
      return guarded;
    }
    guarded.put("promptInjectionLinesRemoved", removedLines.size());
    return guarded;
  }

  private EvidenceQueryResponse citedAnswer(AiSystem system, String question, List<ScoredChunk> scoredChunks) {
    List<Citation> citations = scoredChunks.stream()
        .map(scored -> new Citation(
            scored.document().id().toString(),
            scored.document().title(),
            scored.chunk().sectionRef(),
            snippet(scored.chunk().content())))
        .toList();
    double confidence = Math.max(0.35, Math.min(0.95, scoredChunks.stream()
        .mapToDouble(ScoredChunk::score)
        .average()
        .orElse(0.35)));
    String strongest = citations.get(0).snippet();
    String answer = """
        Based on indexed evidence for %s, the strongest cited material says: %s

        For the question "%s", review the cited sections before changing the %s release decision.
        """
        .formatted(system.name(), strongest, question, system.releaseDecision())
        .strip();
    return new EvidenceQueryResponse(answer, confidence, citations);
  }

  private EvidenceQueryResponse fallbackAnswer(AiSystem system) {
    return new EvidenceQueryResponse(
        "%s currently has a %s release decision. Open gaps: %s."
            .formatted(system.name(), system.releaseDecision(), String.join("; ", system.openGaps())),
        system.evidenceCoverage() / 100.0,
        List.of(
            new Citation(
                "doc_dpia_mvp",
                "%s DPIA".formatted(system.name()),
                "Release controls",
                "Reviewer action is required before unresolved high-risk blockers are released."),
            new Citation(
                "doc_control_map_mvp",
                "EU AI Act control map",
                "High-risk systems",
                "Risk management, data governance, logging, transparency, human oversight, accuracy, and cybersecurity controls must be evidenced.")));
  }

  private String snippet(String content) {
    String normalized = content.replaceAll("\\s+", " ").strip();
    return normalized.length() <= 220 ? normalized : normalized.substring(0, 217) + "...";
  }

  private String checksum(String text) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(text.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) {
      throw new IllegalStateException("Could not calculate evidence checksum", e);
    }
  }

  private record ScoredChunk(EvidenceChunk chunk, EvidenceDocument document, double score) {
  }
}
