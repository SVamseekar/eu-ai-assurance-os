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
}
