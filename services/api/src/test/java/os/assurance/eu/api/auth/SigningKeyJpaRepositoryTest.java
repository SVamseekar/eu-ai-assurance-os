package os.assurance.eu.api.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class SigningKeyJpaRepositoryTest {

    @org.springframework.beans.factory.annotation.Autowired
    private SigningKeyJpaRepository repository;

    @Test
    void findsTheActiveKey() {
        UUID kid = UUID.randomUUID();
        repository.save(new SigningKeyEntity(kid, "RS256", "pub-pem", "priv-pem", Instant.now(), true));

        assertThat(repository.findByActiveTrue()).isPresent();
        assertThat(repository.findByActiveTrue().get().kid()).isEqualTo(kid);
    }

    @Test
    void returnsEmptyWhenNoActiveKey() {
        repository.save(new SigningKeyEntity(UUID.randomUUID(), "RS256", "pub-pem", "priv-pem", Instant.now(), false));

        assertThat(repository.findByActiveTrue()).isEmpty();
    }
}