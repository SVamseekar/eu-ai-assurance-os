package os.assurance.eu.api.evidence;

import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class TextExtractionService {
  public String extract(CreateEvidenceDocumentRequest request) {
    if (request.content() != null && !request.content().isBlank()) {
      return request.content().strip();
    }
    return """
        Evidence document: %s
        Type: %s
        Source: %s
        This metadata-only evidence record has been indexed. Upload extracted text in the content field to enable richer retrieval.
        """
        .formatted(request.title(), request.type().toUpperCase(Locale.ROOT), request.sourceUri())
        .strip();
  }
}
