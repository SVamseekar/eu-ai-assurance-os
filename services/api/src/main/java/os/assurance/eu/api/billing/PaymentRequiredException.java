package os.assurance.eu.api.billing;

public class PaymentRequiredException extends RuntimeException {
  private final String code;

  public PaymentRequiredException(String code, String message) {
    super(message);
    this.code = code;
  }

  public String code() {
    return code;
  }
}
