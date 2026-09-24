package os.assurance.eu.api.system;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import os.assurance.eu.api.conformity.ConformityService;
import os.assurance.eu.api.control.ControlEntity;
import os.assurance.eu.api.control.ControlJpaRepository;
import os.assurance.eu.api.control.ControlStatus;
import os.assurance.eu.api.control.SystemControlEntity;
import os.assurance.eu.api.control.SystemControlJpaRepository;
import os.assurance.eu.api.determination.Applicability;
import os.assurance.eu.api.determination.DeterminationObligation;
import os.assurance.eu.api.determination.DeterminationObligationEntity;
import os.assurance.eu.api.determination.DeterminationObligationJpaRepository;
import os.assurance.eu.api.determination.DeterminationRunEntity;
import os.assurance.eu.api.corpus.CorpusForceLookup;
import os.assurance.eu.api.determination.DeterminationRunJpaRepository;
import os.assurance.eu.api.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReleaseGateService {
  private static final int EVIDENCE_PASS_THRESHOLD = 82;
  private static final int EVAL_PASS_THRESHOLD = 85;
  private static final int EVAL_HARD_BLOCK_THRESHOLD = 78;

  private final SystemControlJpaRepository systemControls;
  private final ControlJpaRepository controls;
  private final TenantContext tenantContext;
  private final DeterminationRunJpaRepository determinationRuns;
  private final DeterminationObligationJpaRepository determinationObligations;
  private final ConformityService conformityService;
  private final CorpusForceLookup corpusForceLookup;
  private final GateControlSource gateControls;

  @Autowired
  public ReleaseGateService(
      SystemControlJpaRepository systemControls,
      ControlJpaRepository controls,
      TenantContext tenantContext,
      DeterminationRunJpaRepository determinationRuns,
      DeterminationObligationJpaRepository determinationObligations,
      ConformityService conformityService,
      CorpusForceLookup corpusForceLookup,
      GateControlSource gateControls) {
    this.systemControls = systemControls;
    this.controls = controls;
    this.tenantContext = tenantContext;
    this.determinationRuns = determinationRuns;
    this.determinationObligations = determinationObligations;
    this.conformityService = conformityService;
    this.corpusForceLookup = corpusForceLookup;
    this.gateControls = gateControls;
  }

  public ReleaseGateService(
      SystemControlJpaRepository systemControls,
      ControlJpaRepository controls,
      TenantContext tenantContext,
      DeterminationRunJpaRepository determinationRuns,
      DeterminationObligationJpaRepository determinationObligations,
      ConformityService conformityService,
      CorpusForceLookup corpusForceLookup) {
    this(
        systemControls,
        controls,
        tenantContext,
        determinationRuns,
        determinationObligations,
        conformityService,
        corpusForceLookup,
        null);
  }

  /** Test/local constructor without control lookup. */
  public ReleaseGateService() {
    this.systemControls = null;
    this.controls = null;
    this.tenantContext = null;
    this.determinationRuns = null;
    this.determinationObligations = null;
    this.conformityService = null;
    this.corpusForceLookup = null;
    this.gateControls = null;
  }

  public ReleaseGateResponse calculate(AiSystem system) {
    List<String> extra = new ArrayList<>();
    extra.addAll(loadControlBlockers(system.id()));
    extra.addAll(loadDeterminationHardBlockers(system.id()));
    return calculate(system, extra);
  }

  public ReleaseGateResponse calculate(AiSystem system, List<String> controlBlockers) {
    List<String> blockers = new ArrayList<>();

    if (system.riskClass() == RiskClass.PROHIBITED) {
      blockers.add("Risk class is prohibited for release");
    }
    if (system.riskClass() == RiskClass.HIGH && hasOversightGap(system.openGaps())) {
      blockers.add("High-risk system is missing required human oversight evidence");
    }
    if (system.evalScore() < EVAL_HARD_BLOCK_THRESHOLD) {
      blockers.add("Eval score is below hard release threshold");
    }
    if (system.dataContractStatus() == DataContractStatus.BREACH) {
      blockers.add("Data contract breach is open");
    }
    if (controlBlockers != null) {
      for (String controlBlocker : controlBlockers) {
        if (controlBlocker != null && !controlBlocker.isBlank() && !blockers.contains(controlBlocker)) {
          blockers.add(controlBlocker);
        }
      }
    }

    if (!blockers.isEmpty()) {
      return finish(system, ReleaseDecision.BLOCKED, blockers);
    }

    boolean needsReview = system.evidenceCoverage() < EVIDENCE_PASS_THRESHOLD
        || system.evalScore() < EVAL_PASS_THRESHOLD
        || system.dataContractStatus() == DataContractStatus.WARNING
        || !system.openGaps().isEmpty();
    if (!needsReview && system.riskClass() == RiskClass.HIGH) {
      needsReview = !loadConformityReviewFlags(system.id(), system.riskClass()).isEmpty();
    }

    return finish(
        system,
        needsReview ? ReleaseDecision.REVIEW : ReleaseDecision.PASS,
        List.of());
  }

  private ReleaseGateResponse finish(AiSystem system, ReleaseDecision decision, List<String> blockers) {
    List<GateControl> controls = gateControls == null || system.id() == null
        ? List.of()
        : gateControls.acceptedLinks(system.id());
    ReleaseDecision adjusted = ControlModePolicy.apply(decision, controls);
    return new ReleaseGateResponse(
        system.id(),
        adjusted,
        ControlModePolicy.blockers(adjusted, blockers, controls),
        controls);
  }

  private List<String> loadControlBlockers(UUID systemId) {
    if (systemControls == null || controls == null || tenantContext == null || systemId == null) {
      return List.of();
    }
    Map<UUID, String> codes = controls.findAll().stream()
        .map(ControlEntity::toDomain)
        .collect(Collectors.toMap(c -> c.id(), c -> c.code(), (a, b) -> a));
    return systemControls
        .findAllByTenantIdAndSystemIdAndStatus(
            tenantContext.tenantId(), systemId, ControlStatus.BLOCKED)
        .stream()
        .map(SystemControlEntity::controlId)
        .map(id -> "CONTROL:" + codes.getOrDefault(id, "UNKNOWN"))
        .distinct()
        .toList();
  }

  private List<String> loadDeterminationHardBlockers(UUID systemId) {
    if (determinationRuns == null || determinationObligations == null || tenantContext == null
        || systemId == null || systemControls == null || controls == null) {
      return List.of();
    }
    DeterminationRunEntity run = determinationRuns
        .findFirstByTenantIdAndSystemIdOrderByCreatedAtDesc(tenantContext.tenantId(), systemId)
        .orElse(null);
    if (run == null) {
      return List.of();
    }
    Map<String, ControlStatus> controlStatusByCode = controlStatusIndex(systemId);
    List<String> blockers = new ArrayList<>();
    for (DeterminationObligationEntity entity :
        determinationObligations.findAllByRunIdOrderByRuleCodeAsc(run.id())) {
      DeterminationObligation item = entity.toDomain();
      if (item.applicability() != Applicability.APPLICABLE) {
        continue;
      }
      if (corpusForceLookup != null && corpusForceLookup.citesFutureDuty(item.legalRefs())) {
        continue;
      }
      String code = item.ruleCode() == null ? "" : item.ruleCode();
      if (code.startsWith("PROHIBITED_")) {
        blockers.add("PROHIBITED_PRACTICE:" + code);
        continue;
      }
      boolean highOrMedium = "HIGH".equalsIgnoreCase(item.severity())
          || "MEDIUM".equalsIgnoreCase(item.severity());
      if (!highOrMedium || item.controlCodes() == null) {
        continue;
      }
      for (String controlCode : item.controlCodes()) {
        if (controlCode == null || controlCode.isBlank()) {
          continue;
        }
        ControlStatus status = controlStatusByCode.get(controlCode.toUpperCase(Locale.ROOT));
        if (status != ControlStatus.PASS) {
          String marker = "OBLIGATION_UNMET:" + code + ":" + controlCode;
          if (!blockers.contains(marker)) {
            blockers.add(marker);
          }
        }
      }
    }
    return blockers;
  }

  private Map<String, ControlStatus> controlStatusIndex(UUID systemId) {
    Map<UUID, String> codes = controls.findAll().stream()
        .map(ControlEntity::toDomain)
        .collect(Collectors.toMap(c -> c.id(), c -> c.code(), (a, b) -> a));
    Map<String, ControlStatus> index = new LinkedHashMap<>();
    for (SystemControlEntity row : systemControls.findAllByTenantIdAndSystemIdOrderByUpdatedAtDesc(
        tenantContext.tenantId(), systemId)) {
      String code = codes.get(row.controlId());
      if (code != null) {
        index.putIfAbsent(code.toUpperCase(Locale.ROOT), row.status());
      }
    }
    return index;
  }

  private List<String> loadConformityReviewFlags(UUID systemId, RiskClass riskClass) {
    if (conformityService == null || systemId == null) {
      return List.of();
    }
    return conformityService.reviewBlockers(systemId, riskClass);
  }

  private boolean hasOversightGap(List<String> gaps) {
    return gaps.stream().anyMatch(gap -> gap.toLowerCase().contains("oversight"));
  }
}
