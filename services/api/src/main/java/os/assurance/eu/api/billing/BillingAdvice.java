package os.assurance.eu.api.billing;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class BillingAdvice {
  @ExceptionHandler(PaymentRequiredException.class)
  ResponseEntity<Map<String, String>> paymentRequired(PaymentRequiredException ex) {
    return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(Map.of(
        "error", "plan_limit",
        "code", ex.code(),
        "message", ex.getMessage(),
        "upgradeUrl", "/settings#billing"));
  }

  @ExceptionHandler(WebhookSignatureException.class)
  ResponseEntity<Map<String, String>> badSignature(WebhookSignatureException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "invalid_signature"));
  }

  /** Dodo down, slow or refusing: the caller gets a clear 502 instead of a server error. */
  @ExceptionHandler(org.springframework.web.client.RestClientException.class)
  ResponseEntity<Map<String, String>> providerUnavailable(org.springframework.web.client.RestClientException ex) {
    return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
        "error", "billing_provider_unavailable",
        "message", "The billing provider is unavailable. Try again in a moment."));
  }

  @ExceptionHandler(BillingConflictException.class)
  ResponseEntity<Map<String, String>> conflict(BillingConflictException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "billing_conflict", "message", ex.getMessage()));
  }
}
