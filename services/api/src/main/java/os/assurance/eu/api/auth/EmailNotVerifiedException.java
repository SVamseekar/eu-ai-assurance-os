package os.assurance.eu.api.auth;

public class EmailNotVerifiedException extends RuntimeException {
  public EmailNotVerifiedException() {
    super("email_not_verified");
  }
}
