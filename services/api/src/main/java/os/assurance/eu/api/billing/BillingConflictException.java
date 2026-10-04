package os.assurance.eu.api.billing;

/** A request that conflicts with the workspace's billing state; answered as 409 with a readable message. */
public class BillingConflictException extends RuntimeException {
  public BillingConflictException(String message) {
    super(message);
  }
}
