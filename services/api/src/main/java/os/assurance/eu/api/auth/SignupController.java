package os.assurance.eu.api.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SignupController {
  private final SignupService signup;

  public SignupController(SignupService signup) {
    this.signup = signup;
  }

  @PostMapping("/auth/signup")
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, String> signup(@Valid @RequestBody SignupRequest request) {
    signup.signup(request);
    return Map.of("status", "verification_sent");
  }

  @PostMapping("/auth/verify-email")
  public TokenResponse verify(@Valid @RequestBody TokenBody body) {
    return signup.verify(body.token());
  }

  @PostMapping("/auth/verify-email/resend")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public Map<String, String> resend(@RequestBody EmailBody body) {
    signup.resend(body == null ? null : body.email());
    return Map.of("status", "accepted");
  }

  public record TokenBody(@NotBlank String token) {}

  public record EmailBody(String email) {}
}
