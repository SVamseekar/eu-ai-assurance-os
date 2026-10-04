package os.assurance.eu.api.auth;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PasswordResetController {
  private final PasswordResetService service;

  public PasswordResetController(PasswordResetService service) {
    this.service = service;
  }

  @PostMapping("/auth/password/forgot")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public Map<String, String> forgot(@RequestBody ForgotBody body) {
    service.forgot(body == null ? null : body.email());
    return Map.of("status", "accepted");
  }

  @PostMapping("/auth/password/reset")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void reset(@RequestBody ResetBody body) {
    service.reset(body == null ? null : body.token(), body == null ? null : body.newPassword());
  }

  public record ForgotBody(String email) {}

  public record ResetBody(String token, String newPassword) {}
}
