package os.assurance.eu.api.contract;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import os.assurance.eu.api.audit.AuditService;
import os.assurance.eu.api.system.AiSystem;
import os.assurance.eu.api.system.AiSystemRepository;
import os.assurance.eu.api.system.DataContractStatus;
import os.assurance.eu.api.system.ReleaseDecision;
import os.assurance.eu.api.system.ReleaseGateService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DataContractService {
  private final DataContractRepository contracts;
  private final DriftEventRepository driftEvents;
  private final AiSystemRepository systems;
  private final ReleaseGateService releaseGateService;
  private final AuditService auditService;

  public DataContractService(
      DataContractRepository contracts,
      DriftEventRepository driftEvents,
      AiSystemRepository systems,
      ReleaseGateService releaseGateService,
      AuditService auditService) {
    this.contracts = contracts;
    this.driftEvents = driftEvents;
    this.systems = systems;
    this.releaseGateService = releaseGateService;
    this.auditService = auditService;
  }

  @Transactional(readOnly = true)
  public List<DataContract> listContracts(UUID systemId) {
    if (systemId == null) {
      return contracts.findAll();
    }
    requireSystem(systemId);
    return contracts.findAllBySystemId(systemId);
  }

  @Transactional(readOnly = true)
  public DataContract getContract(UUID contractId) {
    return requireContract(contractId);
  }

  @Transactional
  public DataContract createContract(CreateDataContractRequest request) {
    requireSystem(request.systemId());
    if (contracts.existsBySystemNameAndVersion(request.systemId(), request.name(), request.version())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Data contract version already exists for system");
    }
    Instant now = Instant.now();
    DataContract saved = contracts.save(new DataContract(
        UUID.randomUUID(),
        request.systemId(),
        request.name(),
        request.owner(),
        request.version(),
        request.status() == null ? DataContractStatus.HEALTHY : request.status(),
        request.coverage() == null ? 100 : request.coverage(),
        now,
        now));
    recalculateSystemContractStatus(saved.systemId());
    auditService.append(
        saved.systemId(),
        "data_contract.created",
        "data_contract",
        saved.id().toString(),
        Map.of("name", saved.name(), "version", saved.version(), "status", saved.status()));
    return saved;
  }

  @Transactional
  public DataContract updateContract(UUID contractId, UpdateDataContractRequest request) {
    DataContract existing = requireContract(contractId);
    String name = request.name() == null ? existing.name() : request.name();
    String version = request.version() == null ? existing.version() : request.version();
    boolean contractKeyChanged = !name.equals(existing.name()) || !version.equals(existing.version());
    if (contractKeyChanged && contracts.existsBySystemNameAndVersion(existing.systemId(), name, version)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Data contract version already exists for system");
    }
    DataContract saved = contracts.save(new DataContract(
        existing.id(),
        existing.systemId(),
        name,
        request.owner() == null ? existing.owner() : request.owner(),
        version,
        request.status() == null ? existing.status() : request.status(),
        request.coverage() == null ? existing.coverage() : request.coverage(),
        existing.createdAt(),
        Instant.now()));
    recalculateSystemContractStatus(saved.systemId());
    auditService.append(
        saved.systemId(),
        "data_contract.updated",
        "data_contract",
        saved.id().toString(),
        Map.of("status", saved.status(), "coverage", saved.coverage()));
    return saved;
  }

  @Transactional(readOnly = true)
  public List<DriftEvent> listDriftEvents(UUID contractId) {
    requireContract(contractId);
    return driftEvents.findAllByContractId(contractId);
  }

  @Transactional
  public DriftEvent createDriftEvent(UUID contractId, DriftEventRequest request) {
    DataContract contract = requireContract(contractId);
    Instant now = Instant.now();
    DriftEvent saved = driftEvents.save(new DriftEvent(
        UUID.randomUUID(),
        contract.id(),
        request.severity(),
        blankToNull(request.field()),
        request.description(),
        DriftStatus.OPEN,
        now,
        now));
    recalculateContractAndSystemStatus(contract.id());
    auditService.append(
        contract.systemId(),
        "data_contract.drift_detected",
        "drift_event",
        saved.id().toString(),
        Map.of(
            "contractId", contract.id(),
            "severity", saved.severity(),
            "field", saved.field() == null ? "" : saved.field()));
    return saved;
  }

  @Transactional
  public DriftEvent updateDriftEvent(UUID contractId, UUID eventId, UpdateDriftEventRequest request) {
    DataContract contract = requireContract(contractId);
    DriftEvent existing = driftEvents.findByContractIdAndId(contractId, eventId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Drift event not found"));
    DriftEvent saved = driftEvents.save(new DriftEvent(
        existing.id(),
        existing.contractId(),
        existing.severity(),
        existing.field(),
        existing.description(),
        request.status(),
        existing.createdAt(),
        Instant.now()));
    recalculateContractAndSystemStatus(contract.id());
    auditService.append(
        contract.systemId(),
        "data_contract.drift_updated",
        "drift_event",
        saved.id().toString(),
        Map.of("contractId", contract.id(), "status", saved.status()));
    return saved;
  }

  private DataContract requireContract(UUID contractId) {
    return contracts.findById(contractId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Data contract not found"));
  }

  private AiSystem requireSystem(UUID systemId) {
    return systems.findById(systemId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "AI system not found"));
  }

  private void recalculateContractAndSystemStatus(UUID contractId) {
    DataContract contract = requireContract(contractId);
    DataContractStatus status = deriveStatus(driftEvents.findAllByContractId(contract.id()));
    contracts.save(new DataContract(
        contract.id(),
        contract.systemId(),
        contract.name(),
        contract.owner(),
        contract.version(),
        status,
        contract.coverage(),
        contract.createdAt(),
        Instant.now()));
    recalculateSystemContractStatus(contract.systemId());
  }

  private void recalculateSystemContractStatus(UUID systemId) {
    AiSystem system = requireSystem(systemId);
    DataContractStatus status = deriveSystemStatus(contracts.findAllBySystemId(systemId));
    ReleaseDecision decision = releaseGateService.calculate(new AiSystem(
        system.id(),
        system.name(),
        system.owner(),
        system.purpose(),
        system.riskClass(),
        system.riskBasis(),
        system.deploymentRegion(),
        system.evidenceCoverage(),
        system.evalScore(),
        status,
        system.releaseDecision(),
        system.openGaps(),
        system.createdAt(),
        Instant.now())).decision();
    systems.save(new AiSystem(
        system.id(),
        system.name(),
        system.owner(),
        system.purpose(),
        system.riskClass(),
        system.riskBasis(),
        system.deploymentRegion(),
        system.evidenceCoverage(),
        system.evalScore(),
        status,
        decision,
        system.openGaps(),
        system.createdAt(),
        Instant.now()));
  }

  private DataContractStatus deriveSystemStatus(List<DataContract> systemContracts) {
    if (systemContracts.stream().anyMatch(contract -> contract.status() == DataContractStatus.BREACH)) {
      return DataContractStatus.BREACH;
    }
    if (systemContracts.stream().anyMatch(contract -> contract.status() == DataContractStatus.WARNING)) {
      return DataContractStatus.WARNING;
    }
    return DataContractStatus.HEALTHY;
  }

  private DataContractStatus deriveStatus(List<DriftEvent> events) {
    List<DriftEvent> open = events.stream()
        .filter(event -> event.status() == DriftStatus.OPEN)
        .toList();
    if (open.stream().anyMatch(event -> event.severity() == DriftSeverity.BREACH)) {
      return DataContractStatus.BREACH;
    }
    if (open.stream().anyMatch(event -> event.severity() == DriftSeverity.WARNING)) {
      return DataContractStatus.WARNING;
    }
    return DataContractStatus.HEALTHY;
  }

  private String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value;
  }
}
