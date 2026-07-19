package os.assurance.eu.api.sector;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sector-packs")
public class SectorPackController {
  private final SectorPackService sectorPackService;

  public SectorPackController(SectorPackService sectorPackService) {
    this.sectorPackService = sectorPackService;
  }

  @GetMapping
  public SectorPacksResponse list() {
    return sectorPackService.listPacks();
  }

  @GetMapping("/{packId}")
  public SectorPackView get(@PathVariable String packId) {
    return sectorPackService.getPack(packId);
  }

  @GetMapping("/resolve")
  public SectorPackView resolve(@RequestParam String sector) {
    return sectorPackService.resolveForSector(sector);
  }

  @GetMapping("/{packId}/templates/{templateId}")
  public SectorTemplateContentResponse template(
      @PathVariable String packId, @PathVariable String templateId) {
    return sectorPackService.loadTemplate(packId, templateId);
  }
}
