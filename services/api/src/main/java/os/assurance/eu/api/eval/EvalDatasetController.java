package os.assurance.eu.api.eval;

import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import os.assurance.eu.api.audit.AuditService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/eval-datasets")
public class EvalDatasetController {
  private final EvalDatasetRepository datasets;
  private final AuditService auditService;

  public EvalDatasetController(EvalDatasetRepository datasets, AuditService auditService) {
    this.datasets = datasets;
    this.auditService = auditService;
  }

  @GetMapping
  public List<EvalDataset> listDatasets() {
    return datasets.findAll();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public EvalDataset createDataset(@Valid @RequestBody CreateEvalDatasetRequest request) {
    if (datasets.existsByNameAndVersion(request.name(), request.version())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Eval dataset version already exists");
    }
    EvalDataset dataset = datasets.save(new EvalDataset(
        UUID.randomUUID(),
        request.name(),
        request.version(),
        request.sampleCount(),
        request.golden(),
        Instant.now()));
    auditService.append(
        null,
        "eval_dataset.created",
        "eval_dataset",
        dataset.id().toString(),
        Map.of(
            "name", dataset.name(),
            "version", dataset.version(),
            "sampleCount", dataset.sampleCount(),
            "golden", dataset.golden()));
    return dataset;
  }
}
