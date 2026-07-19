package os.assurance.eu.api.regmonitor;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import os.assurance.eu.api.audit.AuditService;
import os.assurance.eu.api.control.ControlService;
import os.assurance.eu.api.control.SystemControl;
import os.assurance.eu.api.system.AiSystem;
import os.assurance.eu.api.system.AiSystemRepository;
import os.assurance.eu.api.tenant.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Query + review APIs for the regulatory change feed.
 *
 * <p><strong>Never</strong> auto-changes risk class or control status.
 */
@Service
public class RegMonitorService {
  private final RegItemJpaRepository items;
  private final RegImpactHintJpaRepository hints;
  private final RegItemReviewJpaRepository reviews;
  private final RegSourceJpaRepository sources;
  private final AiSystemRepository systems;
  private final ControlService controlService;
  private final TenantContext tenantContext;
  private final AuditService auditService;
  private final Clock clock;

  public RegMonitorService(
      RegItemJpaRepository items,
      RegImpactHintJpaRepository hints,
      RegItemReviewJpaRepository reviews,
      RegSourceJpaRepository sources,
      AiSystemRepository systems,
      ControlService controlService,
      TenantContext tenantContext,
      AuditService auditService,
      Clock clock) {
    this.items = items;
    this.hints = hints;
    this.reviews = reviews;
    this.sources = sources;
    this.systems = systems;
    this.controlService = controlService;
    this.tenantContext = tenantContext;
    this.auditService = auditService;
    this.clock = clock;
  }

  @Transactional(readOnly = true)
  public RegMonitorFeedResponse listItems(Instant since, Boolean reviewedOnly) {
    List<RegItemEntity> entities = since == null
        ? items.findAllByOrderByFetchedAtDesc()
        : items.findAllByFetchedAtGreaterThanEqualOrderByFetchedAtDesc(since);
    List<RegItemResponse> responses = toResponses(entities);
    if (reviewedOnly != null) {
      responses = responses.stream()
          .filter(r -> r.reviewed() == reviewedOnly)
          .toList();
    }
    return new RegMonitorFeedResponse(
        RegMonitorDisclaimers.PRODUCT_LABEL,
        RegMonitorDisclaimers.FULL,
        RegMonitorDisclaimers.LATENCY_NOTE,
        responses);
  }

  @Transactional(readOnly = true)
  public RegMonitorFeedResponse listRelevantForSystem(UUID systemId) {
    AiSystem system = systems.findById(systemId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "AI system not found"));

    Set<String> systemControlCodes = controlService.listForSystem(systemId).stream()
        .map(SystemControl::controlCode)
        .filter(Objects::nonNull)
        .map(c -> c.toUpperCase(Locale.ROOT))
        .collect(Collectors.toCollection(HashSet::new));

    String sectorBlob = blob(system.sector(), system.purpose(), system.decisionImpact(), system.owner());

    List<RegItemEntity> all = items.findAllByOrderByFetchedAtDesc();
    Map<UUID, List<RegImpactHint>> hintsByItem = loadHints(
        all.stream().map(RegItemEntity::id).toList());

    List<RegItemEntity> relevant = new ArrayList<>();
    Map<UUID, String> reasons = new HashMap<>();
    for (RegItemEntity item : all) {
      List<RegImpactHint> itemHints = hintsByItem.getOrDefault(item.id(), List.of());
      String reason = relevanceReason(item, itemHints, systemControlCodes, sectorBlob);
      if (reason != null) {
        relevant.add(item);
        reasons.put(item.id(), reason);
      }
    }

    List<RegItemResponse> responses = toResponses(relevant);
    responses = responses.stream()
        .map(r -> withReason(r, reasons.get(r.id())))
        .toList();

    return new RegMonitorFeedResponse(
        RegMonitorDisclaimers.PRODUCT_LABEL,
        RegMonitorDisclaimers.FULL,
        RegMonitorDisclaimers.LATENCY_NOTE,
        responses);
  }

  @Transactional
  public RegItemResponse markReviewed(UUID itemId, String notes) {
    RegItemEntity item = items.findById(itemId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reg item not found"));

    UUID tenantId = tenantContext.tenantId();
    Instant now = clock.instant();
    RegItemReviewEntity review = reviews.findByTenantIdAndRegItemId(tenantId, itemId)
        .orElseGet(() -> new RegItemReviewEntity(
            UUID.randomUUID(),
            tenantId,
            itemId,
            tenantContext.actorId(),
            now,
            notes));
    review.updateReview(tenantContext.actorId(), now, notes);
    reviews.save(review);

    // Explicit: review does NOT mutate risk class or control status.
    auditService.append(
        null,
        "reg_item.reviewed",
        "reg_item",
        itemId.toString(),
        Map.of(
            "notes", notes == null ? "" : notes,
            "autoMutatesRiskOrControls", false,
            "title", item.title() == null ? "" : item.title()));

    return toResponses(List.of(item)).get(0);
  }

  private String relevanceReason(
      RegItemEntity item,
      List<RegImpactHint> itemHints,
      Set<String> systemControlCodes,
      String sectorBlob) {
    for (RegImpactHint hint : itemHints) {
      if (hint.controlCode() != null
          && systemControlCodes.contains(hint.controlCode().toUpperCase(Locale.ROOT))) {
        return "Matches system control " + hint.controlCode() + " (hint UNCERTAIN)";
      }
    }
    String text = ((item.title() == null ? "" : item.title()) + " "
        + (item.summary() == null ? "" : item.summary())).toLowerCase(Locale.ROOT);

    if (sectorBlob.contains("insurance") || sectorBlob.contains("claim") || sectorBlob.contains("essential")) {
      if (text.contains("insurance") || text.contains("essential") || text.contains("eligibility")
          || text.contains("claims") || text.contains("high-risk") || text.contains("high risk")) {
        return "Sector/purpose keyword match (insurance/essential services)";
      }
      for (RegImpactHint hint : itemHints) {
        if ("ESSENTIAL_SERVICE_ACCESS".equals(hint.obligationCode())) {
          return "Obligation hint ESSENTIAL_SERVICE_ACCESS for insurance/essential sector";
        }
      }
    }
    if (sectorBlob.contains("hr") || sectorBlob.contains("employment") || sectorBlob.contains("recruit")
        || sectorBlob.contains("worker") || sectorBlob.contains("candidate")) {
      if (text.contains("employment") || text.contains("worker") || text.contains("recruit")
          || text.contains("hr") || text.contains("candidate")) {
        return "Sector/purpose keyword match (employment/HR)";
      }
      for (RegImpactHint hint : itemHints) {
        if ("EMPLOYMENT_HR".equals(hint.obligationCode())) {
          return "Obligation hint EMPLOYMENT_HR for employment sector";
        }
      }
    }
    if (sectorBlob.contains("finance") || sectorBlob.contains("credit") || sectorBlob.contains("bank")) {
      if (text.contains("credit") || text.contains("finance") || text.contains("essential")
          || text.contains("high-risk")) {
        return "Sector/purpose keyword match (finance)";
      }
    }
    // High-risk systems: surface high-risk / oversight / annex III themed items.
    for (RegImpactHint hint : itemHints) {
      if ("HIGH_RISK_BUNDLE_SELF_ASSESSED".equals(hint.obligationCode())
          || "HUMAN_OVERSIGHT_HIGH_IMPACT".equals(hint.obligationCode())) {
        if (!systemControlCodes.isEmpty()) {
          return "High-impact obligation hint " + hint.obligationCode() + " (UNCERTAIN)";
        }
      }
    }
    return null;
  }

  private List<RegItemResponse> toResponses(List<RegItemEntity> entities) {
    if (entities.isEmpty()) {
      return List.of();
    }
    List<UUID> ids = entities.stream().map(RegItemEntity::id).toList();
    Map<UUID, List<RegImpactHint>> hintsByItem = loadHints(ids);
    Map<UUID, RegItemReviewEntity> reviewByItem = reviews
        .findAllByTenantIdAndRegItemIdIn(tenantContext.tenantId(), ids)
        .stream()
        .collect(Collectors.toMap(RegItemReviewEntity::regItemId, r -> r, (a, b) -> a));
    Map<UUID, String> sourceCodes = sources.findAll().stream()
        .collect(Collectors.toMap(RegSourceEntity::id, RegSourceEntity::code, (a, b) -> a));

    List<RegItemResponse> out = new ArrayList<>();
    for (RegItemEntity e : entities) {
      RegItemReviewEntity review = reviewByItem.get(e.id());
      out.add(new RegItemResponse(
          e.id(),
          e.sourceId(),
          sourceCodes.getOrDefault(e.sourceId(), "UNKNOWN"),
          e.externalId(),
          e.title(),
          e.summary(),
          e.publishedAt(),
          e.url(),
          e.contentHash(),
          e.fetchedAt(),
          hintsByItem.getOrDefault(e.id(), List.of()),
          review != null,
          review == null ? null : review.toDomain().reviewedAt(),
          review == null ? null : review.toDomain().notes(),
          null,
          RegMonitorDisclaimers.PRODUCT_LABEL,
          RegMonitorDisclaimers.SHORT));
    }
    return out;
  }

  private Map<UUID, List<RegImpactHint>> loadHints(List<UUID> itemIds) {
    if (itemIds.isEmpty()) {
      return Map.of();
    }
    return hints.findAllByRegItemIdIn(itemIds).stream()
        .map(RegImpactHintEntity::toDomain)
        .collect(Collectors.groupingBy(RegImpactHint::regItemId));
  }

  private static RegItemResponse withReason(RegItemResponse r, String reason) {
    return new RegItemResponse(
        r.id(), r.sourceId(), r.sourceCode(), r.externalId(), r.title(), r.summary(),
        r.publishedAt(), r.url(), r.contentHash(), r.fetchedAt(), r.impactHints(),
        r.reviewed(), r.reviewedAt(), r.reviewNotes(), reason, r.productLabel(), r.disclaimer());
  }

  private static String blob(String... parts) {
    StringBuilder sb = new StringBuilder();
    for (String p : parts) {
      if (p != null) {
        sb.append(' ').append(p.toLowerCase(Locale.ROOT));
      }
    }
    return sb.toString();
  }
}
