package os.assurance.eu.api.proposal;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import os.assurance.eu.api.audit.AuditService;
import os.assurance.eu.api.corpus.CorpusQueryService;
import os.assurance.eu.api.system.AiSystemRepository;
import os.assurance.eu.api.tenant.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MappingProposalService {
  private final MappingProposalJpaRepository proposals;
  private final AiSystemRepository systems;
  private final CorpusQueryService corpus;
  private final AuditService auditService;
  private final TenantContext tenantContext;
  private final RulesDocumentMapper rulesMapper;

  public MappingProposalService(
      MappingProposalJpaRepository proposals,
      AiSystemRepository systems,
      CorpusQueryService corpus,
      AuditService auditService,
      TenantContext tenantContext) {
    this.proposals = proposals;
    this.systems = systems;
    this.corpus = corpus;
    this.auditService = auditService;
    this.tenantContext = tenantContext;
    this.rulesMapper = new RulesDocumentMapper();
  }

  @Transactional(readOnly = true)
  public ProposalListResponse list(UUID systemId) {
    requireSystem(systemId);
    String current = corpus.currentVersionHash();
    List<ProposalView> items = proposals
        .findAllByTenantIdAndSystemIdOrderByCreatedAtAsc(tenantContext.tenantId(), systemId)
        .stream()
        .map(row -> toView(row, current))
        .toList();
    return new ProposalListResponse(current, items);
  }

  @Transactional
  public ProposalView create(UUID systemId, CreateMappingProposalRequest request) {
    requireSystem(systemId);
    String current = corpus.currentVersionHash();
    String pinned = request.corpusVersion() == null || request.corpusVersion().isBlank()
        ? current
        : request.corpusVersion();
    MappingProposalEntity saved = proposals.save(new MappingProposalEntity(
        UUID.randomUUID(),
        tenantContext.tenantId(),
        systemId,
        "PENDING",
        request.relation(),
        pinned,
        blankToNull(request.adapterVersion()),
        blankToNull(request.provisionKey()),
        blankToNull(request.excerpt()),
        Instant.now()));
    return toView(saved, current);
  }

  @Transactional
  public List<ProposalView> mapDocuments(UUID systemId, List<MapDocumentRequest> documents) {
    requireSystem(systemId);
    String current = corpus.currentVersionHash();
    List<LawProvision> lawIndex = lawIndex();
    List<ProposalView> created = new ArrayList<>();
    for (MapDocumentRequest document : documents) {
      MappingDraft draft = rulesMapper.map(document.title(), document.text(), lawIndex);
      MappingProposalEntity saved = proposals.save(new MappingProposalEntity(
          UUID.randomUUID(),
          tenantContext.tenantId(),
          systemId,
          "PENDING",
          draft.relation(),
          current,
          null,
          draft.provisionKey(),
          draft.excerpt(),
          Instant.now()));
      created.add(toView(saved, current));
    }
    return created;
  }

  @Transactional
  public ProposalView accept(UUID systemId, UUID proposalId) {
    return decide(systemId, proposalId, "ACCEPTED");
  }

  @Transactional
  public ProposalView reject(UUID systemId, UUID proposalId) {
    MappingProposalEntity proposal = pending(systemId, proposalId);
    return finish(proposal, "REJECTED", "mapping_proposal.rejected");
  }

  private ProposalView decide(UUID systemId, UUID proposalId, String status) {
    MappingProposalEntity proposal = pending(systemId, proposalId);
    String current = corpus.currentVersionHash();
    if ("abstain".equals(proposal.relation())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Abstain has no link to accept");
    }
    if (!current.equals(proposal.corpusVersion())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Corpus version does not match the pinned corpus");
    }
    if (proposal.provisionKey() != null && !corpus.currentCorpusContains(proposal.provisionKey())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Provision is not in the pinned corpus");
    }
    finish(proposal, status, "mapping_proposal.accepted");
    String force = forceStatus(proposal.provisionKey());
    proposal.assignMode("FUTURE".equals(force) ? "INFORMATIONAL" : "WARNING");
    proposals.save(proposal);
    return toView(proposal, corpus.currentVersionHash());
  }

  private ProposalView finish(MappingProposalEntity proposal, String status, String eventType) {
    proposal.decide(status, tenantContext.actorId(), Instant.now());
    proposals.save(proposal);
    String current = corpus.currentVersionHash();
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("corpusVersion", proposal.corpusVersion());
    payload.put("currentCorpusVersion", current);
    payload.put("relation", proposal.relation());
    payload.put("provisionKey", proposal.provisionKey());
    payload.put("decision", status);
    auditService.append(
        proposal.systemId(),
        eventType,
        "mapping_proposal",
        proposal.id().toString(),
        payload);
    return toView(proposal, current);
  }

  @Transactional
  public ProposalView setMode(UUID systemId, UUID proposalId, String mode) {
    requireSystem(systemId);
    MappingProposalEntity proposal = proposals
        .findByIdAndTenantIdAndSystemId(proposalId, tenantContext.tenantId(), systemId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proposal not found"));
    if (!"ACCEPTED".equals(proposal.status())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Only an accepted link has a control mode");
    }
    String normalized = mode == null ? "" : mode.trim().toUpperCase();
    if (!List.of("INFORMATIONAL", "WARNING", "APPROVAL_REQUIRED", "BLOCKING").contains(normalized)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown control mode");
    }
    String force = forceStatus(proposal.provisionKey());
    if ("FUTURE".equals(force) && "BLOCKING".equals(normalized)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "A future duty cannot be blocking");
    }
    if ("FUTURE".equals(force)) {
      normalized = "INFORMATIONAL";
    }
    proposal.assignMode(normalized);
    proposals.save(proposal);
    return toView(proposal, corpus.currentVersionHash());
  }

  @Transactional
  public void reopenAcceptedLinks(UUID systemId) {
    Instant now = Instant.now();
    for (MappingProposalEntity row : proposals.findAllByTenantIdAndSystemIdOrderByCreatedAtAsc(
        tenantContext.tenantId(), systemId)) {
      if ("ACCEPTED".equals(row.status()) && !"abstain".equals(row.relation())) {
        row.reopen(now);
        proposals.save(row);
      }
    }
  }

  private MappingProposalEntity pending(UUID systemId, UUID proposalId) {
    requireSystem(systemId);
    MappingProposalEntity proposal = proposals
        .findByIdAndTenantIdAndSystemId(proposalId, tenantContext.tenantId(), systemId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proposal not found"));
    if (!"PENDING".equals(proposal.status())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Proposal is already decided");
    }
    return proposal;
  }

  private void requireSystem(UUID systemId) {
    if (systems.findById(systemId).isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "AI system not found");
    }
  }

  private ProposalView toView(MappingProposalEntity row, String current) {
    return new ProposalView(
        row.id(),
        row.systemId(),
        row.status(),
        row.relation(),
        displayState(row, current),
        row.corpusVersion(),
        row.corpusVersion(),
        current,
        row.provisionKey(),
        row.excerpt(),
        row.adapterVersion(),
        forceStatus(row.provisionKey()),
        forceFrom(row.provisionKey()));
  }

  private List<LawProvision> lawIndex() {
    return corpus.current().provisions().stream()
        .map(row -> new LawProvision(row.provisionKey(), row.textExcerpt(), row.forceStatus(), row.forceFrom()))
        .toList();
  }

  private String forceStatus(String provisionKey) {
    CorpusQueryService.ProvisionView match = provision(provisionKey);
    return match == null ? null : match.forceStatus();
  }

  private LocalDate forceFrom(String provisionKey) {
    CorpusQueryService.ProvisionView match = provision(provisionKey);
    return match == null ? null : match.forceFrom();
  }

  private CorpusQueryService.ProvisionView provision(String provisionKey) {
    if (provisionKey == null || provisionKey.isBlank()) {
      return null;
    }
    for (CorpusQueryService.ProvisionView row : corpus.current().provisions()) {
      if (provisionKey.equals(row.provisionKey())) {
        return row;
      }
    }
    return null;
  }

  static String displayState(MappingProposalEntity row, String current) {
    if ("abstain".equals(row.relation())) {
      return "abstain";
    }
    if (current == null || !current.equals(row.corpusVersion())) {
      return "corpus_mismatch";
    }
    if ("PENDING".equals(row.status())) {
      return "pending";
    }
    return row.status().toLowerCase();
  }

  private static String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value;
  }

  public record ProposalListResponse(String currentCorpusVersion, List<ProposalView> items) {
  }

  public record ProposalView(
      UUID id,
      UUID systemId,
      String status,
      String relation,
      String displayState,
      String corpusVersion,
      String pinnedCorpusVersion,
      String currentCorpusVersion,
      String provisionKey,
      String excerpt,
      String adapterVersion,
      String forceStatus,
      LocalDate forceFrom) {
  }

  public record MapDocumentRequest(String title, String text) {
  }
}
