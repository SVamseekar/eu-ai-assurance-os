package os.assurance.eu.api.evidence;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Locale;
import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.util.Timeout;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Fetches evidence document content over HTTPS, S3, or inline content.
 *
 * <p>SSRF defense: {@link SsrfSafeDnsResolver} is the only DNS resolution path used to open
 * the outbound socket. It resolves the hostname exactly once, validates the resolved address
 * against private/loopback/link-local/multicast ranges, and returns that single validated
 * address to Apache HttpClient5's connection manager for the actual connect. Because there is
 * no second, independent resolution between validation and connection (unlike the JDK's
 * {@code java.net.http.HttpClient}, which re-resolves the hostname itself at connect time),
 * a DNS-rebinding attacker cannot swap the address after validation passes.
 */
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
            String extracted = fetchOverHttps(request);
            if (extracted != null) {
                return extracted;
            }
        }
        if ("s3".equals(scheme)) {
            String extracted = fetchFromS3(request);
            if (extracted != null) {
                return extracted;
            }
        }
        return metadataStub(request);
    }

    private String fetchOverHttps(CreateEvidenceDocumentRequest request) {
        URI uri = URI.create(request.sourceUri());
        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid source URI: no host");
        }

        SsrfSafeDnsResolver resolver = new SsrfSafeDnsResolver();
        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
            .setDnsResolver(resolver)
            .build();

        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(Timeout.ofSeconds(10))
            .setResponseTimeout(Timeout.ofSeconds(30))
            .setRedirectsEnabled(false)
            .build();

        try (CloseableHttpClient client = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .disableRedirectHandling()
                .build()) {
            HttpGet getRequest = new HttpGet(uri);
            return client.execute(getRequest, response -> {
                try (InputStream body = response.getEntity().getContent()) {
                    String extracted = tika.parseToString(body, new Metadata(), MAX_FETCH_CHARS);
                    EntityUtils.consume(response.getEntity());
                    return (extracted != null && !extracted.isBlank()) ? extracted.strip() : null;
                } catch (Exception e) {
                    throw new IOException(e);
                }
            });
        } catch (SsrfRejectedException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.warn("Text extraction failed for {}: {}", request.sourceUri(), e.getMessage());
            return null;
        } finally {
            connectionManager.close();
        }
    }

    private String fetchFromS3(CreateEvidenceDocumentRequest request) {
        if (fileStorage == null) {
            return null;
        }
        URI uri = URI.create(request.sourceUri());
        String bucket = uri.getHost();
        String key = uri.getPath().replaceFirst("^/", "");
        if (storageProps != null && !storageProps.bucket().equals(bucket)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "S3 URI references a bucket that is not configured for this deployment");
        }
        try (InputStream body = fileStorage.download(bucket, key)) {
            String extracted = tika.parseToString(body, new Metadata(), MAX_FETCH_CHARS);
            return (extracted != null && !extracted.isBlank()) ? extracted.strip() : null;
        } catch (Exception e) {
            log.warn("S3 extraction failed for {}: {}", request.sourceUri(), e.getMessage());
            return null;
        }
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

    /**
     * Resolves a hostname exactly once per connection and rejects private, loopback,
     * link-local, any-local, or multicast addresses. This is registered directly with
     * Apache HttpClient5's connection manager, so the address it validates is the same
     * address used to open the socket — there is no separate, later resolution for an
     * attacker to race via DNS rebinding.
     */
    static final class SsrfSafeDnsResolver implements DnsResolver {
        @Override
        public InetAddress[] resolve(String host) throws UnknownHostException {
            InetAddress[] addresses = InetAddress.getAllByName(host);
            for (InetAddress addr : addresses) {
                if (addr.isLoopbackAddress()
                        || addr.isLinkLocalAddress()
                        || addr.isSiteLocalAddress()
                        || addr.isAnyLocalAddress()
                        || addr.isMulticastAddress()) {
                    throw new SsrfRejectedException(
                        "Source URI resolves to a private or reserved address");
                }
            }
            return addresses;
        }

        @Override
        public String resolveCanonicalHostname(String host) throws UnknownHostException {
            return InetAddress.getByName(host).getCanonicalHostName();
        }
    }

    static final class SsrfRejectedException extends UnknownHostException {
        SsrfRejectedException(String message) {
            super(message);
        }
    }
}
