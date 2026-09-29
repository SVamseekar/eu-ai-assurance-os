package os.assurance.eu.api.ops;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class ProductionSecretsGuardTest {
  private static final String STRONG = "a".repeat(20) + "B7!" + "c".repeat(20);

  @Test
  void flagsDefaultsBlanksAndShortSecrets() {
    var violations = ProductionSecretsGuard.violations(Map.of(
        "assurance.audit.chain-secret", "local-dev-audit-chain-secret",
        "assurance.oauth.state-secret", "",
        "assurance.eval.callback.secret", "short",
        "assurance.auth.key-encryption-secret", STRONG));
    assertThat(violations).containsExactlyInAnyOrder(
        "assurance.audit.chain-secret", "assurance.oauth.state-secret", "assurance.eval.callback.secret");
  }

  @Test
  void acceptsStrongDistinctSecrets() {
    assertThat(ProductionSecretsGuard.violations(Map.of(
        "assurance.audit.chain-secret", STRONG + "1",
        "assurance.oauth.state-secret", STRONG + "2",
        "assurance.eval.callback.secret", STRONG + "3",
        "assurance.auth.key-encryption-secret", STRONG + "4"))).isEmpty();
  }
}
