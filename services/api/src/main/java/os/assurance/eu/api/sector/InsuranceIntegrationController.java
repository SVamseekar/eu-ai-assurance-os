package os.assurance.eu.api.sector;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Optional insurance integration stubs. No live proprietary vendor calls.
 */
@RestController
@RequestMapping("/api/v1/integrations")
public class InsuranceIntegrationController {
  private final SectorPackService sectorPackService;

  public InsuranceIntegrationController(SectorPackService sectorPackService) {
    this.sectorPackService = sectorPackService;
  }

  @PostMapping("/insurance/claims-model-register")
  @ResponseStatus(HttpStatus.CREATED)
  public ClaimsModelRegisterResponse registerClaimsModel(
      @Valid @RequestBody ClaimsModelRegisterRequest request) {
    return sectorPackService.registerClaimsModel(request);
  }

  @GetMapping("/connectors/model-inventory")
  public Map<String, Object> modelInventory() {
    List<Map<String, Object>> models = sectorPackService.pullModelInventoryStub();
    return Map.of(
        "models", models,
        "connectorMode", "stub",
        "disclaimer", SectorPackDisclaimers.SCOPE,
        "metricsLabel", SectorPackDisclaimers.METRICS_LABEL);
  }
}
