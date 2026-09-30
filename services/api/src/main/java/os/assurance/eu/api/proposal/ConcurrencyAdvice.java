package os.assurance.eu.api.proposal;

import java.util.Map;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ConcurrencyAdvice {
  @ExceptionHandler(OptimisticLockingFailureException.class)
  public ResponseEntity<Map<String, String>> conflict(OptimisticLockingFailureException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(Map.of("error", "concurrent_update", "message", "Someone else changed this item. Reload and try again."));
  }
}
