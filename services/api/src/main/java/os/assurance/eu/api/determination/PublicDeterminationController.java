package os.assurance.eu.api.determination;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Free, no-signup AI Act applicability check (Plan 08 Task 6). No session, no tenant, nothing stored.
 * POSTs are rate limited per IP by {@code AuthRateLimitFilter}.
 */
@RestController
@RequestMapping("/api/public/v1/determination")
public class PublicDeterminationController {
  static final int MAX_BODY_BYTES = 16_384;

  private final DeterminationService determinationService;
  private final ObjectMapper json;

  public PublicDeterminationController(DeterminationService determinationService, ObjectMapper json) {
    this.determinationService = determinationService;
    this.json = json;
  }

  @GetMapping("/questionnaire")
  public QuestionnaireDefinition questionnaire() {
    return determinationService.questionnaire();
  }

  @PostMapping("/preview")
  public Map<String, Object> preview(HttpServletRequest request) throws IOException {
    if (request.getContentLengthLong() > MAX_BODY_BYTES) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body too large");
    }
    byte[] body;
    try (InputStream in = request.getInputStream()) {
      // Also caps chunked bodies that send no Content-Length.
      body = in.readNBytes(MAX_BODY_BYTES + 1);
    }
    if (body.length > MAX_BODY_BYTES) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body too large");
    }
    Map<String, Object> parsed;
    try {
      parsed = json.readValue(body, new TypeReference<Map<String, Object>>() {});
    } catch (IOException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Body must be a JSON object");
    }
    Object answers = parsed == null ? null : parsed.get("answers");
    if (!(answers instanceof Map<?, ?> map)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Questionnaire answers are required");
    }
    @SuppressWarnings("unchecked")
    Map<String, Object> typed = (Map<String, Object>) map;
    return determinationService.preview(typed);
  }
}
