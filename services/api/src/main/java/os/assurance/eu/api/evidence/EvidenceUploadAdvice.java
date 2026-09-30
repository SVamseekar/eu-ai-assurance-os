package os.assurance.eu.api.evidence;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class EvidenceUploadAdvice {
  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<Map<String, String>> fileTooLarge(MaxUploadSizeExceededException ex) {
    return ResponseEntity.status(413).body(Map.of(
        "error", "file_too_large",
        "message", "Files up to 25 MB are supported."));
  }
}
