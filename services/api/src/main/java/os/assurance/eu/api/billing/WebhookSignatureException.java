package os.assurance.eu.api.billing;

public class WebhookSignatureException extends RuntimeException {
  public WebhookSignatureException(String message) {
    super(message);
  }
}
