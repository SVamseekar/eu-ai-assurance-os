package os.assurance.eu.api.auth;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RateLimitAdvice {
  @ExceptionHandler(TooManyAttemptsException.class)
  public ResponseEntity<Map<String, String>> tooMany(TooManyAttemptsException ex) {
    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
        .header("Retry-After", "900")
        .body(Map.of("error", "too_many_requests"));
  }
}
