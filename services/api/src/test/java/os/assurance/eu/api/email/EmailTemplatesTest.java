package os.assurance.eu.api.email;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EmailTemplatesTest {
  @Test
  void verificationEmailContainsLinkAndDisclaimerButNoComplianceClaims() {
    EmailMessage m = EmailTemplates.verifyEmail("https://app.example/verify-email?token=abc");
    assertThat(m.textBody()).contains("https://app.example/verify-email?token=abc");
    assertThat(m.textBody()).contains("not legal advice");
    assertThat(m.textBody().toLowerCase()).doesNotContain("compliant").doesNotContain("certified");
  }
}
