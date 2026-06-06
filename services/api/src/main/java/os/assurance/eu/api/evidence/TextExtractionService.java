package os.assurance.eu.api.evidence;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TextExtractionService {
    private static final Logger log = LoggerFactory.getLogger(TextExtractionService.class);
    private static final int MAX_FETCH_CHARS = 20 * 1024 * 1024;
    private final Tika tika = new Tika();
    private final HttpClient http = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    public String extract(CreateEvidenceDocumentRequest request) {
        if (request.content() != null && !request.content().isBlank()) {
            return request.content().strip();
        }
        String scheme = URI.create(request.sourceUri()).getScheme().toLowerCase(Locale.ROOT);
        if ("https".equals(scheme)) {
            try {
                HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(request.sourceUri()))
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();
                HttpResponse<InputStream> response = http.send(
                    httpRequest, HttpResponse.BodyHandlers.ofInputStream());
                try (InputStream body = response.body()) {
                    String extracted = tika.parseToString(body, new Metadata(), MAX_FETCH_CHARS);
                    if (extracted != null && !extracted.isBlank()) {
                        return extracted.strip();
                    }
                }
            } catch (Exception e) {
                log.warn("Text extraction failed for {}: {}", request.sourceUri(), e.getMessage());
            }
        }
        return metadataStub(request);
    }

    private String metadataStub(CreateEvidenceDocumentRequest request) {
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
