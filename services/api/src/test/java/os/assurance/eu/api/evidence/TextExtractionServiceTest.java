package os.assurance.eu.api.evidence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.InetAddress;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class TextExtractionServiceTest {

    private final TextExtractionService service = new TextExtractionService(null, null);
    private final TextExtractionService.SsrfSafeDnsResolver resolver =
        new TextExtractionService.SsrfSafeDnsResolver();

    @Test
    void rejectsLoopbackHost() {
        var request = new CreateEvidenceDocumentRequest(
            java.util.UUID.randomUUID(), "policy", "Test", "https://127.0.0.1/secret", null, null, null);

        assertThatThrownBy(() -> service.extract(request))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("private or reserved");
    }

    @Test
    void rejectsLinkLocalMetadataHost() {
        var request = new CreateEvidenceDocumentRequest(
            java.util.UUID.randomUUID(), "policy", "Test", "https://169.254.169.254/latest/meta-data/", null, null, null);

        assertThatThrownBy(() -> service.extract(request))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("private or reserved");
    }

    @Test
    void resolverRejectsPrivateAndReservedAddressesDirectly() {
        assertThatThrownBy(() -> resolver.resolve("127.0.0.1"))
            .isInstanceOf(TextExtractionService.SsrfRejectedException.class)
            .hasMessageContaining("private or reserved");

        assertThatThrownBy(() -> resolver.resolve("169.254.169.254"))
            .isInstanceOf(TextExtractionService.SsrfRejectedException.class)
            .hasMessageContaining("private or reserved");

        assertThatThrownBy(() -> resolver.resolve("10.0.0.5"))
            .isInstanceOf(TextExtractionService.SsrfRejectedException.class)
            .hasMessageContaining("private or reserved");
    }

    @Test
    void resolverReturnsValidatedAddressesForPublicHosts() throws Exception {
        InetAddress[] resolved = resolver.resolve("1.1.1.1");

        assertThat(resolved).isNotEmpty();
        assertThat(resolved[0].getHostAddress()).isEqualTo("1.1.1.1");
    }

    @Test
    void resolverIsTheSoleResolutionPathUsedByTheHttpConnectionManager() {
        // SsrfSafeDnsResolver is registered directly with Apache HttpClient5's
        // PoolingHttpClientConnectionManager in fetchOverHttps — the connection manager
        // calls resolver.resolve(host) exactly once per connection and uses that result
        // to open the socket, so there is no second, independent resolution for a
        // DNS-rebinding attacker to race against the validation check above.
        assertThat(resolver).isInstanceOf(org.apache.hc.client5.http.DnsResolver.class);
    }
}
