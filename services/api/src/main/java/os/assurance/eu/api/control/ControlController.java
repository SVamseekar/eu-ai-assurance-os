package os.assurance.eu.api.control;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ControlController {
  private final ControlService controlService;

  public ControlController(ControlService controlService) {
    this.controlService = controlService;
  }

  @GetMapping("/controls")
  public List<Control> listControls() {
    return controlService.listCatalog();
  }

  @GetMapping("/systems/{systemId}/controls")
  public List<SystemControl> listSystemControls(@PathVariable UUID systemId) {
    return controlService.listForSystem(systemId);
  }

  @PutMapping("/systems/{systemId}/controls/{controlId}")
  public SystemControl updateSystemControl(
      @PathVariable UUID systemId,
      @PathVariable UUID controlId,
      @Valid @RequestBody UpdateSystemControlRequest request) {
    return controlService.updateSystemControl(systemId, controlId, request);
  }
}
