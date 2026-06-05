package os.assurance.eu.api.contract;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/data-contracts")
public class DataContractController {
  private final DataContractService service;

  public DataContractController(DataContractService service) {
    this.service = service;
  }

  @GetMapping
  public List<DataContract> listContracts(@RequestParam(required = false) UUID systemId) {
    return service.listContracts(systemId);
  }

  @GetMapping("/{contractId}")
  public DataContract getContract(@PathVariable UUID contractId) {
    return service.getContract(contractId);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public DataContract createContract(@Valid @RequestBody CreateDataContractRequest request) {
    return service.createContract(request);
  }

  @PatchMapping("/{contractId}")
  public DataContract updateContract(
      @PathVariable UUID contractId,
      @Valid @RequestBody UpdateDataContractRequest request) {
    return service.updateContract(contractId, request);
  }

  @GetMapping("/{contractId}/drift-events")
  public List<DriftEvent> listDriftEvents(@PathVariable UUID contractId) {
    return service.listDriftEvents(contractId);
  }

  @PostMapping("/{contractId}/drift-events")
  @ResponseStatus(HttpStatus.CREATED)
  public DriftEvent createDriftEvent(
      @PathVariable UUID contractId,
      @Valid @RequestBody DriftEventRequest request) {
    return service.createDriftEvent(contractId, request);
  }

  @PatchMapping("/{contractId}/drift-events/{eventId}")
  public DriftEvent updateDriftEvent(
      @PathVariable UUID contractId,
      @PathVariable UUID eventId,
      @Valid @RequestBody UpdateDriftEventRequest request) {
    return service.updateDriftEvent(contractId, eventId, request);
  }
}
