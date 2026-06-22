package os.assurance.eu.api.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import os.assurance.eu.api.tenant.UserRole;

@SpringBootTest
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    @Test
    void issuesAndVerifiesAValidToken() {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        String token = jwtService.issueAccessToken(userId, tenantId, UserRole.COMPLIANCE_OFFICER);
        var claims = jwtService.verifyAccessToken(token);

        assertThat(claims).isPresent();
        assertThat(claims.get().userId()).isEqualTo(userId);
        assertThat(claims.get().tenantId()).isEqualTo(tenantId);
        assertThat(claims.get().role()).isEqualTo(UserRole.COMPLIANCE_OFFICER);
    }

    @Test
    void rejectsATamperedToken() {
        String token = jwtService.issueAccessToken(UUID.randomUUID(), UUID.randomUUID(), UserRole.ADMIN);
        String tampered = token.substring(0, token.length() - 4) + "abcd";

        assertThat(jwtService.verifyAccessToken(tampered)).isEmpty();
    }

    @Test
    void rejectsGarbageInput() {
        assertThat(jwtService.verifyAccessToken("not-a-jwt")).isEmpty();
    }

    @Test
    void publishesAJwksWithTheActivePublicKey() {
        var jwkSet = jwtService.currentPublicJwks();

        assertThat(jwkSet.getKeys()).isNotEmpty();
        assertThat(jwkSet.getKeys().get(0).isPrivate()).isFalse();
    }
}