package os.assurance.eu.api.auth;

public class TooManyAttemptsException extends RuntimeException {
  public TooManyAttemptsException() {
    super("Too many attempts. Try again later.");
  }
}
