package os.assurance.eu.api.determination;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import os.assurance.eu.api.audit.AuditService;
import os.assurance.eu.api.control.ControlService;
import os.assurance.eu.api.system.AiSystem;
import os.assurance.eu.api.system.AiSystemRepository;
import os.assurance.eu.api.system.RiskClass;
import os.assurance.eu.api.tenant.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DeterminationService {
  private final ObligationRuleJpaRepository rules;
  private final DeterminationRunJpaRepository runs;
  private final DeterminationObligationJpaRepository obligations;
  private final ObligationRuleEngine ruleEngine;
  private final AiSystemRepository systems;
  private final ControlService controlService;
  private final AuditService auditService;
  private final TenantContext tenantContext;

  public DeterminationService(
      ObligationRuleJpaRepository rules,
      DeterminationRunJpaRepository runs,
      DeterminationObligationJpaRepository obligations,
      ObligationRuleEngine ruleEngine,
      AiSystemRepository systems,
      ControlService controlService,
      AuditService auditService,
      TenantContext tenantContext) {
    this.rules = rules;
    this.runs = runs;
    this.obligations = obligations;
    this.ruleEngine = ruleEngine;
    this.systems = systems;
    this.controlService = controlService;
    this.auditService = auditService;
    this.tenantContext = tenantContext;
  }

  @Transactional(readOnly = true)
  public QuestionnaireDefinition questionnaire() {
    return QuestionnaireDefinition.v1();
  }

  @Transactional
  public DeterminationRun createRun(UUID systemId, Map<String, Object> answers) {
    AiSystem system = requireSystem(systemId);
    if (answers == null || answers.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Questionnaire answers are required");
    }

    String rulesetVersion = DeterminationDisclaimers.RULESET_VERSION;
    List<ObligationRule> activeRules = rules
        .findAllByRulesetVersionAndActiveTrueOrderByCodeAsc(rulesetVersion)
        .stream()
        .map(ObligationRuleEntity::toDomain)
        .toList();
    if (activeRules.isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE, "No obligation rules seeded for " + rulesetVersion);
    }

    List<DeterminationObligation> evaluated = ruleEngine.evaluate(activeRules, answers);
    Map<String, Object> riskSuggestion = suggestRiskClass(evaluated, answers, system.riskClass());

    UUID runId = UUID.randomUUID();
    Instant now = Instant.now();
    Map<String, Object> resultSummary = buildResultSummary(evaluated, riskSuggestion, rulesetVersion);

    DeterminationRunEntity runEntity = new DeterminationRunEntity(
        runId,
        tenantContext.tenantId(),
        system.id(),
        new LinkedHashMap<>(answers),
        resultSummary,
        "COMPLETED",
        rulesetVersion,
        tenantContext.actorId(),
        now);
    runs.save(runEntity);

    List<DeterminationObligation> saved = new ArrayList<>();
    for (DeterminationObligation item : evaluated) {
      DeterminationObligationEntity entity = new DeterminationObligationEntity(
          UUID.randomUUID(),
          runId,
          item.ruleCode(),
          item.applicability(),
          item.rationale(),
          item.controlCodes(),
          item.legalRefs(),
          item.title(),
          item.severity());
      obligations.save(entity);
      saved.add(entity.toDomain());
    }

    List<String> applicableControlCodes = saved.stream()
        .filter(o -> o.applicability() == Applicability.APPLICABLE)
        .flatMap(o -> o.controlCodes().stream())
        .distinct()
        .toList();
    List<String> openedControls = controlService.openControlsForReview(system.id(), applicableControlCodes);

    Map<String, Object> auditPayload = new LinkedHashMap<>();
    auditPayload.put("runId", runId.toString());
    auditPayload.put("rulesetVersion", rulesetVersion);
    auditPayload.put("productLabel", DeterminationDisclaimers.METRICS_LABEL);
    auditPayload.put("disclaimer", DeterminationDisclaimers.SHORT);
    auditPayload.put("applicableCount", countBy(saved, Applicability.APPLICABLE));
    auditPayload.put("uncertainCount", countBy(saved, Applicability.UNCERTAIN));
    auditPayload.put("notApplicableCount", countBy(saved, Applicability.NOT_APPLICABLE));
    auditPayload.put("suggestedRiskClass", riskSuggestion.get("suggestedRiskClass"));
    auditPayload.put("riskAutoApplied", false);
    auditPayload.put("controlsOpenedForReview", openedControls);
    auditService.append(
        system.id(),
        "determination.run.completed",
        "determination_run",
        runId.toString(),
        auditPayload);

    return toRun(runEntity, saved);
  }

  @Transactional(readOnly = true)
  public DeterminationRun getRun(UUID systemId, UUID runId) {
    requireSystem(systemId);
    DeterminationRunEntity run = runs.findByIdAndTenantId(runId, tenantContext.tenantId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Determination run not found"));
    if (!run.systemId().equals(systemId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Determination run not found");
    }
    List<DeterminationObligation> items = obligations.findAllByRunIdOrderByRuleCodeAsc(runId).stream()
        .map(DeterminationObligationEntity::toDomain)
        .toList();
    return toRun(run, items);
  }

  @Transactional(readOnly = true)
  public List<DeterminationRun> listRuns(UUID systemId) {
    requireSystem(systemId);
    return runs.findAllByTenantIdAndSystemIdOrderByCreatedAtDesc(tenantContext.tenantId(), systemId)
        .stream()
        .map(run -> {
          List<DeterminationObligation> items =
              obligations.findAllByRunIdOrderByRuleCodeAsc(run.id()).stream()
                  .map(DeterminationObligationEntity::toDomain)
                  .toList();
          return toRun(run, items);
        })
        .toList();
  }

  @Transactional(readOnly = true)
  public Map<String, Object> latestSnapshotForPack(UUID systemId) {
    return runs.findFirstByTenantIdAndSystemIdOrderByCreatedAtDesc(tenantContext.tenantId(), systemId)
        .map(run -> {
          List<DeterminationObligation> items =
              obligations.findAllByRunIdOrderByRuleCodeAsc(run.id()).stream()
                  .map(DeterminationObligationEntity::toDomain)
                  .toList();
          DeterminationRun full = toRun(run, items);
          Map<String, Object> snap = new LinkedHashMap<>();
          snap.put("disclaimer", DeterminationDisclaimers.FULL);
          snap.put("productLabel", DeterminationDisclaimers.METRICS_LABEL);
          snap.put("runId", full.id().toString());
          snap.put("rulesetVersion", full.rulesetVersion());
          snap.put("status", full.status());
          snap.put("createdAt", full.createdAt().toString());
          snap.put("result", full.result());
          snap.put("obligations", items.stream().map(this::obligationMap).toList());
          return snap;
        })
        .orElseGet(() -> {
          Map<String, Object> empty = new LinkedHashMap<>();
          empty.put("disclaimer", DeterminationDisclaimers.FULL);
          empty.put("productLabel", DeterminationDisclaimers.METRICS_LABEL);
          empty.put("runId", null);
          empty.put("rulesetVersion", DeterminationDisclaimers.RULESET_VERSION);
          empty.put("status", "NONE");
          empty.put("message", "No assisted determination run recorded for this system.");
          empty.put("obligations", List.of());
          return empty;
        });
  }

  private Map<String, Object> buildResultSummary(
      List<DeterminationObligation> evaluated,
      Map<String, Object> riskSuggestion,
      String rulesetVersion) {
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("disclaimer", DeterminationDisclaimers.FULL);
    result.put("productLabel", DeterminationDisclaimers.METRICS_LABEL);
    result.put("rulesetVersion", rulesetVersion);
    result.put("applicableCount", countBy(evaluated, Applicability.APPLICABLE));
    result.put("uncertainCount", countBy(evaluated, Applicability.UNCERTAIN));
    result.put("notApplicableCount", countBy(evaluated, Applicability.NOT_APPLICABLE));
    result.put("riskSuggestion", riskSuggestion);
    result.put(
        "applicableRuleCodes",
        evaluated.stream()
            .filter(o -> o.applicability() == Applicability.APPLICABLE)
            .map(DeterminationObligation::ruleCode)
            .toList());
    result.put(
        "uncertainRuleCodes",
        evaluated.stream()
            .filter(o -> o.applicability() == Applicability.UNCERTAIN)
            .map(DeterminationObligation::ruleCode)
            .toList());
    return result;
  }

  /**
   * Suggest risk class only. Never auto-applies to the AI system record.
   */
  Map<String, Object> suggestRiskClass(
      List<DeterminationObligation> evaluated,
      Map<String, Object> answers,
      RiskClass current) {
    Set<String> highRules = evaluated.stream()
        .filter(o -> o.applicability() == Applicability.APPLICABLE)
        .filter(o -> "HIGH".equalsIgnoreCase(o.severity()))
        .map(DeterminationObligation::ruleCode)
        .collect(Collectors.toCollection(LinkedHashSet::new));

    boolean anyUncertainHigh = evaluated.stream()
        .anyMatch(o -> o.applicability() == Applicability.UNCERTAIN
            && "HIGH".equalsIgnoreCase(o.severity()));

    boolean transparencyOnly = evaluated.stream()
        .anyMatch(o -> o.applicability() == Applicability.APPLICABLE
            && "TRANSPARENCY_NATURAL_PERSONS".equals(o.ruleCode()));

    String suggested;
    String rationale;
    if (!highRules.isEmpty()) {
      suggested = RiskClass.HIGH.name();
      rationale = "HIGH-severity applicable obligations suggested: " + String.join(", ", highRules)
          + ". Human confirmation required before changing system risk class.";
    } else if (anyUncertainHigh) {
      suggested = current == null ? RiskClass.LIMITED.name() : current.name();
      rationale = "Uncertain high-severity indicators present; retain current class pending legal review.";
    } else if (transparencyOnly || truthy(answers.get("interacts_with_natural_persons"))) {
      suggested = RiskClass.LIMITED.name();
      rationale = "Transparency-style interaction indicated without high-severity applicable rules.";
    } else {
      suggested = RiskClass.MINIMAL.name();
      rationale = "No high-severity obligations suggested as applicable from questionnaire.";
    }

    Map<String, Object> suggestion = new LinkedHashMap<>();
    suggestion.put("suggestedRiskClass", suggested);
    suggestion.put("currentRiskClass", current == null ? null : current.name());
    suggestion.put("autoApplied", false);
    suggestion.put("requiresHumanConfirm", true);
    suggestion.put("rationale", rationale);
    suggestion.put(
        "note",
        "Risk class is never changed automatically by assisted determination.");
    return suggestion;
  }

  private boolean truthy(Object value) {
    if (value instanceof Boolean b) {
      return b;
    }
    if (value == null) {
      return false;
    }
    String s = value.toString().trim().toLowerCase(Locale.ROOT);
    return "true".equals(s) || "yes".equals(s);
  }

  private long countBy(List<DeterminationObligation> items, Applicability applicability) {
    return items.stream().filter(o -> o.applicability() == applicability).count();
  }

  private DeterminationRun toRun(DeterminationRunEntity run, List<DeterminationObligation> items) {
    return new DeterminationRun(
        run.id(),
        run.systemId(),
        run.questionnaireJson(),
        run.resultJson(),
        run.status(),
        run.rulesetVersion(),
        run.createdBy(),
        run.createdAt(),
        items,
        DeterminationDisclaimers.FULL);
  }

  private Map<String, Object> obligationMap(DeterminationObligation o) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("ruleCode", o.ruleCode());
    map.put("title", o.title());
    map.put("applicability", o.applicability().name());
    map.put("rationale", o.rationale());
    map.put("controlCodes", o.controlCodes());
    map.put("legalRefs", o.legalRefs());
    map.put("severity", o.severity());
    return map;
  }

  private AiSystem requireSystem(UUID systemId) {
    return systems.findById(systemId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "AI system not found"));
  }
}
