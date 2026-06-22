package os.assurance.eu.api.evidence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class TextExtractionServiceTest {

    private final TextExtractionService service = new TextExtractionService(null, null);

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
    void validateNoSsrfReturnsTheValidatedAddressForReuseByTheCaller() throws Exception {
        var method = TextExtractionService.class.getDeclaredMethod("validateNoSsrf", java.net.URI.class);
        method.setAccessible(true);
        var result = method.invoke(service, java.net.URI.create("https://example.com/doc"));

        assertThat(result).isInstanceOf(java.net.InetAddress.class);
    }
}