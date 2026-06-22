package os.assurance.eu.api.evidence;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SSLParameters;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TextExtractionService {
    private static final Logger log = LoggerFactory.getLogger(TextExtractionService.class);
    private static final int MAX_FETCH_CHARS = 20 * 1024 * 1024;
    private final Tika tika = new Tika();
    private final FileStorageService fileStorage;
    private final FileStorageProperties storageProps;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public TextExtractionService(FileStorageService fileStorage, FileStorageProperties storageProps) {
        this.fileStorage = fileStorage;
        this.storageProps = storageProps;
    }

    public String extract(CreateEvidenceDocumentRequest request) {
        if (request.content() != null && !request.content().isBlank()) {
            return request.content().strip();
        }
        String scheme = URI.create(request.sourceUri()).getScheme().toLowerCase(Locale.ROOT);
        if ("https".equals(scheme)) {
            URI uri = URI.create(request.sourceUri());
            InetAddress validatedAddress = validateNoSsrf(uri);
            String originalHost = uri.getHost();
            try {
                URI pinnedUri = new URI(
                    uri.getScheme(), uri.getUserInfo(), validatedAddress.getHostAddress(),
                    uri.getPort(), uri.getPath(), uri.getQuery(), uri.getFragment());
                HttpClient pinnedClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .followRedirects(HttpClient.Redirect.NEVER)
                    .sslParameters(sniParametersFor(originalHost))
                    .build();
                HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(pinnedUri)
                    .timeout(Duration.ofSeconds(30))
                    .header("Host", originalHost)
                    .GET()
                    .build();
                HttpResponse<InputStream> response = pinnedClient.send(
                    httpRequest, HttpResponse.BodyHandlers.ofInputStream());
                try (InputStream body = response.body()) {
                    String extracted = tika.parseToString(body, new Metadata(), MAX_FETCH_CHARS);
                    if (extracted != null && !extracted.isBlank()) {
                        return extracted.strip();
                    }
                }
            } catch (ResponseStatusException e) {
                throw e;
            } catch (Exception e) {
                log.warn("Text extraction failed for {}: {}", request.sourceUri(), e.getMessage());
            }
        }
        if ("s3".equals(scheme)) {
            if (fileStorage != null) {
                URI uri = URI.create(request.sourceUri());
                String bucket = uri.getHost();
                String key = uri.getPath().replaceFirst("^/", "");
                if (storageProps != null && !storageProps.bucket().equals(bucket)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "S3 URI references a bucket that is not configured for this deployment");
                }
                try (InputStream body = fileStorage.download(bucket, key)) {
                    String extracted = tika.parseToString(body, new Metadata(), MAX_FETCH_CHARS);
                    if (extracted != null && !extracted.isBlank()) {
                        return extracted.strip();
                    }
                } catch (Exception e) {
                    log.warn("S3 extraction failed for {}: {}", request.sourceUri(), e.getMessage());
                }
            }
        }
        return metadataStub(request);
    }

    private InetAddress validateNoSsrf(URI uri) {
        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid source URI: no host");
        }
        try {
            InetAddress[] addresses = InetAddress.getAllByName(host);
            for (InetAddress addr : addresses) {
                if (addr.isLoopbackAddress()
                        || addr.isLinkLocalAddress()
                        || addr.isSiteLocalAddress()
                        || addr.isAnyLocalAddress()
                        || addr.isMulticastAddress()) {
                    throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Source URI resolves to a private or reserved address");
                }
            }
            return addresses[0];
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Source URI host could not be resolved");
        }
    }

    private SSLParameters sniParametersFor(String hostname) {
        SSLParameters params = new SSLParameters();
        params.setServerNames(java.util.List.of(new SNIHostName(hostname)));
        return params;
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