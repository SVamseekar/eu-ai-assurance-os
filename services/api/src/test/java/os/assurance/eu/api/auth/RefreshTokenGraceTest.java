package os.assurance.eu.api.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import os.assurance.eu.api.tenant.TenantContext;

@SpringBootTest(properties = {
    "assurance.eval.worker.enabled=false",
    "assurance.eval.callback.secret=test-eval-callback-secret"
})
class RefreshTokenGraceTest {
  @Autowired RefreshTokenService service;

  @Test
  void immediateReuseWithinGraceWindowIssuesSiblingWithoutChainRevocation() {
    var issued = service.issue(TenantContext.DEFAULT_USER_ID, TenantContext.DEFAULT_TENANT_ID);
    var firstRotation = service.rotate(issued.rawToken());
    assertThat(firstRotation).isInstanceOf(RefreshTokenService.RefreshResult.Rotated.class);
    var rotatedA = (RefreshTokenService.RefreshResult.Rotated) firstRotation;

    var secondRotation = service.rotate(issued.rawToken());
    assertThat(secondRotation).isInstanceOf(RefreshTokenService.RefreshResult.Rotated.class);
    var rotatedB = (RefreshTokenService.RefreshResult.Rotated) secondRotation;

    assertThat(service.rotate(rotatedA.newToken().rawToken()))
        .isInstanceOf(RefreshTokenService.RefreshResult.Rotated.class);
    assertThat(service.rotate(rotatedB.newToken().rawToken()))
        .isInstanceOf(RefreshTokenService.RefreshResult.Rotated.class);
  }
}
