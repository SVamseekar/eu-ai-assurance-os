package os.assurance.eu.api.publicclaims;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/public-claims")
public class PublicClaimsController {
  private final PublicClaimsService publicClaims;

  public PublicClaimsController(PublicClaimsService publicClaims) {
    this.publicClaims = publicClaims;
  }

  @GetMapping
  public PublicClaimsViews.Index list() {
    return publicClaims.index();
  }

  @GetMapping("/{slug}")
  public PublicClaimsViews.Teaser get(@PathVariable String slug) {
    try {
      return publicClaims.teaser(slug);
    } catch (PublicClaimsNotFoundException ex) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
    }
  }

  @GetMapping("/{slug}/evgraph")
  public PublicClaimsViews.Artifacts evgraph(@PathVariable String slug) {
    try {
      return publicClaims.artifacts(slug);
    } catch (PublicClaimsNotFoundException ex) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
    }
  }
}
