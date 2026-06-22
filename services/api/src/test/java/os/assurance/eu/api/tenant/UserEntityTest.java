package os.assurance.eu.api.tenant;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserEntityTest {

    @Test
    void storesAndExposesThePasswordHash() {
        UserEntity user = new UserEntity(
            UUID.randomUUID(), UUID.randomUUID(), "test@example.com",
            UserRole.ADMIN, "bcrypt-hash-value", Instant.now());

        assertThat(user.passwordHash()).isEqualTo("bcrypt-hash-value");
    }
}