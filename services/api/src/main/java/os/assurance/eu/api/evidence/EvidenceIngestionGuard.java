package os.assurance.eu.api.evidence;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class EvidenceIngestionGuard {
  private static final String CHECKSUM_PATTERN = "^[A-Za-z0-9:_./+=-]{1,128}$";

  private final EvidenceProperties properties;

  public EvidenceIngestionGuard(EvidenceProperties properties) {
    this.properties = properties;
  }

  public void validateDocumentRequest(CreateEvidenceDocumentRequest request) {
    validateSourceUri(request.sourceUri());
    validateChecksum(request.checksum());
    if (request.content() != null && request.content().length() > properties.maxContentCharacters()) {
      throw badRequest("Evidence content exceeds maximum configured length");
    }
    sanitizeMetadata(request.metadata());
  }

  public String validateQuestion(String question) {
    String sanitized = question == null ? "" : question.replaceAll("\\s+", " ").strip();
    if (sanitized.isBlank()) {
      throw badRequest("Evidence question is required");
    }
    if (sanitized.length() > properties.maxQuestionCharacters()) {
      throw badRequest("Evidence question exceeds maximum configured length");
    }
    return sanitized;
  }

  public Map<String, Object> sanitizeMetadata(Map<String, Object> metadata) {
    if (metadata == null || metadata.isEmpty()) {
      return Map.of();
    }
    if (metadata.size() > properties.maxMetadataEntries()) {
      throw badRequest("Evidence metadata has too many entries");
    }
    Map<String, Object> sanitized = new LinkedHashMap<>();
    metadata.forEach((key, value) -> {
      if (key == null || key.isBlank() || key.length() > 64) {
        throw badRequest("Evidence metadata keys must be 1-64 characters");
      }
      if (!key.matches("^[A-Za-z0-9_.-]+$")) {
        throw badRequest("Evidence metadata keys may only contain letters, numbers, dot, underscore, and dash");
      }
      String textValue = String.valueOf(value);
      if (textValue.length() > properties.maxMetadataValueCharacters()) {
        throw badRequest("Evidence metadata values exceed maximum configured length");
      }
      sanitized.put(key, value);
    });
    return sanitized;
  }

  private void validateSourceUri(String sourceUri) {
    URI uri;
    try {
      uri = URI.create(sourceUri);
    } catch (IllegalArgumentException e) {
      throw badRequest("Evidence sourceUri must be a valid URI");
    }
    String scheme = uri.getScheme();
    if (scheme == null || properties.allowedSourceSchemes().stream()
        .noneMatch(allowed -> allowed.equalsIgnoreCase(scheme))) {
      throw badRequest("Evidence sourceUri scheme is not allowed");
    }
    if ("https".equalsIgnoreCase(scheme) && (uri.getHost() == null || uri.getHost().isBlank())) {
      throw badRequest("Evidence https sourceUri must include a host");
    }
  }

  private void validateChecksum(String checksum) {
    if (checksum == null || checksum.isBlank()) {
      return;
    }
    if (!checksum.matches(CHECKSUM_PATTERN)) {
      throw badRequest("Evidence checksum contains unsupported characters");
    }
    String lower = checksum.toLowerCase(Locale.ROOT);
    if (lower.contains("secret") || lower.contains("token") || lower.contains("password")) {
      throw badRequest("Evidence checksum must not contain sensitive labels");
    }
  }

  private ResponseStatusException badRequest(String reason) {
    return new ResponseStatusException(HttpStatus.BAD_REQUEST, reason);
  }
}
