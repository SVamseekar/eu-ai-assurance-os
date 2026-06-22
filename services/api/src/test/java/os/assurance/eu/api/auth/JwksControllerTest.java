package os.assurance.eu.api.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.DefaultResponseErrorHandler;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JwksControllerTest {

    @Autowired
    private TestRestTemplate rest;

    @BeforeEach
    void configureRestTemplate() {
        var restTemplate = rest.getRestTemplate();
        restTemplate.setRequestFactory(new JdkClientHttpRequestFactory());
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(org.springframework.http.client.ClientHttpResponse response) {
                return false;
            }
        });
    }

    @Test
    void jwksEndpointIsReachableWithoutAuthenticationAndReturnsAPublicKey() {
        var response = rest.getForEntity("/.well-known/jwks.json", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"keys\"");
        assertThat(response.getBody()).doesNotContain("RSAPrivateKey");
    }
}