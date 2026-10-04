package os.assurance.eu.api.email;

public record EmailMessage(String to, String subject, String textBody) {
  public EmailMessage withTo(String recipient) {
    return new EmailMessage(recipient, subject, textBody);
  }
}
