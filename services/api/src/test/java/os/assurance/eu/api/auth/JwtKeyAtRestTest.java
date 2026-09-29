package os.assurance.eu.api.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import os.assurance.eu.api.tenant.UserRole;

@SpringBootTest(properties = {
    "assurance.eval.worker.enabled=false",
    "assurance.eval.callback.secret=test-eval-callback-secret",
    "assurance.auth.key-encryption-secret=test-key-encryption-secret-at-least-32-chars-long",
    "spring.datasource.url=jdbc:h2:mem:jwt_key_at_rest;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1"
})
class JwtKeyAtRestTest {
  @Autowired SigningKeyJpaRepository signingKeys;
  @Autowired JwtService jwtService;

  @Test
  void signingKeyIsEncryptedAtRestAndStillIssuesValidTokens() {
    SigningKeyEntity active = signingKeys.findByActiveTrue().orElseThrow();
    assertThat(active.privateKeyPem()).startsWith("enc:v1:");

    UUID userId = UUID.randomUUID();
    UUID tenantId = UUID.randomUUID();
    String token = jwtService.issueAccessToken(userId, tenantId, UserRole.ADMIN);
    var claims = jwtService.verifyAccessToken(token);

    assertThat(claims).isPresent();
    assertThat(claims.get().userId()).isEqualTo(userId);
  }
}
