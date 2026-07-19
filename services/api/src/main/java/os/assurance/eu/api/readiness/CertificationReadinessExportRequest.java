package os.assurance.eu.api.readiness;

import jakarta.validation.constraints.Pattern;

public record CertificationReadinessExportRequest(
    @Pattern(regexp = "(?i)json|pdf", message = "format must be json or pdf")
    String format) {

  public String normalisedFormat() {
    if (format == null || format.isBlank()) {
      return "json";
    }
    return format.trim().toLowerCase();
  }
}
